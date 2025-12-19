package com.classroom.googel.oauth.service.impl;

import com.classroom.googel.entity.GoogleClassroomCred;
import com.classroom.googel.enums.Status;
import com.classroom.googel.oauth.service.OAuthService;
import com.classroom.googel.repository.GoogleClassroomCredRepository;
import com.classroom.googel.utils.GoogleClassroomManager;
import com.classroom.googel.utils.StringUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements OAuthService {

//    private final UserRepository userRepository;

    private final GoogleClassroomCredRepository googleClassroomCredRepository;

//    private final GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;

    private final GoogleClassroomManager googleClassroomManager;


    @Override
    public String generateState() {
//        User currentUser = getCurrentUser();
//
//        Optional<GoogleClassroomCred> googleClassroomCredOptional = googleClassroomCredRepository.findByUserAndStatus
//                (currentUser, Status.ACTIVE);
//        if (googleClassroomCredOptional.isPresent()) {
//            GoogleClassroomCred googleClassroomCred = googleClassroomCredOptional.get();
//            googleClassroomCred.setStatus(Status.INACTIVE);
//            googleClassroomCredRepository.save(googleClassroomCred);
//        }

        GoogleClassroomCred goolGoogleClassroomCred = GoogleClassroomCred.builder()
                .state(StringUtils.generateRandomString(30, true, true))
//                .user(currentUser)
                .build();
        goolGoogleClassroomCred = googleClassroomCredRepository.save(goolGoogleClassroomCred);
        return goolGoogleClassroomCred.getState();
    }

    @Override
    public String doLogin(final String state) {
        return googleClassroomManager.doGoogleSignIn(state);
    }


    @Override
    public void handleCallback(final String code, final String state) {

        Optional<GoogleClassroomCred> googleClassroomCredOptional = googleClassroomCredRepository.findByState(state);
        if (googleClassroomCredOptional.isEmpty()) return;


        GoogleClassroomCred googleClassroomCred = googleClassroomCredOptional.get();
        GoogleTokenResponse googleTokenResponse = googleClassroomManager.generateToken(code, state);
        googleClassroomCred.setCode(googleTokenResponse.getAccessToken());
        googleClassroomCred.setStatus(Status.ACTIVE);
        googleClassroomCred.setAccessToken(googleTokenResponse.getAccessToken());
        googleClassroomCred.setLastRefreshedAt(LocalDateTime.now());
        googleClassroomCredRepository.save(googleClassroomCred);
    }

//    @Override
//    public LmcUserVerifyState verifyUserState(Long organizationId) {
//        boolean isGoogleVerified = googleClassroomCredRepository
//                .findByUserAndStatus(getCurrentUser(), Status.ACTIVE)
//                .isPresent();
//        return new LmcUserVerifyState(isGoogleVerified, false, false);
//    }

    @Override
    public String disconnectIntegration() {
//        Optional<GoogleClassroomCred> googleClassroomCredOptional = googleClassroomCredRepository
//                .findByUserAndStatus(getCurrentUser(), Status.ACTIVE);
//
//        if (googleClassroomCredOptional.isEmpty()) {
//            throw new ResourceNotFoundException("You haven't connect Google Classroom");
//        }

        Optional<GoogleClassroomCred> googleClassroomCredOptional = googleClassroomCredRepository
                .findByStatus(Status.ACTIVE);

        if (googleClassroomCredOptional.isEmpty()) {
//            throw new ResourceNotFoundException("You haven't connect Google Classroom");
        }

        GoogleClassroomCred googleClassroomCred = googleClassroomCredOptional.get();
        googleClassroomCred.setStatus(Status.INACTIVE);
        googleClassroomCredRepository.save(googleClassroomCred);
        return "You have successfully disconnected google classroom.";
    }

}