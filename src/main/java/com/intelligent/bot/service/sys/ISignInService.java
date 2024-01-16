package com.intelligent.bot.service.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.bot.model.SignIn;
import com.intelligent.bot.model.WxLog;


public interface ISignInService extends IService<SignIn> {

    Page<SignIn> querySignIn(Page<SignIn> page,Long userId);

}
