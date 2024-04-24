package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDeleteYnAndAlarmYn(String deleteYn, String alarmYn);
    List<Event> findByRepeatParentAndDeleteYn(Long repeatParent, String deleteYn);
    List<Event> findByMember(Member member);
    List<Event> findByMemberAndDeleteYn(Member member, String deleteYn);

    @Query("SELECT e FROM Event e WHERE e.member = :member AND e.deleteYn = 'N' AND (e.startDateTime <= :end AND e.endDateTime >= :start)")
    List<Event> findByMemberAndDateRange(@Param("member") Member member, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Event e WHERE e.member = :member AND FUNCTION('YEAR', e.startDateTime) = :year AND FUNCTION('WEEK', e.startDateTime) = :week")
    List<Event> findByMemberAndYearAndWeek(@Param("member") Member member, @Param("year") int year, @Param("week") int week);

    @Query("SELECT e FROM Event e WHERE e.member = :member AND FUNCTION('YEAR', e.startDateTime) = :year AND FUNCTION('MONTH', e.startDateTime) = :month AND FUNCTION('DAY', e.startDateTime) = :day")
    List<Event> findByMemberAndYearAndMonthAndDay(@Param("member") Member member, @Param("year") int year, @Param("month") int month, @Param("day") int day);

    @Query("SELECT e FROM Event e WHERE e.member = :member AND e.deleteYn = 'N' AND (e.title LIKE %:content% OR e.memo LIKE %:content%)")
    List<Event> findByMemberAndTitleOrMemo(@Param("member") Member member, @Param("content") String content);


}