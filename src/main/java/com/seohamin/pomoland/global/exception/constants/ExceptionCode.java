package com.seohamin.pomoland.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원되지 않는 OAuth입니다."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 정보가 잘못되어 있습니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "회원가입된 유저가 이미 존재 합니다."),
    APPLE_AUTH_ERROR(HttpStatus.BAD_REQUEST, "애플 인증에서 문제가 발생했습니다."),
    INVALID_PAGING_PARAMETER(HttpStatus.BAD_REQUEST, "페이지네이션 파라미터가 잘못되어있습니다."),
    INVALID_QUERY(HttpStatus.BAD_REQUEST, "검색 쿼리의 길이가 올바르지 않거나 없습니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 Enum입니다."),
    INVALID_MAX_DISTANCE(HttpStatus.BAD_REQUEST, "올바르지 않은 최대 거리입니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "잘못된 OAuth provider입니다."),
    TILE_NOT_EXIST(HttpStatus.BAD_REQUEST, "타일이 존재하지 않습니다."),
    SPAWNPOINT_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "스폰포인트가 이미 존재합니다."),
    TILE_ALREADY_OCCUPIED(HttpStatus.BAD_REQUEST, "타일이 이미 점령 되었습니다."),
    TILE_IS_SPAWNPOINT(HttpStatus.BAD_REQUEST,  "타일이 스폰 포인트입니다."),
    CANNOT_OCCUPY(HttpStatus.BAD_REQUEST, "점령할 수 없습니다."),
    TILE_NOT_OWNED(HttpStatus.BAD_REQUEST, "타일을 소유하지 않았습니다."),


    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않았습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 기능에 접근할 수 없습니다."),
    COOL_DOWN(HttpStatus.TOO_MANY_REQUESTS, "쿨다운 중 입니다."),
    DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "일일 요청가능한 횟수를 초과했습니다."),
    ATTEMPT_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "시도 가능한 횟수를 초과했습니다."),

    GOOGLE_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "구글과 통신 중 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서버를 이용할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
