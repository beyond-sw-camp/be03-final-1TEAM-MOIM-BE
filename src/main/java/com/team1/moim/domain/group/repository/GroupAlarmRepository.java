package com.team1.moim.domain.group.repository;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupAlarmType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupAlarmRepository extends JpaRepository<GroupAlarm, Long> {
    List<GroupAlarm> findByGroupAlarmTypeAndSendYn(GroupAlarmType groupAlarmType, String sendYn);
    List<GroupAlarm> findByGroupAndGroupAlarmTypeAndSendYn(Group group,
                                                           GroupAlarmType groupAlarmType,String sendYn);
}
