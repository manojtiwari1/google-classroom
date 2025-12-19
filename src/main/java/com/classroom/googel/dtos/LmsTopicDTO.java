package com.classroom.googel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class LmsTopicDTO {

    @NotNull(message = "Course ID cannot be null")
    private String courseId;

    private String topicId;

    @NotNull(message = "Topic name cannot be null")
    @NotBlank(message = "Topic name cannot be blank")
    private String name;

    private String updateTime;

    private Integer position; // for canvas ordering
}
