package com.intelligent.bot.server.wss.handle;

import com.intelligent.bot.enums.mj.MessageType;
import com.intelligent.bot.model.Task;
import com.intelligent.bot.service.mj.TaskStoreService;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * describe消息处理.
 */
@Component
public class DescribeSuccessHandler extends MessageHandler {

	@Resource
	TaskStoreService taskStoreService;
	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataObject> interaction = message.optObject("interaction");
		if(interaction.isPresent()){
			if(MessageType.CREATE.equals(messageType) && "shorten".equals(interaction.get().getString("name"))){
				Task task = this.discordLoadBalancer.getRunningTaskByNonce(message.getString("nonce"));
				if (task == null) {
					return;
				}
				task.setMessageId(interaction.get().getString("id"));
				taskStoreService.save(task);
			}
			if("describe".equals(interaction.get().getString("name"))){
				DataArray embeds = message.getArray("embeds");
				if (embeds.isEmpty()) {
					return;
				}
				String description = embeds.getObject(0).getString("description");
				Optional<DataObject> imageOptional = embeds.getObject(0).optObject("image");
				if (!imageOptional.isPresent()) {
					return;
				}
				String imageUrl = imageOptional.get().getString("url");
				String taskId = this.discordHelper.findTaskIdWithCdnUrl(imageUrl);
				updateTask(description,taskId,imageUrl,message);
			}
			if("shorten".equals(interaction.get().getString("name"))){
				DataArray embeds = message.getArray("embeds");
				if (embeds.isEmpty()) {
					return;
				}
				String description = embeds.getObject(0).getString("description");
				Task task = this.discordLoadBalancer.getRunningTaskByMessageId(interaction.get().getString("id"));
				if (task == null) {
					return;
				}
				updateTask(description,String.valueOf(task.getId()),null,message);
			}
		}
	}
	public void updateTask(String description,String taskId,String imageUrl,DataObject message){
		Task task = this.discordLoadBalancer.getRunningTask(Long.valueOf(taskId));
		if (task == null) {
			return;
		}
		task.setPrompt(description);
		task.setPromptEn(description);
		task.setFinalPrompt(description);
		task.setImageUrl(!StringUtils.isEmpty(imageUrl) ? replaceCdnUrl(imageUrl) : "");
		finishTask(task, message);
		task.awake();
	}
}
