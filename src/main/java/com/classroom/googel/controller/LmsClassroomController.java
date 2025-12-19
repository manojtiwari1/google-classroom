package com.classroom.googel.controller;


import com.classroom.googel.dtos.*;
import com.classroom.googel.enums.OAuthProvider;
import com.classroom.googel.factory.LmsCourseFactory;
import com.classroom.googel.request.LmsCourseRequest;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/auth/organization/{provider}")
@RequiredArgsConstructor
@Slf4j
public class LmsClassroomController {


    private final LmsCourseFactory lmsCourseFactory;


    @GetMapping("/get-courses")
    public ResponseEntity<?> getGoogleCourses(
            @PathVariable OAuthProvider provider,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) Integer pageSize) {
        LmsCoursesDTO coursesDTO= lmsCourseFactory.getService(provider).getAllCourses(pageToken, pageSize);
        return ResponseEntity.ok(coursesDTO);
    }


    @PostMapping("/create-course")
    public ResponseEntity<?> createCourse(
            @PathVariable OAuthProvider provider,
            @RequestBody LmsCourseDTO request
    ) {

        LmsCourseDTO lmsCourseDTO=lmsCourseFactory.getService(provider).createCourse(request);
        return ResponseEntity.ok(lmsCourseDTO);
    }


    @PostMapping("/create-topic")
    public ResponseEntity<?> createTopic(
            @PathVariable OAuthProvider provider,
            @Valid @RequestBody LmsTopicDTO request) {
        LmsTopicDTO lmsTopicDTO = lmsCourseFactory.getService(provider).createTopic(request);
        return ResponseEntity.ok(lmsTopicDTO);

    }


    @PostMapping("/create-assignment")
    public ResponseEntity<CourseWorkDTO> createAssignment(
            @PathVariable OAuthProvider provider,
            @RequestBody CourseWorkDTO request) {
        CourseWorkDTO lmsCourseWork = lmsCourseFactory.getService(provider).createCourseWork(request);
        return ResponseEntity.ok(lmsCourseWork);

    }


    @GetMapping("/{courseId}/coursework")
    public ResponseEntity<?> getCourseWork(
            @PathVariable OAuthProvider provider,
            @PathVariable String courseId,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) Integer pageSize
    ) {
        List<LmsCourseWorkDTO> lmsCourseWorkDTOList =lmsCourseFactory.getService(provider)
                .getCourseWork(courseId, pageToken, pageSize);
        return ResponseEntity.ok(lmsCourseWorkDTOList);
    }


    @PostMapping("/create-full-course")
    public ResponseEntity<?> createFullCourse(
            @PathVariable OAuthProvider provider,
            @RequestBody @Valid LmsCourseRequest request
    ){
//        log.info("Create Course provider={}, request={}", provider, request);

        String courseMessage = lmsCourseFactory.getService(provider)
                .createFullCourse(request);
        return ResponseEntity.ok(courseMessage);

    }


}
