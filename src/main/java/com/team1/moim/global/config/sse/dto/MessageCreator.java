package com.team1.moim.global.config.sse.dto;

import static com.team1.moim.domain.group.entity.GroupAlarmType.MOIM_ALL_PARTICIPATED;
import static com.team1.moim.domain.group.entity.GroupAlarmType.MOIM_CREATED;
import static com.team1.moim.domain.group.entity.GroupAlarmType.MOIM_DEADLINE;
import static com.team1.moim.domain.group.entity.GroupAlarmType.MOIM_TIME_CONFIRMED;

import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.global.utils.DateTimeConverter;
import java.util.StringJoiner;

public class MessageCreator {
    public static String createMessage(GroupAlarm groupAlarm) {
        String hostName = groupAlarm.getGroup().getMember().getNickname();
        String title = groupAlarm.getGroup().getTitle();
        String message = null;

        if (groupAlarm.getGroupAlarmType() == MOIM_CREATED) {
            StringJoiner joiner = new StringJoiner(" ");
            joiner.add(hostName);
            joiner.add("님께서");
            joiner.add(title);
            joiner.add("에 초대했습니다.");
            message = joiner.toString();
        } else if (groupAlarm.getGroupAlarmType() == MOIM_ALL_PARTICIPATED) {
            message = "초대한 모든 사용자가 참여를 완료했습니다.";
        } else if (groupAlarm.getGroupAlarmType() == MOIM_TIME_CONFIRMED) {
            String formattedDateTime = DateTimeConverter.localDateTimeToString(
                    groupAlarm.getGroup().getConfirmedDate());

            StringJoiner joiner = new StringJoiner(" ");
            joiner.add("모임 시간이 확정되었습니다.")
                    .add("모임 시간은")
                    .add(formattedDateTime)
                    .add("입니다.");
            message = joiner.toString();
        } else if (groupAlarm.getGroupAlarmType() == MOIM_DEADLINE) {
            StringJoiner joiner = new StringJoiner(" ");
            joiner.add(title)
                    .add("모임 참여 결정까지")
                    .add(String.valueOf(groupAlarm.getDeadlineAlarm()))
                    .add(groupAlarm.getGroupAlarmTimeType().name())
                    .add("남았습니다.");
            message = joiner.toString();
        }

        return message;
    }
}
