package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.ToDoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByEventAndSendYn(Event event, String sendYn);
}
