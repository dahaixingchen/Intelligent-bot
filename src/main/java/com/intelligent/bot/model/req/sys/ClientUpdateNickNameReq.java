package com.intelligent.bot.model.req.sys;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ClientUpdateNickNameReq {

    /**
     * 昵称
     */
    @NotNull(message = "昵称不能为空")
    private String nickName;
}
