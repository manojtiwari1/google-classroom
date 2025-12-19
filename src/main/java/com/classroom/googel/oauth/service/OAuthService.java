package com.classroom.googel.oauth.service;

public interface OAuthService {

    String generateState();

    String doLogin(final String state);


    void handleCallback(String code, String state);

//    LmcUserVerifyState verifyUserState(Long organizationId);


    String disconnectIntegration();
}
