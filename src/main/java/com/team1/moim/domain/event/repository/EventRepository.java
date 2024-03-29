package com.team1.moim.domain.event.repository;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Repeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByRepeatParent(Long repeatParent);
}
