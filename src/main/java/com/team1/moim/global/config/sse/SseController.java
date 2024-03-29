package com.team1.moim.global.config.sse;

import com.team1.moim.domain.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class SseController {

    private final SseService sseService;
    private final MemberRepository memberRepository;

    @Autowired
    public SseController(SseService sseService, MemberRepository memberRepository) {
        this.sseService = sseService;
        this.memberRepository = memberRepository;
    }

//    SSE 연결
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() throws ServiceUnavailableException {
        SseEmitter emitter = new SseEmitter(3600*1000L);// 만료시간 설정 30초
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(email);
        sseService.add(email, emitter);
        return ResponseEntity.ok(emitter);

        // 2. 캐싱 처리된 못 받은 알람 connection 완료시 밀어주기.
//        if(Boolean.TRUE.equals(redisTemplate2.hasKey(authentication.getName()))){
//            ListOperations<String,Object> valueOperations = redisTemplate2.opsForList();
//            //System.out.println(valueOperations.size(authentication.getName()));
//            Long size = valueOperations.size(authentication.getName());
//            List<FeedBackNotificationRes> feedBackNotificationResList = valueOperations.range(authentication.getName(),0,size-1)
//                    .stream()
//                    .map(a->(FeedBackNotificationRes)a)
//                    .collect(Collectors.toList());
//            UserIdPassword userIdPassword = userRepository.findByEmail(authentication.getName()).orElseThrow(
//                    ()-> new TheFitBizException(ErrorCode.NOT_FOUND_MEMBER));
//            if(userIdPassword.getRole().equals(Role.MEMBER)){
//                for(FeedBackNotificationRes feedBackNotificationRes : feedBackNotificationResList) {
//                    sendLastInfoToMember(authentication.getName(), feedBackNotificationRes);
//                }
//            }else if(userIdPassword.getRole().equals(Role.TRAINER)){
//                for(FeedBackNotificationRes feedBackNotificationRes : feedBackNotificationResList) {
//                    sendLastInfoToTrainer(authentication.getName(), feedBackNotificationRes);
//                }
//            }
//            redisTemplate2.delete(authentication.getName());
//        }
    }

//    public void sendToTrainer(String trainerEmail, String type,String uploadDate,String memberName) {
//        ChannelTopic channel = new ChannelTopic(trainerEmail);
//        String message = type+"_"+uploadDate+"_"+memberName+"_"+trainerEmail+"_sendToTrainer";
//        redisPublisher.publish(channel, message);
//    }


}
