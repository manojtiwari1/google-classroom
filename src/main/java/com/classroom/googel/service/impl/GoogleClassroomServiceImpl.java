package com.classroom.googel.service.impl;

import com.classroom.googel.dtos.*;
import com.classroom.googel.entity.GoogleClassroomCred;
import com.classroom.googel.enums.CourseMaterialType;
import com.classroom.googel.enums.CourseWorkType;
import com.classroom.googel.enums.Status;
import com.classroom.googel.mapper.GoogleCourseWorkMapper;
import com.classroom.googel.oauth.mapper.GoogleCourseMapper;
import com.classroom.googel.repository.GoogleClassroomCredRepository;
import com.classroom.googel.request.LmsCourseMaterialRequest;
import com.classroom.googel.request.LmsCourseRequest;
import com.classroom.googel.request.LmsCourseWorkRequest;
import com.classroom.googel.request.LmsTopicRequest;
import com.classroom.googel.service.LmsIntegrationService;
import com.classroom.googel.utils.GoogleClassroomClient;
import com.classroom.googel.utils.GoogleClassroomManager;
import com.classroom.googel.utils.GoogleDriveUploader;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.model.*;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manoj Tiwari
 * @version 1.0
 * @created 01/12/2025
 * @lastUpdated 01/12/2025
 * @description DTO representing the Google Classroom Course response structure
 * returned from the API: https://classroom.googleapis.com/v1/courses
 * @see <a href="https://developers.google.com/classroom/reference/rest/v1/courses">Google Classroom API Docs</a>
 * @since 01/12/2025
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleClassroomServiceImpl implements LmsIntegrationService {

    private final GoogleClassroomCredRepository googleClassroomCredRepository;

    private final GoogleClassroomManager googleClassroomManager;

    private final GoogleClassroomClient googleClassroomClient;

    private final GoogleDriveUploader googleDriveClient;

//    private final AwsS3Service awsS3Service;

    @Override
    public LmsCoursesDTO getAllCourses(String pageToken, Integer pageSize) {

        pageSize = pageSize == null ? 50 : pageSize;
        GoogleClassroomCred googleClassroomCred = getGoogleClassroomCred();

        try {
            String accessToken = getAccessToken(googleClassroomCred);
            Classroom classroom = googleClassroomClient.getClient(accessToken);
            ListCoursesResponse response = classroom.courses().list()
                    .setPageSize(pageSize)
                    .setPageToken(pageToken)
                    .setFields("courses(id,name,section,descriptionHeading,room,ownerId,creationTime,updateTime,enrollmentCode,courseState,alternateLink,teacherGroupEmail,courseGroupEmail,guardiansEnabled,calendarId),nextPageToken")
                    .execute();
            List<Course> googleCourses = response.getCourses();

            if (googleCourses == null || googleCourses.isEmpty()) {
                return new LmsCoursesDTO();
            }

            List<LmsCourseDTO> mappedCourses = googleCourses.stream()
                    .map(GoogleCourseMapper::toDto).collect(Collectors.toList());
            LmsCoursesDTO wrapper = new LmsCoursesDTO();

            wrapper.setCourses(mappedCourses);
            wrapper.setNextPageToken(response.getNextPageToken());
            return wrapper;

        } catch (Exception e) {
            log.error("Error while fetching course from Google Classroom", e);
        }
        return new LmsCoursesDTO();
    }

    @Override
    public LmsCourseDTO createCourse(LmsCourseDTO dto) {

        GoogleClassroomCred googleClassroomCred = getGoogleClassroomCred();
        try {
            String accessToken = getAccessToken(googleClassroomCred);
            Classroom classroom = googleClassroomClient.getClient(accessToken);

            Course created = createGoogleCourse(classroom, dto);
            return GoogleCourseMapper.toDto(created);

        } catch (Exception e) {
            log.error("Error while creating Google Classroom course", e);
//            throw new ApplicationException(ResponseCode.BAD_REQUEST, "Error while creating Google Classroom course");
            throw new RuntimeException("Error while creating Google Classroom course" + e.getMessage());
        }

    }

    @Override
    public LmsTopicDTO createTopic(LmsTopicDTO request) {
        GoogleClassroomCred googleClassroomCred = getGoogleClassroomCred();
        try {
            String accessToken = getAccessToken(googleClassroomCred);
            Classroom classroom = googleClassroomClient.getClient(accessToken);
            Topic topic = createGoogleTopic(classroom, request);
            return LmsTopicDTO.builder().
                    topicId(topic.getTopicId()).
                    name(topic.getName()).
                    courseId(topic.getCourseId()).
                    updateTime(topic.getUpdateTime()).
                    build();

        } catch (Exception e) {
            log.error("Error while creating Google Classroom topic", e);
//            throw new ApplicationException(ResponseCode.BAD_REQUEST, "Error while creating Google Classroom topic");
            throw new RuntimeException("Error while creating Google Classroom topic " + e.getMessage());
        }
    }

    @Override
    public CourseWorkDTO createCourseWork(CourseWorkDTO request) {
        GoogleClassroomCred googleClassroomCred = getGoogleClassroomCred();
        try {
            String accessToken = getAccessToken(googleClassroomCred);
            Classroom classroom = googleClassroomClient.getClient(accessToken);
            // Implementation for creating CourseWork goes here
            // Since the implementation details are not provided, returning null for now
            CourseWork courseWork = createGoogleCourseWork(classroom, request);

            return null;
        } catch (Exception e) {
            log.error("Error while creating Google Classroom course work", e);
//            throw new ApplicationException(ResponseCode.BAD_REQUEST, "Error while creating Google Classroom course work");
            throw new RuntimeException("Error while creating Google Classroom course work. " + e.getMessage());
        }
    }

    @Override
    public List<LmsCourseWorkDTO> getCourseWork(String courseId, String pageToken, Integer pageSize) {
        pageSize = pageSize == null ? 100 : pageSize;

        GoogleClassroomCred cred = getGoogleClassroomCred();

        try {
            String accessToken = getAccessToken(cred);
            Classroom classroom = googleClassroomClient.getClient(accessToken);

            List<LmsCourseWorkDTO> all = new ArrayList<>();

            // 1. Get Assignments / Quizzes (CourseWork)
            ListCourseWorkResponse cwResponse = classroom.courses().courseWork()
                    .list(courseId)
                    .setPageSize(pageSize)
                    .setPageToken(pageToken)
                    .setFields("courseWork(id,title,description,workType,state,topicId,creationTime,updateTime,dueDate,dueTime),nextPageToken")
                    .execute();

            if (cwResponse.getCourseWork() != null) {
                for (CourseWork cw : cwResponse.getCourseWork()) {
                    all.add(GoogleCourseWorkMapper.fromCourseWork(cw));
                }
            }


            return all;
        } catch (Exception e) {
            log.error("Error while fetching CourseWork from Google Classroom", e);
            throw new RuntimeException("Cannot fetch coursework: " + e.getMessage());
        }
    }

    @Override
    public String createFullCourse(LmsCourseRequest request) {

        GoogleClassroomCred googleClassroomCred = getGoogleClassroomCred();

        try {
            String accessToken = getAccessToken(googleClassroomCred);
            Drive driveService = googleDriveClient.getDriveClient(accessToken);
            Classroom classroom = googleClassroomClient.getClient(accessToken);
            // 1. Create Course
            LmsCourseDTO courseDTO = new LmsCourseDTO();
            courseDTO.setName(request.getCourseName());
            courseDTO.setOwnerId(googleClassroomCred.getGoogleId());
            Course createdCourse = createGoogleCourse(classroom, courseDTO);
            String courseId = createdCourse.getId();
            // 2. Create Topics
            if (!request.getTopicList().isEmpty()) {
                for (LmsTopicRequest topicDTO : request.getTopicList()) {
                    LmsTopicDTO lmsTopicDTO = LmsTopicDTO.builder()
                            .name(topicDTO.getTopicName())
                            .courseId(courseId)
                            .build();
                    Topic topic = createGoogleTopic(classroom, lmsTopicDTO);
                    // 3. Create CourseWorks under Topics
                    if (!topicDTO.getCourseWorks().isEmpty()) {
                        for (LmsCourseWorkRequest courseWorkDTO : topicDTO.getCourseWorks()) {
                            if (CourseWorkType.ASSIGNMENT.equals(courseWorkDTO.getWorkType())) {
                                createGoogleCourseWorkAssignment(classroom, courseWorkDTO, courseId,
                                        topic, driveService, createdCourse);
                            } else if (CourseWorkType.QUIZ.equals(courseWorkDTO.getWorkType())) {
                                createGoogleCourseWorkQuiz(classroom, courseWorkDTO, courseId,
                                        topic, driveService, createdCourse);
                            } else if (CourseWorkType.MATERIAL.equals(courseWorkDTO.getWorkType())) {
                                createGoogleCourseWorkMaterial(classroom, courseWorkDTO, courseId,
                                        topic, driveService, createdCourse);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while creating full course in Google Classroom", e);
//            throw new ApplicationException(ResponseCode.BAD_REQUEST, "Error while creating full course in Google Classroom");
        }

        return "Course created successfully";
    }


    private CourseWork createGoogleCourseWork(Classroom classroom, CourseWorkDTO request) throws IOException {

        CourseWork courseWork = new CourseWork()
                .setTitle(request.getTitle())
                .setDescription(convertHtmlToClassroomText(request.getDescription()))
                .setState("PUBLISHED")
                .setDueDate(toClassroomDate(request.getDueDate()))
                .setDueTime(toClassroomTime(request.getDueDate()))
                .setMaxPoints(100.0)
                .setWorkType("ASSIGNMENT");

        return classroom.courses().courseWork()
                .create(String.valueOf(request.getCourseId()), courseWork)
                .execute();

    }


    private void createGoogleCourseWorkAssignment(Classroom classroom,
                                                  LmsCourseWorkRequest request,
                                                  String courseId,
                                                  Topic topic,
                                                  Drive drive,
                                                  Course createdCourse) throws IOException {

        List<Material> materials = buildMaterialsForCourseWork(request, drive, createdCourse);

        CourseWork courseWork = new CourseWork()
                .setTitle(request.getName())
                .setDescription(convertHtmlToClassroomText(request.getDescription()))
                .setState("PUBLISHED")
                .setTopicId(topic.getTopicId())
                .setDueDate(toClassroomDate(request.getDueDate()))
                .setDueTime(toClassroomTime(request.getDueDate()))
                .setMaxPoints(request.getMaxPoints())
                .setWorkType("ASSIGNMENT")
                .setMaterials(materials);
        ;
//        return
        classroom.courses().courseWork()
                .create(courseId, courseWork)
                .execute();

    }

    private void createGoogleCourseWorkMaterial(Classroom classroom,
                                                LmsCourseWorkRequest dto,
                                                String courseId,
                                                Topic topic,
                                                Drive drive,
                                                Course createdCourse) throws IOException {


        List<Material> materials = buildMaterialsForCourseWork(dto, drive, createdCourse);
        CourseWorkMaterial material = new CourseWorkMaterial()
                .setTitle(dto.getName())
                .setDescription(convertHtmlToClassroomText(dto.getDescription()))
                .setTopicId(topic.getTopicId())
                .setMaterials(materials)
                .setState("PUBLISHED");

        // Attach all materials
        // IMPORTANT: use courseWorkMaterials API
        classroom.courses()
                .courseWorkMaterials()
                .create(courseId, material)
                .execute();
    }


    //CourseWork
    private void createGoogleCourseWorkQuiz(Classroom classroom,
                                            LmsCourseWorkRequest request,
                                            String courseId,
                                            Topic topic,
                                            Drive drive,
                                            Course createdCourse) throws IOException {


        List<Material> materials = buildMaterialsForCourseWork(request, drive, createdCourse);
        // Attach Google Form as LINK (not FORM)
        if (request.getFormLink() != null && !request.getFormLink().isEmpty()) {
            Link link = new Link()
                    .setUrl(request.getFormLink())
                    .setTitle(request.getName());

            Material material = new Material().setLink(link);

            materials.add(material);
        }
        // Classroom does NOT allow FORM materials. WorkType should be ASSIGNMENT.
        CourseWork courseWork = new CourseWork()
                .setTitle(request.getName())
                .setDescription(convertHtmlToClassroomText(request.getDescription()))
                .setState("PUBLISHED")
                .setDueDate(toClassroomDate(request.getDueDate()))
                .setDueTime(toClassroomTime(request.getDueDate()))
                .setMaxPoints(request.getMaxPoints())
                .setTopicId(topic.getTopicId())
                .setWorkType("ASSIGNMENT")
                .setMaterials(materials)
                .setAssociatedWithDeveloper(true);

        // Create coursework
        classroom.courses()
                .courseWork()
                .create(courseId, courseWork)
                .execute();
    }

    // Create Course in Google Classroom
    private Course createGoogleCourse(Classroom classroom, LmsCourseDTO dto) throws Exception {
        Course course = new Course()
                .setName(dto.getName())
                .setSection(dto.getSection())
                .setDescriptionHeading(dto.getDescriptionHeading())
                .setRoom(dto.getRoom())
                .setOwnerId(dto.getOwnerId())
                .setCourseState(dto.getCourseState());

        return classroom.courses().create(course).execute();
    }

//    private void updateCourseImage(Classroom classroom, String courseId, String s3Key) {
//
//        try {
//            // Generate 10-minute pre-signed URL
//            String preSignedUrl = generatePresignedUrl(s3Key);
//
//            Course patch = new Course()
//                    .setPhotoUrl(preSignedUrl);
//
//            classroom.courses()
//                    .patch(courseId, patch)
//                    .setUpdateMask("photoUrl")
//                    .execute();
//
//            log.info("Google course image updated successfully for courseId={}", courseId);
//
//        } catch (Exception e) {
//            log.error("Failed to update course image", e);
//            throw new RuntimeException("Unable to update course picture");
//        }
//    }


    // Create Topic in a Course
    private Topic createGoogleTopic(Classroom classroom, LmsTopicDTO lmsTopicDTO) throws Exception {

        Topic topic = new Topic()
                .setTopicId(lmsTopicDTO.getTopicId())
                .setName(lmsTopicDTO.getName())
                .setCourseId(lmsTopicDTO.getCourseId());

        return classroom.courses().topics().create(lmsTopicDTO.getCourseId(), topic).execute();
    }


    private GoogleClassroomCred getGoogleClassroomCred() {
//        User currentUser = getCurrentUser();
//
//        return googleClassroomCredRepository.findByUserAndStatus(currentUser, Status.ACTIVE)
//                .orElseThrow(() -> new ApplicationException(ResponseCode.BAD_REQUEST, "Google classroom not connected"));


        return googleClassroomCredRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Google ClassroomCred not found"));
    }

//    public com.experience.model.User getCurrentUser() {
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//        return userRepository.findById(currentUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//    }

    private String getAccessToken(GoogleClassroomCred googleClassroomCred) throws IOException {

        LocalDateTime lastRefreshedAt = googleClassroomCred.getLastRefreshedAt();
        LocalDateTime currentTime = LocalDateTime.now();

        if (lastRefreshedAt != null && lastRefreshedAt.plusMinutes(59).isBefore(currentTime)) {
//            log.info("Refreshing access token for user :: ({})", googleClassroomCred.getUser().getEmail());
            Credential credential = googleClassroomManager.getCredential(googleClassroomCred.getState());
            if (credential != null && credential.refreshToken()) {
                googleClassroomCred.setAccessToken(credential.getAccessToken());
                googleClassroomCred.setLastRefreshedAt(LocalDateTime.now());
                googleClassroomCredRepository.save(googleClassroomCred);
                log.info("Access Token updated successfully!!!");
            }
        }
        return googleClassroomCred.getAccessToken();
    }


    private com.google.api.services.classroom.model.Date toClassroomDate(Date date) {
        if (null == date) return null;
        LocalDateTime ldt = toLocalDateTime(date);

        return new com.google.api.services.classroom.model.Date()
                .setYear(ldt.getYear())
                .setMonth(ldt.getMonthValue())
                .setDay(ldt.getDayOfMonth());
    }

    private TimeOfDay toClassroomTime(Date date) {
        if (null == date) return null;
        LocalDateTime ldt = toLocalDateTime(date);

        return new TimeOfDay()
                .setHours(ldt.getHour())
                .setMinutes(ldt.getMinute())
                .setSeconds(ldt.getSecond());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (null == date) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String convertHtmlToClassroomText(String html) {

        if (html == null || html.trim().isEmpty()) {
            return "";   // or return null; based on your requirement
        }

        // Remove inline styles
        html = html.replaceAll("(?i) style=\"[^\"]*\"", "");

        // HEADINGS
        html = html.replaceAll("(?i)<h1[^>]*>", "");
        html = html.replaceAll("(?i)</h1>", "");
        html = html.replaceAll("(?i)<h2[^>]*>", "");
        html = html.replaceAll("(?i)</h2>", "");
        html = html.replaceAll("(?i)<h3[^>]*>", "");
        html = html.replaceAll("(?i)</h3>", "");

        // Bold
        html = html.replaceAll("(?i)<b[^>]*>(\\s*)<b[^>]*>", "<b>");
        html = html.replaceAll("(?i)</b>(\\s*)</b>", "</b>");

        // Italics
        html = html.replaceAll("(?i)<(em|i)[^>]*>(.*?)</(em|i)>", "<i>$2</i>");

        // Paragraphs
        html = html.replaceAll("(?i)<p[^>]*>", "");
        html = html.replaceAll("(?i)</p>", "\n\n");

        // Lists
        html = html.replaceAll("(?i)<li[^>]*>", "• ");
        html = html.replaceAll("(?i)</li>", "\n");

        html = html.replaceAll("(?i)<ul[^>]*>", "\n");
        html = html.replaceAll("(?i)</ul>", "\n");

        html = html.replaceAll("(?i)<ol[^>]*>", "\n");
        html = html.replaceAll("(?i)</ol>", "\n");

        // Line breaks
        html = html.replaceAll("(?i)<br[^>]*>", "\n");

        // Remove leftover tags except <b>, <i>
        html = html.replaceAll("(?i)</?(?!b|i)[a-zA-Z0-9]+[^>]*>", "");

        // HTML Entities
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("&amp;", "&");
        html = html.replaceAll("&lt;", "<");
        html = html.replaceAll("&gt;", ">");
        html = html.replaceAll("&quot;", "\"");
        html = html.replaceAll("&#39;", "'");

        // Cleanup
        html = html.replaceAll("[ \t]+", " ");
        html = html.replaceAll("\n{3,}", "\n\n");

        return html.trim();
    }


    /**
     * This method is used to get S3 object like file
     * and upload on google drive
     * @param mat
     * @param driveService
     * @param courseFolderId
     * @return
     * @throws IOException
     */

//    private com.google.api.services.drive.model.File uploadFileToGoogleDrive(LmsCourseMaterialRequest mat, Drive driveService, String courseFolderId) throws IOException {
//
//        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//        fileMetadata.setName(mat.getTitle());
//        fileMetadata.setParents(Collections.singletonList(courseFolderId));
//
//        // Download file from S3 / CloudFront or use InputStream from your source
////        InputStream inputStream = new URL(mat.getAlternateLink()).openStream();
//        S3Object s3Object = awsS3Service.getS3ObjectAsStream(extractKeyFromCloudFrontUrl(mat.getAlternateLink()));
//        InputStream inputStream = s3Object.getObjectContent();
//        String ext = mat.getTitle().substring(mat.getTitle().lastIndexOf('.'));
//
//        String mimeType = getMimeTypeBasedOnExtension(ext);
//
//        FileContent mediaContent = new FileContent(mimeType, streamToTempFile(inputStream));
//
//        return driveService.files()
//                .create(fileMetadata, mediaContent)
//                .setFields("id,name,webViewLink")
//                .execute();
//    }

    public String extractKeyFromCloudFrontUrl(String url) {
        return url.substring(url.indexOf(".net/") + 5);
    }

    private String getMimeTypeBasedOnExtension(String ext) {
        switch (ext.toLowerCase()) {
            case ".pdf":
                return "application/pdf";
            case ".doc":
            case ".docx":
                return "application/msword";
            case ".ppt":
            case ".pptx":
                return "application/vnd.ms-powerpoint";
            case ".xls":
            case ".xlsx":
                return "application/vnd.ms-excel";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".txt":
                return "text/plain";
            default:
                return "application/octet-stream"; // Default binary type
        }
    }


    private java.io.File streamToTempFile(InputStream stream) throws IOException {
        java.io.File temp = java.io.File.createTempFile("upload-", ".tmp");
        java.nio.file.Files.copy(stream, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }

    public Material createDriveMaterial(com.google.api.services.drive.model.File uploadedDriveFile) {

        DriveFile driveFile = new DriveFile()
                .setId(uploadedDriveFile.getId())
                .setTitle(uploadedDriveFile.getName())
                .setAlternateLink(uploadedDriveFile.getWebViewLink())
                .setThumbnailUrl(uploadedDriveFile.getThumbnailLink());

        SharedDriveFile shared = new SharedDriveFile()
                .setDriveFile(driveFile)          // << IMPORTANT
                .setShareMode("VIEW");            // or EDIT / STUDENT_COPY

        return new Material().setDriveFile(shared);
    }

    private List<Material> buildMaterialsForCourseWork(LmsCourseWorkRequest request,
                                                       Drive drive,
                                                       Course createdCourse) throws IOException {
        List<Material> materials = new ArrayList<>();

        String courseFolderId = createdCourse.getTeacherFolder().getId();   // NEW

        for (LmsCourseMaterialRequest mat : request.getCourseMaterials()) {
            Material material = new Material();

            if (CourseMaterialType.LINK.equals(mat.getType())) {
                Link link = new Link()
                        .setUrl(mat.getAlternateLink())
                        .setTitle(mat.getTitle());
                material.setLink(link);
            } else if (CourseMaterialType.FILE.equals(mat.getType())) {
//                com.google.api.services.drive.model.File uploaded = uploadFileToGoogleDrive(mat, drive, courseFolderId);
//                material = createDriveMaterial(uploaded);
            }

            materials.add(material);
        }
        return materials;
    }


}

