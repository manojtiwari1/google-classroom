package com.classroom.googel.dtos;

import lombok.Data;

@Data
public class LmsMaterialItemDTO {

    private String type;    // DRIVE_FILE, LINK, YOUTUBE, DRIVE_FOLDER, FORM

    private String id;

    private String url;
}
