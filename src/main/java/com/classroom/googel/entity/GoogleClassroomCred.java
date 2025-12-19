package com.classroom.googel.entity;

import com.classroom.googel.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(name = "classroom_credentials")
@Builder
@Data
@AllArgsConstructor
public class GoogleClassroomCred {

    @Id
    private String id;


    private String state;

    private String email;

    private String name;

    private String googleId;

    private String code;

//  @Convert(converter = GoogleTokenResponseConverter.class)

//    @ManyToOne
//    private User user;

//    @ManyToOne
//    private UserOrganization userOrganization;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime lastRefreshedAt;


    public GoogleClassroomCred() {

    }
}
