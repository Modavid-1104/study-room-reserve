package com.studyroom.studyroomreserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.studyroomreserve.VO.ReservationListVo;
import com.studyroom.studyroomreserve.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    @Select("SELECT r.id, r.seat_id, s.seat_no, sr.name AS room_name, " +
            "r.start_time, r.end_time, r.status, r.sign_in_time, r.create_time " +
            "FROM reservation r " +
            "LEFT JOIN seat s ON r.seat_id = s.id " +
            "LEFT JOIN study_room sr ON s.room_id = sr.id " +
            "WHERE r.user_id = #{userId} AND r.is_deleted = 0 " +
            "ORDER BY r.create_time DESC")
    List<ReservationListVo> selectMyList(@Param("userId") Long userId);
}
