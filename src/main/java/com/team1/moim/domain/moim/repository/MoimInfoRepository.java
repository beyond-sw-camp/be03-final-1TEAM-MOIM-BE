package com.team1.moim.domain.moim.repository;

import com.team1.moim.domain.moim.entity.MoimInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoimInfoRepository extends JpaRepository<MoimInfo, Long> {
}
