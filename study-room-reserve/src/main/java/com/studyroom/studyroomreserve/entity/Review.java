package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reservationId;
    private Long roomId;
    private Long userId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;

    // 媒体列表：不是数据库字段，仅用于接口返回时携带图片/视频
    @TableField(exist = false)
    private List<ReviewMedia> mediaList;
}
