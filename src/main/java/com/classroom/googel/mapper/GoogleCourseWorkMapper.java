package com.classroom.googel.mapper;

import com.classroom.googel.dtos.LmsCourseWorkDTO;
import com.google.api.services.classroom.model.CourseWork;

public class GoogleCourseWorkMapper {

    public static LmsCourseWorkDTO fromCourseWork(CourseWork cw) {

        LmsCourseWorkDTO dto = new LmsCourseWorkDTO();

        dto.setId(cw.getId());
        dto.setCourseId(cw.getCourseId());
        dto.setTitle(cw.getTitle());
        dto.setDescription(cw.getDescription());
        dto.setWorkType(cw.getWorkType());
        dto.setState(cw.getState());
        dto.setTopicId(cw.getTopicId());

        if (cw.getDueDate() != null) {
            dto.setDueDate(
                    cw.getDueDate().getYear() + "-" +
                            cw.getDueDate().getMonth() + "-" +
                            cw.getDueDate().getDay()
            );
        }

        if (cw.getDueTime() != null) {
            dto.setDueTime(
                    cw.getDueTime().getHours() + ":" +
                            cw.getDueTime().getMinutes()
            );
        }

        dto.setCreationTime(cw.getCreationTime());
        dto.setUpdateTime(cw.getUpdateTime());

        return dto;
    }


}

