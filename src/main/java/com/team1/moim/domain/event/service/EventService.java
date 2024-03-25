package com.team1.moim.domain.event.service;

import com.team1.moim.domain.event.dto.request.CreateEventRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.repository.EventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Value("${image.path")
    private String filePath;

    public EventResponse create(CreateEventRequest createEventRequest) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow();
        Path path = null;
        if (createEventRequest.getFile() != null){
            MultipartFile file = createEventRequest.getFile();
            String fileName = file.getOriginalFilename();
            path = Paths.get(filePath, fileName);
            try{
                byte[] bytes = file.getBytes();
                Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }catch (IOException e) {
                throw new IllegalArgumentException("File Not Available");
            }
//            imagePath = s3Service.uploadFile(FILE_TYPE, createEventRequest.getFile());

        }
        Event event = CreateEventRequest.toEntity(createEventRequest.getTitle(), createEventRequest.getMemo(), createEventRequest.getStartDate(), createEventRequest.getEndDate(), createEventRequest.getPlace(), createEventRequest.getMatrix(), path);
        return EventResponse.from(eventRepository.save(event));

    }


}
