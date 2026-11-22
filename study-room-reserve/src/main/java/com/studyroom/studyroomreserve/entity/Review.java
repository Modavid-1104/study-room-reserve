package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

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
}
