package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDeleteYnAndAlarmYn(String deleteYn, String alarmYn);
    List<Event> findByRepeatParent(Long repeatParent);
    List<Event> findByMember(Member member);

    @Query("SELECT e FROM Event e WHERE e.member = :member AND FUNCTION('YEAR', e.startDateTime) = :year AND FUNCTION('MONTH', e.startDateTime) = :month")
    List<Event> findByMemberAndYearAndMonty(@Param("member") Member member, @Param("year") int year, @Param("month") int month);

}