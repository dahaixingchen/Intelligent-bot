package com.intelligent.bot.api.spark;

import com.alibaba.fastjson.JSONObject;
import com.intelligent.bot.base.exception.E;
import com.intelligent.bot.base.result.B;
import com.intelligent.bot.constant.CommonConst;
import com.intelligent.bot.enums.sys.SendType;
import com.intelligent.bot.listener.spark.ChatListener;
import com.intelligent.bot.listener.spark.SparkDeskClient;
import com.intelligent.bot.model.MessageLog;
import com.intelligent.bot.model.SysConfig;
import com.intelligent.bot.model.Task;
import com.intelligent.bot.model.req.mj.SubmitReq;
import com.intelligent.bot.model.req.spark.ChatReq;
import com.intelligent.bot.model.spark.*;
import com.intelligent.bot.service.baidu.BaiDuService;
import com.intelligent.bot.service.spark.ISparkService;
import com.intelligent.bot.service.sys.CheckService;
import com.intelligent.bot.service.sys.IMessageLogService;
import com.intelligent.bot.utils.sys.JwtUtil;
import com.intelligent.bot.utils.sys.RedisUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/spark")
@Log4j2
public class SparkController {

    @Resource
    ISparkService sparkService;

    @PostMapping(value = "/chat",name = "星火大模型提交")
    public B<Long> chat(@RequestBody ChatReq req) {
        return B.okBuild(sparkService.chat(req));
    }
}
