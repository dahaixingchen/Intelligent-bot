package com.intelligent.bot.utils.sys;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.intelligent.bot.constant.CommonConst;
import com.intelligent.bot.model.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * ossClient
 */
@Slf4j
@Component
public class OssUtil {

    public static String upload(String file,String fileName) {
        if(!fileName.contains("jpg")){
            fileName = fileName + ".jpg";
        }
        SysConfig sysConfig = RedisUtil.getCacheObject(CommonConst.SYS_CONFIG);
        OSS ossClient = new OSSClientBuilder().build(sysConfig.getEndpoint(), sysConfig.getAccessKeyId(), sysConfig.getAccessKeySecret());
        ossClient.putObject(sysConfig.getBucketName(), new String(fileName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8), new ByteArrayInputStream(Base64.getDecoder().decode(file)));
        ossClient.shutdown();
        return "https://"+sysConfig.getBucketName()+"."+sysConfig.getEndpoint()+"/"+fileName;
    }
    public static String upload(MultipartFile file,String fileName) throws IOException {
        return upload(FileUtil.multipartFileToBase64(file),fileName);
    }

}
