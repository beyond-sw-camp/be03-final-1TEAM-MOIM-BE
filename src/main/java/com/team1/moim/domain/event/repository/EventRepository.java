package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDeleteYnAndAlarmYn(String deleteYn, String alarmYn);
    List<Event> findByRepeatParent(Long repeatParent);
}