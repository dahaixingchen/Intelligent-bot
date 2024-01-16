package com.intelligent.bot.api.sys;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.bot.annotate.AvoidRepeatRequest;
import com.intelligent.bot.base.exception.E;
import com.intelligent.bot.base.result.B;
import com.intelligent.bot.constant.CommonConst;
import com.intelligent.bot.dao.MjTaskDao;
import com.intelligent.bot.enums.mj.TaskAction;
import com.intelligent.bot.enums.mj.TaskStatus;
import com.intelligent.bot.enums.sys.SendType;
import com.intelligent.bot.model.*;
import com.intelligent.bot.model.base.BasePageHelper;
import com.intelligent.bot.model.gpt.Message;
import com.intelligent.bot.model.req.sys.*;
import com.intelligent.bot.model.res.sys.*;
import com.intelligent.bot.model.res.sys.admin.GptRoleQueryRes;
import com.intelligent.bot.model.spark.Text;
import com.intelligent.bot.service.sys.*;
import com.intelligent.bot.utils.sys.FileUtil;
import com.intelligent.bot.utils.sys.ImgUtil;
import com.intelligent.bot.utils.sys.JwtUtil;
import com.intelligent.bot.utils.sys.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/client")
@Log4j2
@Transactional(rollbackFor = E.class)
public class ClientController {

    @Resource
    IUserService userService;
    @Resource
    IMessageLogService useLogService;
    @Resource
    IAnnouncementService announcementService;
    @Resource
    IProductService productService;
    @Resource
    IOrderService orderService;
    @Resource
    MjTaskDao mjTaskDao;
    @Resource
    IMjTaskService mjTaskService;
    @Resource
    IGptRoleService gptRoleService;
    @Resource
    ISignInService signInService;


    @RequestMapping(value = "/home", name = "用户首页信息")
    public B<ClientHomeRes> home(@Validated @RequestBody ClientHomeReq req) {
        SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        User user = userService.getById(JwtUtil.getUserId());
        if(!user.getAvatar().contains("http")){
            user.setAvatar(sysConfig.getImgReturnUrl() + user.getAvatar());
        }
        List<MessageLog> logList;
        if(!Objects.equals(req.getSendType(), SendType.GPT.getType())){
            if(Objects.equals(req.getSendType(), SendType.SPARK_V2.getType())){
                logList = useLogService.lambdaQuery()
                        .select(MessageLog::getUseValue,MessageLog::getId,MessageLog::getSendType)
                        .eq(MessageLog::getUserId, JwtUtil.getUserId())
                        .in(MessageLog::getSendType,SendType.SPARK_V2.getType(),SendType.SPARK_V3.getType())
                        .orderByDesc(MessageLog::getId)
                        .list();
            }else {
                logList = useLogService.lambdaQuery()
                        .select(MessageLog::getUseValue,MessageLog::getId,MessageLog::getSendType)
                        .eq(MessageLog::getUserId, JwtUtil.getUserId())
                        .eq(MessageLog::getSendType, req.getSendType())
                        .orderByDesc(MessageLog::getId)
                        .list();
            }
        } else {
            logList = useLogService.lambdaQuery()
                    .select(MessageLog::getUseValue,MessageLog::getId,MessageLog::getSendType)
                    .eq(MessageLog::getUserId, JwtUtil.getUserId())
                    .in(MessageLog::getSendType, SendType.GPT.getType(),SendType.GPT_4.getType())
                    .orderByDesc(MessageLog::getId)
                    .list();
        }
        List<Announcement> list = announcementService.lambdaQuery().select(Announcement::getContent).orderByDesc(Announcement::getSort).last("limit 1").list();
        ClientHomeRes clientHomeRes = BeanUtil.copyProperties(user, ClientHomeRes.class);
        clientHomeRes.setAnnouncement((null != list && !list.isEmpty()) ? list.get(0).getContent() : "暂无通知公告");
        if(Objects.equals(req.getSendType(), SendType.MJ.getType())){
            List<MjTaskRes> mjTaskList = mjTaskDao.selectUserMjTask(JwtUtil.getUserId());
            List<MjTaskTransformRes> transformList = mjTaskDao.selectTransform();
            for (MjTaskRes mjTaskRes : mjTaskList) {
                if(Arrays.asList(
                        TaskAction.IMAGINE,
                        TaskAction.VARIATION,
                        TaskAction.REROLL,
                        TaskAction.BLEND,
                        TaskAction.VARY_STRONG,
                        TaskAction.VARY_SUBTLE,
                        TaskAction.ZOOM_OUT_2X,
                        TaskAction.ZOOM_OUT_15X
                        ).contains(mjTaskRes.getTaskAction())){
                    if(mjTaskRes.getTaskAction().equals(TaskAction.REROLL)
                            && (null != mjTaskRes.getRelatedTaskAction()
                            && Arrays.asList(
                            TaskAction.PAN_UP,
                            TaskAction.PAN_DOWN,
                            TaskAction.PAN_LEFT,
                            TaskAction.PAN_RIGHT).contains(mjTaskRes.getRelatedTaskAction()))){
                        mjTaskRes.setButtonType(4);
                    }else {
                        mjTaskRes.setButtonType(1);
                    }
                    switch (mjTaskRes.getTaskAction()){
                        case IMAGINE:
                            mjTaskRes.setTaskActionName("图片绘制");
                            break;
                        case VARIATION:
                            mjTaskRes.setTaskActionName("相似绘制");
                            break;
                        case BLEND:
                            mjTaskRes.setTaskActionName("多图混合");
                            break;
                        case REROLL:
                            mjTaskRes.setTaskActionName("重新执行");
                            break;
                        case VARY_STRONG:
                            mjTaskRes.setTaskActionName("重绘（增强）");
                            break;
                        case VARY_SUBTLE:
                            mjTaskRes.setTaskActionName("重绘（微妙）");
                            break;
                        case ZOOM_OUT_2X:
                            mjTaskRes.setTaskActionName("变焦（2x）");
                            break;
                        case ZOOM_OUT_15X:
                            mjTaskRes.setTaskActionName("变焦（1.5x）");
                            break;
                    }
                }
                if(TaskAction.UPSCALE.equals(mjTaskRes.getTaskAction())){
                    mjTaskRes.setButtonType(2);
                    mjTaskRes.setTaskActionName("图片放大");
                }
                if(Arrays.asList(
                        TaskAction.UPSCALE2,
                        TaskAction.UPSCALE4).contains(mjTaskRes.getTaskAction())){
                    mjTaskRes.setButtonType(3);
                    if(TaskAction.UPSCALE2.equals(mjTaskRes.getTaskAction())){
                        mjTaskRes.setTaskActionName("图片放大（2x）");
                    }else {
                        mjTaskRes.setTaskActionName("图片放大（4x）");
                    }
                }
                if(Arrays.asList(
                        TaskAction.PAN_UP,
                        TaskAction.PAN_DOWN,
                        TaskAction.PAN_LEFT,
                        TaskAction.PAN_RIGHT).contains(mjTaskRes.getTaskAction())){
                    mjTaskRes.setButtonType(4);
                    switch (mjTaskRes.getTaskAction()) {
                        case PAN_UP:
                            mjTaskRes.setTaskActionName("拉伸(上)");
                            break;
                        case PAN_DOWN:
                            mjTaskRes.setTaskActionName("拉伸(下)");
                            break;
                        case PAN_LEFT:
                            mjTaskRes.setTaskActionName("拉伸(左)");
                            break;
                        case PAN_RIGHT:
                            mjTaskRes.setTaskActionName("拉伸(右)");
                            break;
                    }
                }
                List<MjTaskTransformRes> taskTransformList = new ArrayList<>();
                for (MjTaskTransformRes transform : transformList) {
                    if(transform.getRelatedTaskId().equals(mjTaskRes.getId())){
                        taskTransformList.add(transform);
                    }
                }
                mjTaskRes.setTaskActionName(mjTaskRes.getTaskActionName() + " - " + (mjTaskRes.getStatus().equals(TaskStatus.SUCCESS) ? "成功" : "失败"));
                if(null !=  mjTaskRes.getImageUrl() && mjTaskRes.getImageUrl().contains(".jpg")){
                    mjTaskRes.setImageUrl(mjTaskRes.getImageUrl().startsWith("http") ? mjTaskRes.getImageUrl() : sysConfig.getImgReturnUrl() + mjTaskRes.getImageUrl());
                }
                mjTaskRes.setTaskTransformList(taskTransformList);
            }
            clientHomeRes.setMjTaskList(mjTaskList);
            return B.okBuild(clientHomeRes);

        }else {
            List<ClientHomeLogRes> homeLogResList = new ArrayList<>();
            logList.forEach(e -> {
                ClientHomeLogRes res = new ClientHomeLogRes();
                res.setId(e.getId());
                res.setSendType(e.getSendType());
                if (e.getSendType().equals(SendType.GPT.getType())
                        || e.getSendType().equals(SendType.GPT_4.getType())
                        || e.getSendType().equals(SendType.BING.getType())
                        || e.getSendType().equals(SendType.SPARK_V2.getType())
                        || e.getSendType().equals(SendType.SPARK_V3.getType())) {
                    if(e.getSendType().equals(SendType.SPARK_V2.getType())
                            || e.getSendType().equals(SendType.SPARK_V3.getType())){
                        res.setTitle(JSONObject.parseArray(e.getUseValue(), Text.class).get(0).getContent());
                        List<Text> messages = JSONObject.parseArray(e.getUseValue(), Text.class);
                        res.setContent(JSONObject.toJSONString(messages));
                    }else {
                        res.setTitle(JSONObject.parseArray(e.getUseValue(), Message.class).get(0).getContent());
                        List<Message> messages = JSONObject.parseArray(e.getUseValue(), Message.class);
                        messages.removeIf(m -> m.getRole().equals(Message.Role.SYSTEM.getValue()));
                        res.setContent(JSONObject.toJSONString(messages));
                    }
                } else {
                    MessageLogSave messageLogSave = JSONObject.parseObject(e.getUseValue(), MessageLogSave.class);
                    List<String> imgList = new ArrayList<>();
                    messageLogSave.getImgList().forEach( m ->{
                        imgList.add(m.startsWith("http") ? m : sysConfig.getImgReturnUrl() + m );
                    });
                    res.setTitle(messageLogSave.getPrompt());
                    messageLogSave.setImgList(imgList);
                    res.setContent(JSONObject.toJSONString(messageLogSave));
                }
                homeLogResList.add(res);

            });
            clientHomeRes.setLogList(homeLogResList);
        }
        clientHomeRes.setGptRoleList(gptRoleService.getGptRoleLimit10());
        return B.okBuild(clientHomeRes);
    }

    @RequestMapping(value = "/updateAvatar", name = "修改头像")
    @AvoidRepeatRequest(intervalTime = 2626560, msg = "头像每个月可更换一次")
    public B<Void> updateAvatar(MultipartFile file) throws IOException {
        User user = new User();
        String fileName = ImgUtil.uploadMultipartFile(file, FileUtil.getFileName());
        user.setAvatar(fileName);
        user.setId(JwtUtil.getUserId());
        userService.updateById(user);
        return B.okBuild();
    }

    @RequestMapping(value = "/updatePassword", name = "修改密码")
    @AvoidRepeatRequest(intervalTime = 2626560, msg = "密码每个月可更换一次")
    public B<String> updatePassword(@Validated @RequestBody ClientUpdatePasswordReq req) {
        User user = new User();
        user.setId(JwtUtil.getUserId());
        user.setPassword(SecureUtil.md5(req.getPassword()));
        userService.updateById(user);
        RedisUtil.deleteObject(CommonConst.REDIS_KEY_PREFIX_TOKEN + user.getId());
        return B.okBuild("修改成功请重新登陆");
    }

    @RequestMapping(value = "/recharge", name = "充值")
    public B<ClientRechargeRes> recharge() {
        PayConfig payConfig = RedisUtil.getCacheObject(CommonConst.PAY_CONFIG);
        return B.okBuild(ClientRechargeRes.builder()
                .productList(productService.getProductList())
                .orderList(orderService.userOrderList(JwtUtil.getUserId()))
                .payType(payConfig.getPayType())
                .build());
    }

    @RequestMapping(value = "/register/method",name = "查询注册方式", method = RequestMethod.POST)
    public B<Integer> getRegisterMethod() {
        SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        return B.okBuild(sysConfig.getRegistrationMethod());
    }

    @RequestMapping(value = "/getFunctionState",name = "获取配置开启状态", method = RequestMethod.POST)
    public B<GetFunctionState> getOpenSdState() {
        SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        return B.okBuild( GetFunctionState.builder()
                .isOpenSd(sysConfig.getIsOpenSd())
                .isOpenFlagStudio(sysConfig.getIsOpenFlagStudio())
                .isOpenBing(sysConfig.getIsOpenBing())
                .isOpenMj(sysConfig.getIsOpenMj())
                .isOpenGpt(sysConfig.getIsOpenGpt())
                .isOpenSpark(sysConfig.getIsOpenSpark())
                .isOpenGptOfficial(sysConfig.getIsOpenGptOfficial()).build()
        );
    }

    @RequestMapping(value = "/delete/log", name = "删除对话")
    public B<Void> deleteLog(@Validated @RequestBody ClientDeleteLog req) {
        useLogService.removeById(req.getId());
        return B.okBuild();
    }

    @RequestMapping(value = "/empty/log", name = "清空对话")
    public B<Void> emptyLog(@Validated @RequestBody ClientEmptyLog req) {
        useLogService.lambdaUpdate()
                .set(MessageLog::getDeleted,1)
                .eq(MessageLog::getUserId,JwtUtil.getUserId())
                .eq(MessageLog::getSendType,req.getSendType())
                .update();
        return B.okBuild();
    }

    @RequestMapping(value = "/delete/mj/task", name = "删除mj任务")
    public B<Void> deleteMjTask(@Validated @RequestBody ClientDeleteLog req) {
        mjTaskService.deleteMjTask(req.getId());
        return B.okBuild();
    }

    @RequestMapping(value = "/empty/mj/task", name = "清空mj任务")
    public B<Void> emptyMjTask() {
        mjTaskService.emptyMjTask(JwtUtil.getUserId());
        return B.okBuild();
    }

    @RequestMapping(value = "/upload/img", name = "上传图片")
    @AvoidRepeatRequest(intervalTime = 10, msg = "请勿频繁上传图片")
    public B<String> uploadImg(MultipartFile file) throws IOException {
        String oldFileName = Objects.requireNonNull(file.getOriginalFilename()).substring(0, file.getOriginalFilename().lastIndexOf("."));
        String fileName = ImgUtil.uploadMultipartFile(file, oldFileName);
        SysConfig cacheObject = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        return B.okBuild(cacheObject.getImgReturnUrl() + fileName);
    }

    @RequestMapping(value = "/client/conf", name = "获取客户端配置，logo，名称")
    public B<ClientConfRes> clientConf(){
        SysConfig cacheObject = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        ClientConfRes clientConfRes = BeanUtil.copyProperties(cacheObject, ClientConfRes.class);
        clientConfRes.setClientLogo(cacheObject.getImgReturnUrl() + clientConfRes.getClientLogo());
        return B.okBuild(clientConfRes);
    }

    @RequestMapping(value = "/gallery", name = "画廊")
    public B<Page<GalleryRes>> gallery(@Validated @RequestBody BasePageHelper basePageHelper){
        Page<GalleryRes> page = new Page<>(basePageHelper.getPageNumber(),basePageHelper.getPageSize());
        Page<GalleryRes> gallery = mjTaskDao.getGallery(page);
        SysConfig cacheObject = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        gallery.getRecords().forEach( g ->{
            if(!g.getImageUrl().contains("http")){
                g.setImageUrl(cacheObject.getImgReturnUrl() + g.getImageUrl());
            }
            if(!g.getAvatar().contains("http")){
                g.setAvatar(cacheObject.getImgReturnUrl() + g.getAvatar());
            }
        });
        return B.okBuild(gallery);
    }

    @RequestMapping(value = "/updateMobile", name = "修改手机号")
    @AvoidRepeatRequest(intervalTime = 2626560, msg = "手机号每个月可更换一次")
    public B<Void> updateMobile(@Validated @RequestBody ClientUpdateMobileReq req) {
        Long count = userService.lambdaQuery()
                .eq(User::getMobile, req.getMobile())
                .ne(User::getId, JwtUtil.getUserId())
                .count();
        if(count > 0){
            throw new E("手机号已存在");
        }
        User user = new User();
        user.setId(JwtUtil.getUserId());
        user.setMobile(req.getMobile());
        userService.updateById(user);
        return B.okBuild();
    }
    @RequestMapping(value = "/getGptRole", name = "获取随机的十个gpt角色列表")
    public B<List<GptRoleQueryRes>> getGptRole() {
        return B.okBuild(gptRoleService.getGptRoleLimit10());
    }

    @RequestMapping(value = "/sign/in", name = "签到")
    @AvoidRepeatRequest(intervalTime = 20, msg = "请勿重复签到")
    public B<Void> signIn(){
        Long count = signInService.lambdaQuery()
                .eq(SignIn::getUserId, JwtUtil.getUserId())
                .eq(SignIn::getSignInDate, LocalDate.now())
                .count();
        if(count > 0){
            throw new E("当日已签到");
        }
        SysConfig cacheObject = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        SignIn signIn = new SignIn();
        signIn.setUserId(JwtUtil.getUserId());
        signIn.setSignInDate(LocalDate.now());
        signIn.setNumber(cacheObject.getSignInNumber());
        signInService.save(signIn);
        return B.okBuild();
    }

    @RequestMapping(value = "/sign/in/query", name = "查询签到记录")
    public B<Page<SignIn>> signInQuery(@Validated @RequestBody BasePageHelper basePageHelper){
        Page<SignIn> page = new Page<>(basePageHelper.getPageNumber(),basePageHelper.getPageSize());
        Page<SignIn> SignInPage = signInService.querySignIn(page,JwtUtil.getUserId());
        return B.okBuild(SignInPage);
    }

    @RequestMapping(value = "/updateNickName", name = "修改昵称")
    public B<Void> updateNickName(@Validated @RequestBody ClientUpdateNickNameReq req) {
        User user = new User();
        user.setId(JwtUtil.getUserId());
        user.setName(req.getNickName());
        userService.updateById(user);
        return B.okBuild();
    }
}
