package com.classroom.googel.repository;

import com.classroom.googel.entity.GoogleClassroomCred;
import com.classroom.googel.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleClassroomCredRepository extends
        JpaRepository<GoogleClassroomCred, Integer>, JpaSpecificationExecutor<GoogleClassroomCred> {


    Optional<GoogleClassroomCred> findByState(String state);

    Optional<GoogleClassroomCred> findByStatus(Status status);
}
