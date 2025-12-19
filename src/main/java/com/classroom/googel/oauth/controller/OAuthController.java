package com.classroom.googel.oauth.controller;

import com.classroom.googel.enums.OAuthProvider;
import com.classroom.googel.oauth.factory.OAuthServiceFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

/**
 * @author Manoj Tiwari
 * 28/11/2025
 *
 */

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth/oauth")
public class OAuthController {

    private final OAuthServiceFactory oAuthServiceFactory;

    @GetMapping("/state/{provider}")
    public ResponseEntity<?> googleClassroomCredDTOResponseEntity(@PathVariable OAuthProvider provider) {

        String generateState =oAuthServiceFactory.getOAuthService(provider).generateState();
        return ResponseEntity.ok(generateState);
    }

    @GetMapping("/authorize")
    public ResponseEntity<?> doGoogleSignIn(HttpServletRequest request) {
        String state = request.getParameter("state");
        OAuthProvider provider = OAuthProvider.valueOf(request.getParameter("provider"));
        String consentUrl = oAuthServiceFactory.getOAuthService(provider).doLogin(state);
        return ResponseEntity.ok(consentUrl);
    }


    @GetMapping("/classroom")
    public void saveAuthorizationCode(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        oAuthServiceFactory.getOAuthService(OAuthProvider.GOOGLE).handleCallback(code, state);
        // Return an HTML response to auto-close the popup
        String closeScript = "<html> <body><script>window.opener.postMessage('auth-success', '*'); // Notify the parent window window.close(); // Close the popup </script></body></html>";
        response.setContentType("text/html");
        response.getWriter().write(closeScript);
    }


//    @GetMapping("/verify-user-state")
//    public ResponseEntity<?> verifyUserState() {
//        return oAuthServiceFactory.getOAuthService(OAuthProvider.GOOGLE).verifyUserState();
//    }

    @PatchMapping("/disconnect-google-classroom")
    public ResponseEntity<?> disconnectGoogleClassroomIntegration(){
        String message = oAuthServiceFactory.getOAuthService(OAuthProvider.GOOGLE).disconnectIntegration();
        return ResponseEntity.ok(message);
    }

}
