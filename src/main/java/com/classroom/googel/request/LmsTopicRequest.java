package com.classroom.googel.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class LmsTopicRequest {

    private Long id;

    @NotNull(message = "Topic name cannot be null")
    private String topicName;

    private Integer sortingOrder;

    private boolean active;

    private List<LmsCourseWorkRequest> courseWorks;
}
