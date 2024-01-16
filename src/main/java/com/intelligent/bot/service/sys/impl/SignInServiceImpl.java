package com.intelligent.bot.service.sys.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.bot.base.exception.E;
import com.intelligent.bot.base.result.B;
import com.intelligent.bot.dao.OrderDao;
import com.intelligent.bot.dao.SignInDao;
import com.intelligent.bot.dao.UserDao;
import com.intelligent.bot.model.SignIn;
import com.intelligent.bot.model.User;
import com.intelligent.bot.model.base.BaseDeleteEntity;
import com.intelligent.bot.model.req.sys.admin.UserAddReq;
import com.intelligent.bot.model.req.sys.admin.UserQueryPageReq;
import com.intelligent.bot.model.req.sys.admin.UserUpdateReq;
import com.intelligent.bot.model.res.sys.admin.AdminHomeOrder;
import com.intelligent.bot.model.res.sys.admin.AdminHomeOrderPrice;
import com.intelligent.bot.model.res.sys.admin.AdminHomeRes;
import com.intelligent.bot.model.res.sys.admin.UserQueryPageRes;
import com.intelligent.bot.service.sys.ISignInService;
import com.intelligent.bot.service.sys.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service("signInService")
@Transactional(rollbackFor = Exception.class)
public class SignInServiceImpl extends ServiceImpl<SignInDao, SignIn> implements ISignInService {

    @Override
    public Page<SignIn> querySignIn(Page<SignIn> page, Long userId) {
        return this.baseMapper.querySignIn(page,userId);
    }
}
