package com.classroom.googel.dtos;

import com.classroom.googel.enums.CourseWorkState;
import com.classroom.googel.enums.CourseWorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class CourseWorkDTO implements Serializable {

    private Long id;

    @Size(min = 2, max = 100, message = "Length should be between 2 to 100")
    @NotBlank(message = "Title should not be blank")
    private String title;

    private String description;

    private CourseWorkState state;

    private Date dueDate;

    private String scheduledTime;

    private Integer maxPoints;

    private CourseWorkType workType;

//    private AssigneeMode assigneeMode;

    private Long creatorId;

//    private SubmissionModificationMode submissionModificationMode;

//    private AssignmentDTO assignment;

    private Long topicId;

//    private Set<StudentDTO> individualStudentsOptions = new HashSet<>();

//    private Set<CourseMaterialDTO> courseMaterials = new HashSet<>();

    private Long courseId;

    private Date createdOn;

    private String saveType;

    private String link;

    private String iframeLink;

    private Boolean teacherMaterial;

    private Boolean isActive;

    private Integer commentCount;
}
