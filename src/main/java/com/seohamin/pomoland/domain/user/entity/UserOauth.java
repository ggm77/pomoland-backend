package com.seohamin.pomoland.domain.user.entity;

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

import java.time.LocalDateTime;

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
    @Column(length = 4100, nullable = true)
    @Size(max = 4100)
    private String refreshToken;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime linkedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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
