package com.classroom.googel.utils;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import org.springframework.stereotype.Component;

@Component
public class GoogleClassroomClient {


    public Classroom getClient(String accessToken) {

        HttpRequestInitializer httpRequestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
            request.setConnectTimeout(3 * 60000);
            request.setReadTimeout(3 * 60000);
        };

        return new Classroom.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                httpRequestInitializer
        ).setApplicationName("Worked")
                .build();
    }
}
