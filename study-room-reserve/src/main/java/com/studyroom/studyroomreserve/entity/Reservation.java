package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime signInTime;
    private LocalDateTime signOutTime;   // 新增
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
    private String signImage;
}

