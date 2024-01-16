package com.intelligent.bot.service.mj;


import com.intelligent.bot.base.result.B;
import com.intelligent.bot.enums.mj.BlendDimensions;
import com.intelligent.bot.model.Task;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface TaskService {

	B<Void> submitImagine(Task task, List<String> imgList);

	B<Void> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index,  int messageFlags);

	B<Void> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index,  int messageFlags);
	B<Void> submitVary(String vary,Task task, String targetMessageId, String targetMessageHash,  int messageFlags);
	B<Void> submitZoomOut(Integer zoomOut,Task task, String targetMessageId, String targetMessageHash,  int messageFlags);
	B<Void> submitPan(String plan,Task task, String targetMessageId, String targetMessageHash,  int messageFlags);

	B<Void> submitReroll(Task task, String targetMessageId, String targetMessageHash, int messageFlags);

	B<Void> submitDescribe(Task task, DataUrl dataUrl);

	B<Void> submitShorten(Task task);

	B<Void> submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions);
}