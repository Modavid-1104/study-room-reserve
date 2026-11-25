package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private Integer type;        // 1 图片 2 视频
    private String url;
    private LocalDateTime createTime;
    @TableLogic
    private Integer isDeleted;
}
