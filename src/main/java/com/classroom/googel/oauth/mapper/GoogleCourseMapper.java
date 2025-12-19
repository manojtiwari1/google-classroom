package com.classroom.googel.oauth.mapper;


import com.classroom.googel.dtos.LmsCourseDTO;
import com.google.api.services.classroom.model.Course;

public class GoogleCourseMapper {

    public static LmsCourseDTO toDto(Course course) {
        if (course == null) return null;

        LmsCourseDTO dto = new LmsCourseDTO();

        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setSection(course.getSection());
        dto.setDescriptionHeading(course.getDescriptionHeading());
        dto.setRoom(course.getRoom());
        dto.setOwnerId(course.getOwnerId());
        dto.setCreationTime(course.getCreationTime());
        dto.setUpdateTime(course.getUpdateTime());
        dto.setEnrollmentCode(course.getEnrollmentCode());
        dto.setCourseState(course.getCourseState());
        dto.setAlternateLink(course.getAlternateLink());
        dto.setTeacherGroupEmail(course.getTeacherGroupEmail());
        dto.setCourseGroupEmail(course.getCourseGroupEmail());
        dto.setGuardiansEnabled(course.getGuardiansEnabled());
        dto.setCalendarId(course.getCalendarId());

        return dto;
    }
}
