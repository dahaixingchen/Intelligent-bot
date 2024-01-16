package com.intelligent.bot.service.mj;


import com.intelligent.bot.base.result.B;
import com.intelligent.bot.enums.mj.BlendDimensions;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface DiscordService {

	B<Void> imagine(String prompt, String nonce);

	B<Void> upscale(String messageId, int index, String messageHash, int messageFlags, String nonce);

	B<Void> variation(String messageId, int index, String messageHash, int messageFlags, String nonce);

	B<Void> submitVary(String vary,String messageId, String messageHash, int messageFlags, String nonce);

	B<Void> submitZoomOut(Integer zoomOut,String messageId, String messageHash, int messageFlags, String nonce);

	B<Void> submitPan(String plan,String messageId, String messageHash, int messageFlags, String nonce);

	B<Void> reroll(String messageId, String messageHash, int messageFlags, String nonce);

	B<Void> describe(String finalFileName, String nonce);

	B<Void> shorten(String prompt,String nonce,Long id);

	B<Void> blend(List<String> finalFileNames, BlendDimensions dimensions, String nonce);

	B<String> upload(String fileName, DataUrl dataUrl);

	B<String> sendImageMessage(String content, String finalFileName);

}
