package com.team1.moim.domain.group.repository;

import com.team1.moim.domain.group.entity.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByIsConfirmedAndIsDeletedAndId(String isConfirmed, String isDeleted, Long id);
}
