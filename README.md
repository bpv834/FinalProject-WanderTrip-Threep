
**WanderTrip**은 "어디로 떠나야 할지" 고민하는 사람들을 위한 여행 일정 + 여행지 룰렛 추천 앱입니다.  
랜덤 여행지 추천 기능부터 일정 관리, 여행기 작성까지!  
당신의 여행을 더욱 쉽고 재미있게 만들어 드립니다.

---

## 📎 관련 링크

- 📱 **Play Store**: [WanderTrip 다운로드](https://play.google.com/store/apps/details?id=com.lion.wandertrip)
- 📝 **Velog 블로그**: [개발 기록 보러가기](https://velog.io/@bpv834/posts)

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🗺️ 여행지 룰렛 | 한반도 전체 지도 위에서 랜덤으로 여행지를 추천받을 수 있어요. 직접 선택도 가능해요. |
| 📆 일정 관리 | 날짜별로 여행 일정을 작성하고, 관광지/식당/숙소를 룰렛으로 랜덤 선택할 수 있어요. |
| 🌟 인기 여행지 추천 | 국내 관광객 데이터를 기반으로 인기 있는 여행지와 도시를 추천해요. |
| ❤️ 관심 콘텐츠 | 여행지를 관심 목록에 저장하고 나중에 빠르게 다시 확인할 수 있어요. |
| 📝 여행기 작성 | 여행 후기를 자유롭게 작성하고 공유할 수 있어요. 스크랩 기능으로 다른 유저의 여행기를 일정에 담을 수 있어요. |
| 📍 구글맵 연동 | 여행 일정을 지도 위에 시각화해 방문한 장소를 한눈에 볼 수 있어요. |

---

## 🛠️ 사용 기술

### 🧱 Architecture
- **MVVM + Clean Architecture**
- Repository → UseCase(Service) → ViewModel

### 🎨 UI
- **Jetpack Compose** 기반 화면 구성
- **Navigation Compose** 로 화면 전환
- **StateFlow** 기반 상태 관리

### 🔧 비동기 & DI
- **Hilt** (의존성 주입)
- **Coroutines / Flow / LiveData** 병행 사용

### ☁️ Firebase & Cloud
- **Firebase Authentication**: 익명 로그인 및 사용자 인증
- **Firebase Storage**: 이미지 업로드/관리
- **Firebase Functions Gen2**: 외부 API 호출용 서버리스 함수 실행 (e.g. Kakao API 요청)
- **GCP Secret Manager**: Kakao API Key 등 민감한 키를 보안 저장소에서 로드하여 Functions에서 사용

### 🌐 외부 연동
- **Kakao Login**
- **Kakao Local REST API** (여행지 검색 및 위치 기반 데이터 조회)
- **Google Map**
- **Retrofit2** (네트워크 통신)
- **Glide** (이미지 로딩)

---

## 📁 프로젝트 구조

com.lion.wandertrip  
├── model             # UI에서 사용하는 화면 전용 모델 (화면 표현에 특화된 데이터)  
├── vo                # 비즈니스 계층에서 사용하는 VO (Value Object)  
├── repository        # 외부 데이터 접근 계층 (API, Firebase 등)  
├── service           # 비즈니스 로직 계층 (UseCase 역할)  
├── presentation      # UI 계층 (Compose UI, ViewModel 포함)  
│   └── my_interesting_page  
│   └── trip_detail  
│   └── trip_note ...  
├── util              # Const, 공용 유틸 함수  
├── di                # Hilt 모듈 설정 (DI 구성)  
└── TripApplication.kt  # Application 클래스 (앱 전역 상태 관리)  

---

## 🎯 주요 기술적 특징

| 기술 포인트 | 설명 |
|-------------|------|
| ✅ **스마트 컴포지션 최적화** | 관심 콘텐츠 항목에서 `remember`와 `key`를 활용해 개별 컴포저블을 캐싱하고, 상태 변경 항목만 리컴포지션되도록 범위를 좁혀 성능 최적화 |
| ✅ **관심 콘텐츠 필터링** | 지역/카테고리 기반 필터링, 좋아요 취소 시에도 기존 UI를 최소한만 리렌더링하도록 설계 |
| ✅ **Scroll 제스처 충돌 해결** | `GoogleMap`을 `LazyColumn` 등 스크롤 가능한 컴포넌트 안에 넣을 때, `Modifier.pointerInput`과 `awaitPointerEventScope`를 사용해 터치를 감지하고, 터치 중에는 `userScrollEnabled = false`로 상위 스크롤을 비활성화하여 제스처 충돌을 해결함 |
| ✅ **SavedStateHandle 활용** | 상세화면에서 좋아요 상태가 변경되었는지 확인하기 위해 `likeChangedContentId`, `likeChangedState`를 `savedStateHandle`에 저장하고, 목록 복귀 시 `observeAsState()` + `LaunchedEffect`로 해당 항목만 상태 갱신 처리. 단순 refresh 플래그 방식에서 벗어나, 실제 변경된 상태만 리렌더링하도록 개선 |
| ✅ **룰렛 애니메이션 구현** | `Animatable`, `graphicsLayer`, `LaunchedEffect`, `coroutineScope`를 조합해 Jetpack Compose에서 자연스러운 룰렛 회전 애니메이션 구현. 회전 속도 감속, 도착 지점 보정 등 물리 기반 회전을 정밀하게 표현 |
| ✅ **지역 기반 외부 검색** | Kakao Local API + 지도 연동을 통해, 사용자가 선택한 지역(시/군/구) 기반으로 위도/경도를 가져오고 주변 장소(관광지, 식당, 숙소 등)를 실시간 검색하여 일정에 바로 추가 가능 |

---

## 🧪 개선 예정 항목 및 로드맵

- 🤝 친구와 **일정 공유 기능**
- 🎲 사용자별 랜덤 룰렛 여행 횟수를 기록하고, 이를 기반으로 랭킹을 제공하는 기능 추가 (사용자별 참여도 추적 및 경쟁 유도)
- 🔔 여행 일정 시작 알림 기능 추가
- 🔔 관심 콘텐츠 업데이트 알림 기능 추가
- 🔔 친구 일정 공유 알림 기능 추가 (향후 친구 공유 기능 연계)

## 🧩 추가 기능 및 구현 기술
🔒 사용자 데이터 해시화 처리 및 보안 강화 (JBcrypt 사용)
📁 내부 저장소 파일 관리 (앨범, 카메라 촬영 이미지 저장 등)
📸 카메라 및 갤러리 연동 기능
☁️ Firebase Storage를 이용한 이미지 및 파일 업로드/관리
📝 여행 후기 및 리뷰 작성 기능
📞 액티비티 인텐트 활용하여 전화번호 클릭 시 다이얼러 실행
🔗 외부 링크 클릭 시 웹 브라우저 또는 해당 앱으로 연결
