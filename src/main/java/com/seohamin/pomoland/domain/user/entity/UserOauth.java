package com.seohamin.pomoland.domain.user.entity;

import com.seohamin.pomoland.global.crypto.OauthRefreshTokenConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_user_oauth_provider_provider_user_id",
        columnNames = {"provider", "provider_user_id"}
))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member;

    //google, apple...
    @Column(length = 20, nullable = false)
    @NotNull
    @Size(max = 20)
    private String provider;

    //google에서의 sub, naver의 id같이 oauth에서 쓰는 식별자
    @Column(length = 255, nullable = false)
    @NotNull
    @Size(max = 255)
    private String providerUserId;

    @Column(length = 320, nullable = true)
    @Size(max = 320)
    private String email;

    @Column(length = 255, nullable = true)
    @Size(max = 255)
    private String name;

    @Column(length = 2048, nullable = true)
    @Size(max = 2048)
    private String profileImage;

    //oauth에서 제공하는 리프레시 토큰 (unlink시 필요함)
    //DB 유출 시 서드파티 토큰까지 유출되는 것을 막기 위해 AES-GCM으로 암호화해서 저장함
    //컬럼 길이는 암호화(IV+태그)와 base64 인코딩으로 늘어난 길이를 담을 수 있도록 여유있게 설정
    @Convert(converter = OauthRefreshTokenConverter.class)
    @Column(length = 5600, nullable = true)
    @Size(max = 4100)
    private String refreshToken;

    @CreatedDate
    @Column(nullable = false)
    private Instant linkedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public UserOauth(
            final User user,
            final String provider,
            final String providerUserId,
            final String email,
            final String name,
            final String profileImage,
            final String refreshToken
    ) {
        this.member = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
