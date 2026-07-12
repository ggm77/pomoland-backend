package com.seohamin.pomoland.domain.auth.oauth2.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

// TODO: 실제 프론트엔드가 배포되면 이 컨트롤러는 제거하고 프론트에서 콜백을 처리한다.
@Controller
public class OauthCallbackController {

    @GetMapping("/oauth2/callback/google")
    public void googleCallback(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/oauth-test.html").forward(request, response);
    }

    @GetMapping("/oauth2/callback/apple")
    public void appleCallback(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/oauth-test.html").forward(request, response);
    }
}