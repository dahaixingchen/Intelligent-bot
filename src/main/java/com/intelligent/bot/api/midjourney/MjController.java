package com.intelligent.bot.api.midjourney;

import cn.hutool.core.text.CharSequenceUtil;
import com.intelligent.bot.api.midjourney.loadbalancer.DiscordLoadBalancer;
import com.intelligent.bot.api.midjourney.support.TaskCondition;
import com.intelligent.bot.base.exception.E;
import com.intelligent.bot.base.result.B;
import com.intelligent.bot.constant.CommonConst;
import com.intelligent.bot.enums.mj.TaskAction;
import com.intelligent.bot.enums.mj.TaskStatus;
import com.intelligent.bot.model.SysConfig;
import com.intelligent.bot.model.Task;
import com.intelligent.bot.model.req.mj.*;
import com.intelligent.bot.service.baidu.BaiDuService;
import com.intelligent.bot.service.mj.TaskService;
import com.intelligent.bot.service.mj.TaskStoreService;
import com.intelligent.bot.service.sys.CheckService;
import com.intelligent.bot.service.sys.IMjTaskService;
import com.intelligent.bot.utils.mj.BannedPromptUtils;
import com.intelligent.bot.utils.mj.MimeTypeUtils;
import com.intelligent.bot.utils.mj.SnowFlake;
import com.intelligent.bot.utils.sys.IDUtil;
import com.intelligent.bot.utils.sys.JwtUtil;
import com.intelligent.bot.utils.sys.RedisUtil;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mj")
@Log4j2
public class MjController {

	@Resource
	BaiDuService baiDuService;
	@Resource
	TaskStoreService taskStoreService;
	@Resource
	CheckService checkService;
	@Resource
	TaskService taskService;

	@Resource
	IMjTaskService mjTaskService;

	@Resource
	DiscordLoadBalancer discordLoadBalancer;

	@PostMapping(value = "/submit",name = "提交Imagine或UV任务")
	public B<Task> submit(@RequestBody SubmitReq req) {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		if(null == sysConfig.getIsOpenMj() || sysConfig.getIsOpenMj() == 0){
			throw new E("暂未开启Mj");
		}
		String prompt = req.getPrompt();
		if (CharSequenceUtil.isBlank(prompt)) {
			throw new E("prompt不能为空");
		}
		if(CharSequenceUtil.isBlank(req.getPrompt()) || req.getPrompt().contains("nsfw") || !baiDuService.textToExamine(req.getPrompt())){
			throw new E("生成内容不合规");
		}
		prompt = prompt.trim();
		Task task = newTask();
		task.setAction(TaskAction.IMAGINE);
		task.setPrompt(prompt);
		task.setPlotMode(req.getPlotMode());
		String promptEn = translatePrompt(prompt);
		if (BannedPromptUtils.isBanned(promptEn)) {
			throw new E("可能包含敏感词");
		}
		int userNumber = CommonConst.MJ_NUMBER;
		String plotMode = "--relax";
		if(req.getPlotMode() == 2){
			userNumber = CommonConst.MJ_NUMBER_FAST;
			plotMode = "--fast";
		}
		if(req.getPlotMode() == 3){
			userNumber = CommonConst.MJ_NUMBER_TURBO;
			plotMode = "--turbo";
		}
		checkService.checkUser(JwtUtil.getUserId(),userNumber);
		task.setPromptEn(promptEn
				+(!StringUtils.isEmpty(req.getNo()) ? " --no "+(this.baiDuService.translateToEnglish(req.getNo())) : "")
				+(!StringUtils.isEmpty(req.getVersion()) ? " " +req.getVersion() : "")
				+(!StringUtils.isEmpty(req.getStyle()) ? " " +req.getStyle() : "")
				+(!StringUtils.isEmpty(req.getAr()) ? " " +req.getAr() : "")
				+(!StringUtils.isEmpty(req.getQ()) ? " " +req.getQ() : "")
				+(!StringUtils.isEmpty(req.getStylize()) ? " " + req.getStylize() : "")
				+(!StringUtils.isEmpty(req.getChaos()) ? " " +req.getChaos() : "")
				+ " " + plotMode);
		task.setDescription("/imagine " + prompt);
		this.taskService.submitImagine(task,req.getImgList());
		return B.okBuild(task);
	}

	@PostMapping(value = "/submit/uv",name = "提交选中放大或变换任务")
	public B<Task> submitUV(@RequestBody UVSubmitReq req) {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		if(null == sysConfig.getIsOpenMj() || sysConfig.getIsOpenMj() == 0){
			throw new E("暂未开启Mj");
		}
		if (null == req.getId()) {
			throw new E("id 不能为空");
		}
		if(!Arrays.asList(TaskAction.UPSCALE, TaskAction.VARIATION, TaskAction.REROLL).contains(req.getTaskAction())){
			throw new E("action参数错误");
		}
		String description = "/up " + req.getId();
		if (TaskAction.REROLL.equals(req.getTaskAction())) {
			description += " R";
		} else {
			description += " " + req.getTaskAction().name().charAt(0) + req.getIndex();
		}
		if (TaskAction.UPSCALE.equals(req.getTaskAction())) {
			TaskCondition condition = new TaskCondition().setDescription(description);
			Task existTask = this.taskStoreService.findOne(condition);
			if (existTask != null) {
				throw new E("任务已存在");
			}
		}
		Task targetTask = this.taskStoreService.get(req.getId());
		Task task = version( req.getTaskAction(), description, req.getIndex(),targetTask);
		if (TaskAction.UPSCALE.equals(req.getTaskAction())) {
			this.taskService.submitUpscale(task, targetTask.getMessageId(), targetTask.getMessageHash(), req.getIndex(),targetTask.getFlags());
		} else if (TaskAction.VARIATION.equals(req.getTaskAction())) {
			this.taskService.submitVariation(task, targetTask.getMessageId(), targetTask.getMessageHash(), req.getIndex(),targetTask.getFlags());
		} else {
			this.taskService.submitReroll(task, targetTask.getMessageId(), targetTask.getMessageHash(), targetTask.getFlags());
		}
		return B.okBuild(task);
	}

	@PostMapping(value = "/submit/vary",name = "变换放大后的图片")
	public B<Task> submitVersion(@RequestBody UVSubmitReq req) {
		if(!Arrays.asList(
				TaskAction.VARY_STRONG,
				TaskAction.VARY_SUBTLE,
				TaskAction.UPSCALE2,
				TaskAction.UPSCALE4,
				TaskAction.ZOOM_OUT_2X,
				TaskAction.ZOOM_OUT_15X,
				TaskAction.PAN_UP,
				TaskAction.PAN_DOWN,
				TaskAction.PAN_LEFT,
				TaskAction.PAN_RIGHT).contains(req.getTaskAction())){
			throw new E("action参数错误");
		}
		Object value = null;
		switch (req.getTaskAction()){
			case VARY_STRONG:
				value = CommonConst.VARY_STRONG;
				break;
			case VARY_SUBTLE:
				value = CommonConst.VARY_SUBTLE;
				break;
			case UPSCALE2:
				value = CommonConst.UPSCALE2;
				break;
			case UPSCALE4:
				value = CommonConst.UPSCALE4;
				break;
			case ZOOM_OUT_2X:
				value = CommonConst.ZOOM_OUT_2X;
				break;
			case ZOOM_OUT_15X:
				value = CommonConst.ZOOM_OUT_15X;
				break;
			case PAN_LEFT:
				value = CommonConst.PAN_LEFT;
				break;
			case PAN_RIGHT:
				value = CommonConst.PAN_RIGHT;
				break;
			case PAN_UP:
				value = CommonConst.PAN_UP;
				break;
			case PAN_DOWN:
				value = CommonConst.PAN_DOWN;
				break;
			default:
				throw new E("异常指令");
		}
		String description = "/"+value  + " " + req.getId();
		TaskCondition condition = new TaskCondition().setDescription(description);
		Task existTask = this.taskStoreService.findOne(condition);
		if (existTask != null) {
			throw new E("任务已存在");
		}
		Task targetTask = this.taskStoreService.get(req.getId());
		Task task = version(req.getTaskAction(), description,1,targetTask);
		if(Arrays.asList(
				TaskAction.VARY_STRONG,
				TaskAction.VARY_SUBTLE,
				TaskAction.UPSCALE2,
				TaskAction.UPSCALE4).contains(req.getTaskAction())){
			this.taskService.submitVary(String.valueOf(value),task, targetTask.getMessageId(), targetTask.getMessageHash(),targetTask.getFlags());
		}
		if(Arrays.asList(
				TaskAction.ZOOM_OUT_2X,
				TaskAction.ZOOM_OUT_15X).contains(req.getTaskAction())){
			this.taskService.submitZoomOut(Integer.parseInt(value.toString()),task, targetTask.getMessageId(), targetTask.getMessageHash(),targetTask.getFlags());
		}
		if(Arrays.asList(
				TaskAction.PAN_UP,
				TaskAction.PAN_DOWN,
				TaskAction.PAN_LEFT,
				TaskAction.PAN_RIGHT).contains(req.getTaskAction())){
			this.taskService.submitPan(String.valueOf(value),task, targetTask.getMessageId(), targetTask.getMessageHash(),targetTask.getFlags());
		}
		return B.okBuild(task);
	}

	@PostMapping(value = "/describe",name = "提交Describe图生文任务")
	public B<Task> describe(@RequestBody DescribeReq req) {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		if(null == sysConfig.getIsOpenMj() || sysConfig.getIsOpenMj() == 0){
			throw new E("暂未开启Mj");
		}
		if (CharSequenceUtil.isBlank(req.getBase64())) {
			throw new E("校验错误");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(req.getBase64());
		} catch (MalformedURLException e) {
			throw new E("base64格式错误");
		}
		checkService.checkUser(JwtUtil.getUserId(), CommonConst.MJ_DESCRIBE_NUMBER);
		Task task = newTask();
		task.setAction(TaskAction.DESCRIBE);
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setDescription("/describe " + taskFileName);
		this.taskService.submitDescribe(task, dataUrl);
		return B.okBuild(task);
	}

	@PostMapping(value = "/shorten",name = "咒语分析")
	public B<Task> shorten(@RequestBody ShortenReq req) {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		if(null == sysConfig.getIsOpenMj() || sysConfig.getIsOpenMj() == 0){
			throw new E("暂未开启Mj");
		}
		if (CharSequenceUtil.isBlank(req.getPrompt())) {
			throw new E("校验错误");
		}
		checkService.checkUser(JwtUtil.getUserId(), CommonConst.MJ_DESCRIBE_NUMBER);
		Task task = newTask();
		task.setAction(TaskAction.SHORTEN);
		task.setDescription("/shorten " + task.getId());
		task.setPrompt(req.getPrompt());
		task.setPromptEn(req.getPrompt());
		this.taskService.submitShorten(task);
		return B.okBuild(task);
	}

	@PostMapping(value = "/blend",name = "提交Blend任务")
	public B<Task> blend(@RequestBody SubmitBlendReq req) {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		if(null == sysConfig.getIsOpenMj() || sysConfig.getIsOpenMj() == 0){
			throw new E("暂未开启Mj");
		}
		checkService.checkUser(JwtUtil.getUserId(), CommonConst.MJ_BLEND_NUMBER);
		List<String> base64Array = req.getBase64Array();
		if (base64Array == null || base64Array.size() < 2 || base64Array.size() > 5) {
			throw new E("base64List参数错误");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		List<DataUrl> dataUrlList = new ArrayList<>();
		try {
			for (String base64 : base64Array) {
				DataUrl dataUrl = serializer.unserialize(base64);
				dataUrlList.add(dataUrl);
			}
		} catch (MalformedURLException e) {
			throw new E("base64格式错误");
		}
		Task task = newTask();
		task.setAction(TaskAction.BLEND);
		task.setDescription("/blend " + task.getId() + " " + dataUrlList.size());
		this.taskService.submitBlend(task, dataUrlList,req.getDimensions());
		return B.okBuild(task);
	}

	@PostMapping(value = "/public/status",name = "mj任务公开状态修改")
	public B<Void> privateTask(@RequestBody PrivateTask req) {
		this.mjTaskService.lambdaUpdate()
				.set(Task::getPublicStatus,req.getPublicStatus())
				.eq(Task::getId,req.getId())
				.update();
		return B.okBuild();
	}

	@GetMapping(value = "/queue",name = "查询任务队列")
	public List<Task> queue() {
		return this.discordLoadBalancer.getQueueTaskIds().stream()
				.map(this.taskStoreService::get).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Task::getSubmitTime))
				.collect(Collectors.toList());
	}



	@PostMapping("getTask")
	public B<Task> getTask(@RequestBody TaskReq req) {
		return B.okBuild(taskStoreService.get(req.getId()));
	}
//	@PostMapping("callBack")
	public void callBack(@RequestBody MjCallBack mjTask) throws Exception {
//		log.info("mj开始回调,回调内容：{}", mjTask);
//		if(mjTask.getStatus() == TaskStatus.SUCCESS){
//			MjTask targetTask = new MjTask();
//			String localImgUrl = FileUtil.base64ToImage((mjTask.getImageUrl()));
//			targetTask.setImageUrl(localImgUrl);
//			targetTask.setId(mjTask.getId());
//			targetTask.setStatus(TaskStatus.SUCCESS);
//			mjTask.setImageUrl(targetTask.getImageUrl());
//			asyncService.updateMjTask(targetTask);
//		}
//		SseEmitterServer.sendMessage(mjTask.getUserId(),mjTask);
	}

	private Task newTask() {
		SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
		Task task = new Task();
		task.setId(IDUtil.getNextId());
		task.setSubmitTime(System.currentTimeMillis());
		task.setNotifyHook(sysConfig.getApiUrl() + CommonConst.MJ_CALL_BACK_URL);
		task.setNonce(SnowFlake.INSTANCE.nextId());
		task.setUserId(JwtUtil.getUserId());
		task.setSubType(1);
		return task;
	}

	private String translatePrompt(String prompt) {
		String promptEn;
		int paramStart = prompt.indexOf(" --");
		if (paramStart > 0) {
			promptEn = this.baiDuService.translateToEnglish(prompt.substring(0, paramStart)).trim() + prompt.substring(paramStart);
		} else {
			promptEn = this.baiDuService.translateToEnglish(prompt).trim();
		}
		if (CharSequenceUtil.isBlank(promptEn)) {
			promptEn = prompt;
		}
		return promptEn;
	}

	private Task version(TaskAction taskAction,String description,int index,Task targetTask){
		if (targetTask == null) {
			throw new E("关联任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			throw new E("关联任务状态错误");
		}
		checkService.checkUser(JwtUtil.getUserId(),taskAction.equals(TaskAction.VARIATION) ? CommonConst.MJ_V_NUMBER : CommonConst.MJ_U_NUMBER);
		Task task = newTask();
		task.setAction(taskAction);
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setFinalPrompt(targetTask.getFinalPrompt());
		task.setRelatedTaskId(targetTask.getId());
		task.setProgressMessageId(targetTask.getMessageId());
		task.setDiscordInstanceId(targetTask.getDiscordInstanceId());
		task.setDescription(description);
		task.setIndex(index);
		return task;
	}
}
