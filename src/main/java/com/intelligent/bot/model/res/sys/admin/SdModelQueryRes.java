package com.intelligent.bot.model.res.sys.admin;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SdModelQueryRes {



    /**
     * 模型名
     */
    private String modelName;

    /**
     * 模型图片地址
     */
    private String imgUrl;

    /**
     * 图片返回前缀地址
     */
    private String imgReturnUrl;

    /**
     * id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}


