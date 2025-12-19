package com.classroom.googel.oauth.factory;

import com.classroom.googel.enums.OAuthProvider;
import com.classroom.googel.oauth.service.OAuthService;
import com.classroom.googel.oauth.service.impl.GoogleOAuthServiceImpl;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuthServiceFactory {

    private Map<OAuthProvider, OAuthService> oauthServiceMap =
            new EnumMap<OAuthProvider, OAuthService>(OAuthProvider.class);

    public OAuthServiceFactory(List<OAuthService> oAuthServices){
        for(OAuthService oAuthService: oAuthServices){
            if(oAuthService instanceof GoogleOAuthServiceImpl){
                oauthServiceMap.put(OAuthProvider.GOOGLE, oAuthService);
            }
        }
    }


    public OAuthService getOAuthService(OAuthProvider provider){
        return oauthServiceMap.get(provider);
    }
}
