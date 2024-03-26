package com.team1.moim.domain.event.service;

import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import com.team1.moim.domain.event.entity.ToDoList;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.event.repository.ToDoListRepository;
import com.team1.moim.global.config.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private static final String FILE_TYPE = "events";

    private final EventRepository eventRepository;
    private final ToDoListRepository toDoListRepository;
    private final S3Service s3Service;

    public EventResponse create(EventRequest request, List<ToDoListRequest> toDoListRequests) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow();
        Matrix matrix;
        if(request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if(request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if(request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        String fileUrl = null;
        if (request.getFile() != null){
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }
        Event event = EventRequest.toEntity(request.getTitle(), request.getMemo(), request.getStartDate(), request.getEndDate(), request.getPlace(), matrix, fileUrl);
        eventRepository.save(event);
//        ToDoList 추가
        if(toDoListRequests != null) {
            for(ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity(toDoListRequest.getContents(), toDoListRequest.getIsChecked(), event);
                toDoListRepository.save(toDoList);
            }
        }
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse update(Long eventId, EventRequest request) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();
//        if(member.getId() != event.getMember().getId()) {
//            throw new AccessDeniedException("작성한 회원이 아닙니다.");
//        }
        String fileUrl = null;
        if (request.getFile() != null){
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }
        Matrix matrix;
        if(request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if(request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if(request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        event.update(request.getTitle(), request.getMemo(), request.getStartDate(), request.getEndDate(), request.getPlace(), matrix, fileUrl);
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.delete();
    }
}
