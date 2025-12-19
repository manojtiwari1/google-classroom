package com.classroom.googel.factory;

import com.classroom.googel.enums.OAuthProvider;
import com.classroom.googel.service.LmsIntegrationService;
import com.classroom.googel.service.impl.GoogleClassroomServiceImpl;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class LmsCourseFactory {

    private final Map<OAuthProvider, LmsIntegrationService> serviceMap = new EnumMap<>(OAuthProvider.class);


    public LmsCourseFactory(List<LmsIntegrationService> lmsIntegrationServices) {
        for(LmsIntegrationService service : lmsIntegrationServices) {
            if(service instanceof GoogleClassroomServiceImpl) {
                serviceMap.put(OAuthProvider.GOOGLE, service);
            }
        }
    }

    public LmsIntegrationService getService(OAuthProvider provider){
        return serviceMap.get(provider);
    }
}
