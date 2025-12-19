package com.classroom.googel.utils;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class GoogleDriveUploader {


    public Drive getDriveClient(String accessToken) {

        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        };

        return new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer).
                setApplicationName("Worked").
                build();
    }

    public com.google.api.services.drive.model.File upload(
            Drive drive,
            File localFile,
            String mimeType
    ) throws IOException {

        FileContent fileContent = new FileContent(mimeType, localFile);

        com.google.api.services.drive.model.File fileMeta =
                new com.google.api.services.drive.model.File()
                        .setName(localFile.getName());

        return drive.files()
                .create(fileMeta, fileContent)
                .setFields("id, name, webViewLink, thumbnailLink")
                .execute();
    }
}
