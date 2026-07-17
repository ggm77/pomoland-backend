package com.seohamin.pomoland.global.auth.jwt;

import com.seohamin.pomoland.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        final String authorizationHeader = httpServletRequest.getHeader("Authorization");

        //JWT를 제대로 가지고 있는지 검사
        //없으면 익명으로 진행
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            final String jwt =  authorizationHeader.substring(7);

            //JWT 검증
            //검증 실패하면 익명으로 진행
            final String userIdStr;
            final Claims jwtClaims;
            try {
                jwtClaims = jwtProvider.getClaims(jwt);
                userIdStr = jwtClaims.getSubject();
            } catch (CustomException ex) { //검증 실패시 던져지는 CustomException 무시하고 진행시키기
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            //액세스 토큰이 아니면(리프레시 토큰 등) 익명으로 진행
            //authorities 클레임 유무 같은 우연에 기대지 않고 명시적으로 토큰 타입을 검증함
            if (!jwtProvider.isAccessToken(jwtClaims)) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            //토큰에서 Claims 추출
            final List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthorities(jwtClaims);

            //이미 JWT 검증으로 인증 완료됨
            final Authentication auth =
                    new UsernamePasswordAuthenticationToken(userIdStr, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            //JWT 검증 실패해서 401 던짐
            SecurityContextHolder.clearContext();
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().write(
                    "{\"status\":\"UNAUTHORIZED\",\"message\":\"토큰이 만료되었거나 없습니다.\",\"timestamp\":\"" + LocalDateTime.now() + "\"}"
            );
        }
    }
}
