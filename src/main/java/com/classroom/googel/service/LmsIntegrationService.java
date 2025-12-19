package com.classroom.googel.service;

import com.classroom.googel.dtos.*;
import com.classroom.googel.request.LmsCourseRequest;

import java.util.List;

public interface LmsIntegrationService {

    LmsCoursesDTO getAllCourses(String pageToken, Integer pageSize);

    LmsCourseDTO createCourse(LmsCourseDTO dto);

    LmsTopicDTO createTopic(LmsTopicDTO request);

    CourseWorkDTO createCourseWork(CourseWorkDTO request);

    List<LmsCourseWorkDTO> getCourseWork(String courseId, String pageToken, Integer pageSize);

    String createFullCourse(LmsCourseRequest request);
}
