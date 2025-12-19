package com.classroom.googel.dtos;

import lombok.Data;

import java.util.List;

@Data
public class LmsCoursesDTO {

    private List<LmsCourseDTO> courses;

    private String nextPageToken;

}
