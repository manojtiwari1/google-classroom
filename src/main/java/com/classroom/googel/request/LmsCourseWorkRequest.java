package com.classroom.googel.request;

import com.classroom.googel.enums.CourseWorkState;
import com.classroom.googel.enums.CourseWorkType;
import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class LmsCourseWorkRequest {

    private Long id;

    @Max(value = 3000 , message = "Name cannot exceed 3000 characters.")
    private String name;

    @Max(value = 30000 , message = "Description cannot exceed 30000 characters.")
    private String description;

    private CourseWorkType workType;   // Could be enum

    private CourseWorkState state;      // Could be enum

    private Double maxPoints;

    private List<LmsCourseMaterialRequest> courseMaterials;

    private Date dueDate;

    private String formLink;

    private String iframeLink;

    private Integer sortingOrder;

    private String saveType;


}


