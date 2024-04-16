package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.RepeatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepeatRepository extends JpaRepository<RepeatEvent, Long> {
    RepeatEvent findByEventId(Long eventId);
}
