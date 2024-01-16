package com.intelligent.bot.server.wss.handle;

import com.intelligent.bot.api.midjourney.support.TaskCondition;
import com.intelligent.bot.enums.mj.MessageType;
import com.intelligent.bot.enums.mj.TaskAction;
import com.intelligent.bot.model.mj.data.ContentParseData;
import com.intelligent.bot.utils.mj.ConvertUtils;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * variation消息处理.
 * 完成(create): **cat** - Variations (Strong或Subtle) by <@1012983546824114217> (relaxed)
 * 完成(create): **cat** - Variations by <@1012983546824114217> (relaxed)
 */
@Component
public class VariationSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX_1 = "\\*\\*(.*?)\\*\\* - Variations by <@\\d+> \\((.*?)\\)";
	private static final String CONTENT_REGEX_2 = "\\*\\*(.*?)\\*\\* - Variations \\(.*?\\) by <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = getParseData(content);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {
			TaskCondition condition = new TaskCondition()
					.setActionSet(Arrays.asList(
							TaskAction.VARIATION,
							TaskAction.VARY_STRONG,
							TaskAction.VARY_SUBTLE))
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
