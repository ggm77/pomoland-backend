# Pomoland (뽀모도로 땅따먹기)

> 공부 시간을 보여주는 서비스가 아니라, 공부한 시간이 내 땅이 되게 한다.

뽀모도로 세션을 완주할 때마다 포인트를 얻고, 그 포인트로 모두가 공유하는 맵 위의 땅(타일)을 점령·방어하는 게임형 학습 동기 부여 서비스의 백엔드 서버입니다.

## 기획 배경

뽀모도로 기법(집중 25분 + 휴식 5분 반복)은 널리 쓰이지만, 타이머 자체는 "시작하게 만들고 끝까지 채우게 만드는 동기"까지 주지는 못합니다. 혼자 공부하는 사용자는 다음과 같은 문제를 겪습니다.

- 타이머를 켜는 것 자체가 순전히 개인 의지에 달려 있다.
- 세션을 중간에 끊어도 아무 손해가 없어 완주할 이유가 약하다.
- 공부 시간이 숫자 기록으로만 남아 성취감이 약하다.
- 경쟁·상호작용 요소가 없어 며칠 쓰다 흥미를 잃는다.

Pomoland는 **세션 "완주"에만 보상을 지급**해 게임 규칙 자체가 뽀모도로 원칙(끊지 않는 집중)을 강제하도록 설계했습니다. 공부 시간이 숫자가 아니라 공유 맵 위의 눈에 보이는 영토로 남고, 다른 사용자와 타일을 점령·방어·탈환하며 직접 상호작용한다는 점이 기존 기록형/개인 보상형 타이머 앱과 다른 지점입니다.

## 게임 규칙 (서버 구현 기준)

모든 판정은 클라이언트가 아니라 서버가 기록한 세션 데이터를 기준으로 계산됩니다.

- **세션**: 유저별 `studyTime`(기본 25분) 길이의 세션을 생성하며, 30초(`HEARTBEAT_INTERVAL`)마다 하트비트를 보내 진행 중임을 알립니다. 하트비트가 끊기면 세션은 만료 처리됩니다. 동시에 살아있는 세션은 유저당 1개만 허용됩니다.
- **완주 판정**: 세션 종료 시각(`endAt`)이 지난 뒤에만 완료 처리가 가능하며, 포기(abandon)한 세션은 보상이 없습니다.
- **포인트 적립**: 세션 완료 시 `(세션 분 / 5) * 2` 포인트를 지급합니다 (25분 세션 기준 10포인트). 동시에 일자별 공부시간 통계(`StudyDailyStat`)가 갱신되어 랭킹/통계에 반영됩니다.
- **타일 점령(occupy)**:
  - 빈 타일은 투자한 포인트만큼 방어력으로 설정하며 그대로 점령됩니다.
  - 이미 점유된 타일은 **현재 방어력보다 큰 포인트**를 투자해야 빼앗을 수 있습니다.
  - 스폰 포인트로 지정된 타일은 점령할 수 없습니다.
  - 내가 이미 보유한 타일과 **상하좌우로 인접한 좌표만** 점령 가능합니다.
  - 동시에 같은 빈 타일을 점령하려는 경쟁 상황은 유니크 제약(x, y) + `saveAndFlush`로 충돌을 감지해 한쪽만 성공시킵니다.
- **타일 방어(defense)**: 내가 보유한 타일에 포인트를 추가로 투자해 방어력을 높일 수 있습니다.
- **스폰 포인트**: 유저마다 맵 범위 내 좌표를 지정해 자신의 첫 거점(스폰 타일)을 만듭니다.
- **랭킹**: 일간/주간 공부시간(KST 기준), 보유 타일 수, 보유 포인트 4가지 기준으로 상위 N명을 조회합니다.

## 주요 기능

- **소셜 로그인**: Apple, Google OAuth2 로그인 및 JWT(Access/Refresh) 발급·재발급
- **뽀모도로 세션**: 세션 생성 → 하트비트로 진행 유지 → 완료/포기 처리, 포인트·공부시간 자동 적립
- **맵 & 타일**: 전체 맵/특정 타일 조회, 타일 점령(occupy), 타일 방어력 강화(defense), 스폰 포인트 지정
- **랭킹**: 일간/주간 공부 시간, 보유 타일 수, 보유 포인트 랭킹 조회
- **유저 관리**: 내 정보 조회/수정, 회원 탈퇴, 공부/휴식 시간 등 개인 설정

## 기술 스택

- **Language / Runtime**: Java 25
- **Framework**: Spring Boot 4.1 (Web, Data JPA, Security, WebFlux(WebClient))
- **DB**: MariaDB (JDBC)
- **인증**: JWT (jjwt), Apple/Google OAuth2
- **기타**: Lombok, BouncyCastle(PEM 파싱), springdoc-openapi(Swagger UI)
- **빌드**: Gradle (Wrapper 포함)
- **(별도 저장소) Frontend**: React

## 프로젝트 구조

```
src/main/java/com/seohamin/pomoland
├── domain
│   ├── auth
│   │   ├── oauth2      # Apple/Google 로그인 API
│   │   └── token       # JWT 재발급 API
│   ├── map/tile        # 맵/타일 조회, 점령, 방어 API
│   ├── ranking          # 일간/주간/타일/포인트 랭킹 API
│   ├── session          # 뽀모도로 세션 생성/유지/완료/포기 API
│   └── user             # 유저 조회/수정/탈퇴, 스폰포인트, 설정 API
└── global
    ├── auth            # JWT, Apple 클라이언트 시크릿 서명 등 인증 공통 로직
    ├── config          # Swagger, Security 등 설정
    ├── crypto          # OAuth 리프레시 토큰 AES 암복호화
    └── exception       # 공통 예외 처리
```

각 도메인은 `controller / service / repository / entity / dto`로 구성됩니다.

## API 개요

| 도메인 | Base Path | 설명 |
|---|---|---|
| Auth (OAuth2) | `/api/v1/auth/oauth2/*` | Apple/Google 로그인, Apple 콜백 처리 |
| Auth (Token) | `/api/v1/auth/token/refresh` | Refresh Token으로 JWT 재발급 |
| User | `/api/v1/users/*` | 유저 조회/수정/탈퇴, 스폰포인트, 설정 |
| Session | `/api/v1/session/*` | 뽀모도로 세션 생성/하트비트/조회/포기/완료 |
| Map/Tile | `/api/v1/map/tiles/*` | 맵/타일 조회, 타일 점령, 타일 방어 |
| Ranking | `/api/v1/rankings/*` | 일간/주간/타일/포인트 랭킹 조회 |

인증이 필요한 API는 JWT Access Token을 `Authorization` 헤더로 전달해야 합니다.

### 상세 엔드포인트

**Auth**
- `POST /api/v1/auth/oauth2/apple`, `/oauth2/google` — 소셜 로그인, JWT 발급
- `POST /api/v1/auth/oauth2/callback/apple` — Apple 인가 코드 콜백 → 프론트 리다이렉트
- `POST /api/v1/auth/token/refresh` — Refresh Token으로 재발급

**User**
- `GET /api/v1/users/{userId}`, `GET /users/me` — 유저 조회
- `PATCH /users/me`, `DELETE /users/me` — 정보 수정, 회원 탈퇴
- `POST /users/me/spawnpoint` — 스폰 포인트(첫 거점) 생성
- `GET`/`PUT /users/me/settings` — 공부/휴식 시간 등 설정 조회·수정

**Session**
- `POST /session` — 세션 시작
- `POST /session/{sessionUuid}/heartbeat` — 진행 유지(30초 주기)
- `GET /session/{sessionUuid}` — 세션 상태 조회
- `POST /session/{sessionUuid}/abandon` — 세션 포기
- `POST /session/{sessionUuid}/complete` — 세션 완료 → 포인트 적립

**Map/Tile**
- `GET /map/tiles` — 전체 맵 조회
- `GET /map/tiles/{x}/{y}` — 특정 타일 조회
- `POST /map/tiles/{x}/{y}/occupy` — 타일 점령
- `POST /map/tiles/{x}/{y}/defense` — 타일 방어력 강화

**Ranking**
- `GET /rankings/daily`, `/weekly` — 공부시간 랭킹
- `GET /rankings/tiles` — 보유 타일 수 랭킹
- `GET /rankings/points` — 보유 포인트 랭킹

## 로드맵

기획 초안 기준으로 다음 항목들은 아직 미구현 상태이며 확장 예정입니다.

- 방어력 자동 감소(decay), 하루 인정 포인트 상한 등 밸런스/부정행위 방지 로직
- 신규 유저 스폰 주변 보호 구역
- 시즌제(주기적 맵 리셋)와 시즌 보상(뱃지, 타일 테마)
- WebSocket(STOMP) 기반 실시간 맵 갱신 (현재는 폴링 기반)
- 잔디(연속 공부 일수) 시각화, 과목/태그별 통계
- 탭 이탈·화면 전환 감지를 통한 부정행위 방지