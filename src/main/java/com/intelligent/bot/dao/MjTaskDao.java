package com.intelligent.bot.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.bot.model.Task;
import com.intelligent.bot.model.res.sys.GalleryRes;
import com.intelligent.bot.model.res.sys.MjTaskRes;
import com.intelligent.bot.model.res.sys.MjTaskTransformRes;
import com.intelligent.bot.model.res.sys.admin.GptKeyQueryRes;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MjTaskDao extends BaseMapper<Task> {


    List<MjTaskRes> selectUserMjTask(@Param("userId") Long userId);

    List<MjTaskTransformRes> selectTransform();

    int batchDeleteByUserId(@Param("userId") Long userId);

    int deleteByKeyId(@Param("id") Long id);

    Page<GalleryRes> getGallery(Page<GalleryRes> page);
}
