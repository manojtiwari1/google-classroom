package com.classroom.googel.request;


import com.classroom.googel.enums.CourseMaterialType;
import lombok.Data;

@Data
public class LmsCourseMaterialRequest {

    private Long id;

    private CourseMaterialType type;        // FILE, LINK etc (can be enum)

    private String title;

    private String thumbnailUrl;

    private String alternateLink;

    private Long courseWorkId;
}

