package com.intelligent.bot.server.wss.handle;


import com.intelligent.bot.api.midjourney.support.TaskCondition;
import com.intelligent.bot.enums.mj.MessageType;
import com.intelligent.bot.enums.mj.TaskAction;
import com.intelligent.bot.model.mj.data.ContentParseData;
import com.intelligent.bot.utils.mj.ConvertUtils;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * Pan 消息处理.
 * 完成(create): **Alaska sled dog --niji 5 --style cute --chaos 3** - Pan Right by <@1139160503432794132> (fast)
 */
@Component
public class PanSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX_1 = "\\*\\*(.*?)\\*\\* - Pan by <@\\d+> \\((.*?)\\)";

	private static final String CONTENT_REGEX_2 = "\\*\\*(.*?)\\*\\* - Pan (.*?) by <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = getParseData(content);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {
			TaskCondition condition = new TaskCondition()
					.setActionSet(Arrays.asList(
							TaskAction.PAN_UP,
							TaskAction.PAN_DOWN,
							TaskAction.PAN_LEFT,
							TaskAction.REROLL,
							TaskAction.PAN_RIGHT))
					.setFinalPromptEn(parseData.getPrompt());
			findAndFinishImageTask(condition, parseData.getPrompt(), message);
		}
	}

	private ContentParseData getParseData(String content) {
		ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX_1);
		if (parseData == null) {
			parseData = ConvertUtils.parseContent(content, CONTENT_REGEX_2);
		}
		return parseData;
	}

}
