package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Repeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepeatRepository extends JpaRepository<Repeat, Long> {
    Repeat findByEventId(Long eventId);
}
