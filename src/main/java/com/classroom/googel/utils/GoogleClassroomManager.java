package com.classroom.googel.utils;

import com.classroom.googel.entity.GoogleClassroomCred;
import com.classroom.googel.repository.GoogleClassroomCredRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.drive.DriveScopes;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GoogleClassroomManager {

    private final String APPLICATION_NAME = "worked-classroom";

    private final HttpTransport httpTransport = new NetHttpTransport();

    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = List.of(
            ClassroomScopes.CLASSROOM_COURSES,
            ClassroomScopes.CLASSROOM_ROSTERS,
            ClassroomScopes.CLASSROOM_COURSEWORK_ME,
            ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS,
            ClassroomScopes.CLASSROOM_TOPICS,
            ClassroomScopes.CLASSROOM_PROFILE_EMAILS,
            ClassroomScopes.CLASSROOM_COURSEWORKMATERIALS,
            ClassroomScopes.CLASSROOM_COURSEWORKMATERIALS_READONLY,
            DriveScopes.DRIVE_FILE,
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile",
            "openid"

    );
    private final GoogleClassroomCredRepository googleClassroomCredRepository;

    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;


    @Value("${google.classroom.oauth.callback.uri}")
    private String callBackURI;


    @Value("${google.classroom.secret.key.path}")
    private Resource secretKeyPath;

    @Value("${google.classroom.credentials.folder.path}")
    private Resource clientCredentials;

    @Value("${google.classroom.client.id}")
    private String clientId;


    @Value("${google.classroom.credentials.token.expiration}")
    private Long tokenExpiryTime;

    public GoogleClassroomManager(GoogleClassroomCredRepository googleClassroomCredRepository) {
        this.googleClassroomCredRepository = googleClassroomCredRepository;
    }


    @PostConstruct
    public void init() throws IOException {
        GoogleClientSecrets googleClientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(secretKeyPath.getInputStream()));
        googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, googleClientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(clientCredentials.getFile())).build();
    }

    public String doGoogleSignIn(String state) {
        GoogleAuthorizationCodeRequestUrl url = googleAuthorizationCodeFlow.newAuthorizationUrl();
        return url.setRedirectUri(callBackURI)
                .setAccessType("offline")
                .set("prompt", "consent")
                .setState(state).build(); // select_account, consent
    }

    public GoogleTokenResponse generateToken(String code, String userIdentifierKey) {
        GoogleTokenResponse googleTokenResponse=null;
        try{
            log.info("Saving token for userIdentifierKey: {}", userIdentifierKey);
            googleTokenResponse = googleAuthorizationCodeFlow.newTokenRequest(code).
                    setRedirectUri(callBackURI).
                    execute();


            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory
            ).setAudience(Collections.singletonList(clientId))  // your Google OAuth client ID
                    .build();
            String idTokenString = googleTokenResponse.getIdToken();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
//                throw new ApplicationException(ResponseCode.BAD_REQUEST, "Invalid ID Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            log.info("Google Payload received => {}", payload);
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = (String) payload.get("sub");
//            String picture = (String) payload.get("picture");

            // ----------- SAVE IN DATABASE -----------------
            Optional<GoogleClassroomCred> googleClassroomCredOptional =
                    googleClassroomCredRepository.findByState(userIdentifierKey);
            if(googleClassroomCredOptional.isPresent()){
                GoogleClassroomCred googleClassroomCred = googleClassroomCredOptional.get();
                googleClassroomCred.setEmail(email);
                googleClassroomCred.setName(name);
                googleClassroomCred.setGoogleId(googleId);
//                googleClassroomCred.setProfilePicture(picture);
                googleClassroomCredRepository.save(googleClassroomCred);
            }

            googleTokenResponse.setExpiresInSeconds(tokenExpiryTime);
            googleAuthorizationCodeFlow.createAndStoreCredential(googleTokenResponse, userIdentifierKey);
        }catch (Exception e){
            e.printStackTrace();
//            throw new ApplicationException(ResponseCode.BAD_REQUEST, "Error while saving token. Please try again.");
        }

        return googleTokenResponse;
    }


    public Credential getCredential(String state) throws IOException {
        return googleAuthorizationCodeFlow.loadCredential(state);
    }
}
