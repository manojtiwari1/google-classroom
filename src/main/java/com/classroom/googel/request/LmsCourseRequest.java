package com.classroom.googel.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LmsCourseRequest {

    @NotNull(message = "Course name cannot be null")
    private String courseName;

    private List<LmsTopicRequest> topicList;
}
