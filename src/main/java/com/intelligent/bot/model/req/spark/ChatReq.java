package com.intelligent.bot.model.req.spark;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class ChatReq {

	/**
	 * 内容
	 */
	private String problem;

	/**
	 * 日志id
	 */
	private Long logId;


	/**
	 * 模型类型2/3
	 */
	private Integer type = 2;
}
