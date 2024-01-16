package com.intelligent.bot.model.req.sys.admin;

import lombok.Data;

import java.io.Serializable;


@Data
public class SysConfigUpdateReq implements Serializable {

    private static final long serialVersionUID = 326308725675949330L;

    /**
     * 注册模式  1 账号密码 2 邮箱注册 3 公众号
     */
    private Integer registrationMethod;

    /**
     * 签到赠送次数
     */
    private Integer signInNumber;

    /**
     * 默认注册次数
     */
    private Integer defaultTimes;

    /**
     * gpt请求地址
     */
    private String gptUrl;

    /**
     * gpt4请求地址
     */
    private String gpt4Url;

    /**
     * gpt开关 0-未开启、1-开启gpt3.5、2-开启gpt4.0、3-全部
     */
    private Integer isOpenGpt;

    /**
     * gpt画图开关 0-未开启、1-开启
     */
    private Integer isOpenGptOfficial;


    /**
     * 图片上传路径
     */
    private String imgUploadUrl;

    /**
     * 图片返回前缀地址
     */
    private String imgReturnUrl;

    /**
     * 后台接口地址
     */
    private String apiUrl;

    /**
     * 客户端页面地址
     */
    private String clientUrl;

    /**
     * sd接口地址
     */
    private String sdUrl;

    /**
     * 是否开启sd 0未开启 1开启
     */
    private Integer isOpenSd;

    /**
     * 是否开启FlagStudio 0-未开启 1开启
     */
    private Integer isOpenFlagStudio;

    /**
     * FlagStudio key
     */
    private String flagStudioKey;

    /**
     * FlagStudio接口地址
     */
    private String flagStudioUrl;

    /**
     * 百度appid
     */
    private String baiduAppid;

    /**
     * 百度Secret
     */
    private String baiduSecret;

    /**
     * discord服务器id
     */
    private String mjGuildId;

    /**
     * discord频道id
     */
    private String mjChannelId;

    /**
     * discord用户Token
     */
    private String mjUserToken;

    /**
     * 机器人token
     */
    private String mjBotToken;

    /**
     * 机器人名称
     */
    private String mjBotName;

    /**
     * 百度应用key
     */
    private String baiduKey;

    /**
     * 百度应用Secret
     */
    private String baiduSecretKey;

    /**
     * 是否开启mj 0未开启 1开启
     */
    private Integer isOpenMj;

    /**
     * 是否开启代理 0未开启 1开启
     */
    private Integer isOpenProxy;

    /**
     * 代理ip
     */
    private String proxyIp;

    /**
     * 代理端口
     */
    private Integer proxyPort;

    /**
     * id
     */
    private Long id;

    /**
     *  客户端 logo 地址
     */
    private String clientLogo;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 是否开启阿里oss存储 0-未开启、1-开启
     */
    private Integer isOpenOss;

    /**
     * Endpoint（oss地域节点）
     */
    private String endpoint;

    /**
     * Bucket 名称
     */
    private String bucketName;

    /**
     * 阿里accessKeyId
     */
    private String accessKeyId;

    /**
     * accessKeySecret
     */
    private String accessKeySecret;

    /**
     * AI会话默认角色
     */
    private String defaultRole;

    /**
     * 星火模型开关 0 -未开启 、1-开启
     */
    private Integer isOpenSpark;

    /**
     * 星火APPID
     */
    private String sparkAppId;

    /**
     * 星火APIKey
     */
    private String sparkApiKey;

    /**
     * 星火APISecret
     */
    private String sparkApiSecret;
}
