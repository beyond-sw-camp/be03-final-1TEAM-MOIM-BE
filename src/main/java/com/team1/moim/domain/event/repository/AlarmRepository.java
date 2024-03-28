package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.ToDoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
