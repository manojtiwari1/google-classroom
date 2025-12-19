package com.classroom.googel.dtos;

import lombok.Data;

import java.util.List;

@Data
public class LmsCourseWorkDTO {


    private String id;
    private String courseId;
    private String title;
    private String description;
    private String state;
    private String workType;       // ASSIGNMENT, MULTIPLE_CHOICE_QUESTION, etc.
    private String topicId;

    private String creationTime;
    private String updateTime;

    private String dueDate;        // optional
    private String dueTime;        // optional

    private List<LmsMaterialItemDTO> materials;
}
