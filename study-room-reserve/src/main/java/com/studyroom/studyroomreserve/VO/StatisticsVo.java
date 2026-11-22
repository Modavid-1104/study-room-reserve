package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class StatisticsVo {
    private long totalMinutes;     // 累计学习分钟数
    private long todayMinutes;     // 今日学习分钟数
    private int checkInCount;      // 打卡次数（已完成的预约数）
    private int continuousDays;    // 连续学习天数
}
