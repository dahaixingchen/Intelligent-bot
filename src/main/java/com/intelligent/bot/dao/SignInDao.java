package com.intelligent.bot.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.bot.model.CardPin;
import com.intelligent.bot.model.SignIn;
import com.intelligent.bot.model.res.sys.admin.CardPinQueryRes;
import org.apache.ibatis.annotations.Param;

public interface SignInDao extends BaseMapper<SignIn> {

    Page<SignIn> querySignIn(Page<SignIn> page, @Param("userId") Long userId);

}
