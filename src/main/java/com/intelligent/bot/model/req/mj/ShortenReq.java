package com.intelligent.bot.model.req.mj;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 提示词分析提交
 */
@Data
public class ShortenReq {

	@NotNull
	private String prompt;


}
