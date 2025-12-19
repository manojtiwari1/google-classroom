package com.classroom.googel.dtos;

import lombok.Data;

@Data
public class LmsCourseDTO {

    private String id;

    private String name;

    private String section;

    private String descriptionHeading;

    private String room;

    private String ownerId;

    private String creationTime;

    private String updateTime;

    private String enrollmentCode;

    private String courseState;

    private String alternateLink;

    private String teacherGroupEmail;

    private String courseGroupEmail;

    private Boolean guardiansEnabled;

    private String calendarId;

    // private TeacherFolder teacherFolder;

    // private GradebookSettings gradebookSettings;
}
