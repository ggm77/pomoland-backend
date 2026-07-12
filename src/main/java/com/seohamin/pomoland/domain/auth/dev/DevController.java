package com.seohamin.pomoland.domain.auth.dev;

import com.seohamin.pomoland.domain.user.entity.User;
import com.seohamin.pomoland.domain.user.repository.UserRepository;
import com.seohamin.pomoland.global.auth.jwt.JwtProvider;
import com.seohamin.pomoland.global.constant.Role;
import com.seohamin.pomoland.global.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DevController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @GetMapping("/dev/token")
    public ResponseEntity<JwtDto> getDevToken(
            @RequestParam final Long userId
    ) {
        return ResponseEntity.ok(
                new JwtDto(
                    jwtProvider.creatAccessToken(userId, Role.USER),
                    jwtProvider.getTokenType(),
                    jwtProvider.getAccessTokenExpirationTime(),
                    jwtProvider.creatRefreshToken(userId)
                )
        );
    }

    @PostMapping("/dev/user")
    public ResponseEntity<Void> createUser(
            @RequestParam final String username
    ) {

        final User user = User.builder()
                .role(Role.USER)
                .username(username)
                .point(100)
                .pomoTry(0)
                .pomoComplete(0)
                .build();

        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}
