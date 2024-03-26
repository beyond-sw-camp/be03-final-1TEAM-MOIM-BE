package com.team1.moim.domain.moim.repository;

import com.team1.moim.domain.moim.entity.Moim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoimRepository extends JpaRepository<Moim, Long> {
    Moim findByIsConfirmedAndIsDeletedAndId(String isConfirmed, String isDeleted, Long id);
}
