package com.intelligent.bot.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.intelligent.bot.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sign_in")
public class SignIn extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 326308725675949330L;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 获得次数
     */
    private Integer number;
    /**
     * 签到日期
     */
    private LocalDate signInDate;


}
