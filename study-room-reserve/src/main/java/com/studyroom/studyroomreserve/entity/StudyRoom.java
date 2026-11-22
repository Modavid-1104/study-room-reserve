package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyRoom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String cover;
    private String description;
    private Integer totalSeat;
    private String openTime;
    private String closeTime;
    private Double latitude;     // 纬度
    private Double longitude;    // 经度
    private Integer radius;      // 签到半径(米)

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}

