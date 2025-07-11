package com.lion.wandertrip.util



// 목적지 타입
enum class ContentTypeId(val contentTypeCode: Int, val contentTypeName: String) {
    TOURIST_ATTRACTION(12, "관광지"),
    ACCOMMODATION(32, "숙박"),
    RESTAURANT(39, "음식점"),
}

// 상태
enum class State(val stateNumber: Int, val stateStr: String) {
    ACTIVATE(1, "활성화"),
    DEACTIVATE(2, "비활성화"),
}

// 주요 도시 및 광역시의 AreaCode
enum class AreaCode(val areaCode: Int, val areaName: String) {
    SEOUL(1, "서울"),
    INCHEON(2, "인천"),
    DAEJEON(3, "대전"),
    DAEGU(4, "대구"),
    GWANGJU(5, "광주"),
    BUSAN(6, "부산"),
    ULSAN(7, "울산"),
    SEJONG(8, "세종시"),
    GYEONGGI(31, "경기도"),
    GANGWON(32, "강원도"),
    CHUNGBUK(33, "충청북도"),
    CHUNGNAM(34, "충청남도"),
    GYEONGBUK(35, "경상북도"),
    GYEONGNAM(36, "경상남도"),
    JEONBUK(37, "전라북도"),
    JEONNAM(38, "전라남도"),
    JEJU(39, "제주도")
}

// 서울의 구 (AreaCode_1)
enum class AreaCode_1(val districtCode: Int, val districtName: String) {
    GANGNAM(1, "강남구"),
    GANGDONG(2, "강동구"),
    GANGBUK(3, "강북구"),
    GANGSEO(4, "강서구"),
    GWANAK(5, "관악구"),
    GWANGJIN(6, "광진구"),
    GURO(7, "구로구"),
    GEUMCHEON(8, "금천구"),
    NOWON(9, "노원구"),
    DOBONG(10, "도봉구"),
    DONGDAEMUN(11, "동대문구"),
    DONGJAK(12, "동작구"),
    MAPO(13, "마포구"),
    SEODAEMUN(14, "서대문구"),
    SEOCHO(15, "서초구"),
    SEONGDONG(16, "성동구"),
    SEONGBUK(17, "성북구"),
    SONGPA(18, "송파구"),
    YANGCHEON(19, "양천구"),
    YEONGDEUNGPO(20, "영등포구"),
    YONGSAN(21, "용산구"),
    EUNPYEONG(22, "은평구"),
    JONGNO(23, "종로구"),
    JUNG(24, "중구"),
    JUNGRANG(25, "중랑구")
}

// 인천의 구 (AreaCode_2)
enum class AreaCode_2(val districtCode: Int, val districtName: String) {
    GANGHWA(1, "강화군"),
    GYEYANG(2, "계양구"),
    MICHUHOL(3, "미추홀구"),
    NAMDONG(4, "남동구"),
    DONG(5, "동구"),
    BUPYEONG(6, "부평구"),
    SEO(7, "서구"),
    YEONSU(8, "연수구"),
    ONGJIN(9, "옹진군"),
    JUNG(10, "중구")
}

// 대전의 구 (AreaCode_3)
enum class AreaCode_3(val districtCode: Int, val districtName: String) {
    DAEDEOK(1, "대덕구"),
    DONG(2, "동구"),
    SEO(3, "서구"),
    YUSEONG(4, "유성구"),
    JUNG(5, "중구")
}

// 대구의 구 (AreaCode_4)
enum class AreaCode_4(val districtCode: Int, val districtName: String) {
    NAM(1, "남구"),
    DALSEO(2, "달서구"),
    DALSEONG(3, "달성군"),
    DONG(4, "동구"),
    BUK(5, "북구"),
    SEO(6, "서구"),
    SUSEONG(7, "수성구"),
    JUNG(8, "중구")
}

// 광주의 구 (AreaCode_5)
enum class AreaCode_5(val districtCode: Int, val districtName: String) {
    GWANGSAN(1, "광산구"),
    DONG(2, "동구"),
    BUK(3, "북구"),
    SEO(4, "서구"),
    NAM(5, "남구")
}

// 부산의 구 (AreaCode_6)
enum class AreaCode_6(val districtCode: Int, val districtName: String) {
    GANGSEO(1, "강서구"),
    GEUMJEONG(2, "금정구"),
    GIJANG(3, "기장군"),
    NAM(4, "남구"),
    DONG(5, "동구"),
    DONGNAE(6, "동래구"),
    BUSANJIN(7, "부산진구"),
    BUK(8, "북구"),
    SASANG(9, "사상구"),
    SAHA(10, "사하구"),
    SEO(11, "서구"),
    SUYEONG(12, "수영구"),
    YEONJE(13, "연제구"),
    YEONGDO(14, "영도구"),
    JUNG(15, "중구"),
    HAEUNDAE(16, "해운대구")
}

// 울산의 구 (AreaCode_7)
enum class AreaCode_7(val districtCode: Int, val districtName: String) {
    JUNG(1, "중구"),
    NAM(2, "남구"),
    DONG(3, "동구"),
    BUK(4, "북구"),
    ULJU(5, "울주군")
}

// 세종특별자치시 (AreaCode_8)
enum class AreaCode_8(val districtCode: Int, val districtName: String) {
    SEJONG(1, "세종특별자치시")
}

// 경상북도 (AreaCode_35)
enum class AreaCode_35(val districtCode: Int, val districtName: String) {
    GYEONGSAN(1, "경산시"),
    GYEONGJU(2, "경주시"),
    GORYEONG(3, "고령군"),
    GUMI(4, "구미시"),
    GIMCHEON(6, "김천시"),
    MUNGYEONG(7, "문경시"),
    BONGHWA(8, "봉화군"),
    SANGJU(9, "상주시"),
    SEONGJU(10, "성주군"),
    ANDONG(11, "안동시"),
    YEONGDEOK(12, "영덕군"),
    YEONGYANG(13, "영양군"),
    YEONGJU(14, "영주시"),
    YOUNGCHEON(15, "영천시"),
    YECHEON(16, "예천군"),
    ULLEUNG(17, "울릉군"),
    ULJIN(18, "울진군"),
    UISEONG(19, "의성군"),
    CHEONGDO(20, "청도군"),
    CHEONGSONG(21, "청송군"),
    CHILGOK(22, "칠곡군"),
    POHANG(23, "포항시")
}

// 경상남도 (AreaCode_36)
enum class AreaCode_36(val districtCode: Int, val districtName: String) {
    GEOJE(1, "거제시"),
    GEOCHEONG(2, "거창군"),
    GOSEONG(3, "고성군"),
    GIMHAE(4, "김해시"),
    NAMHAE(5, "남해군"),
    MASAN(6, "마산시"),
    MIRYANG(7, "밀양시"),
    SACHEON(8, "사천시"),
    GANJEONG(9, "간청군"),
    YANGSAN(10, "양산시"),
    UIREONG(12, "의령군"),
    JINJU(13, "진주시"),
    JINHAE(14, "진해시"),
    CHANGNYEONG(15, "창녕군"),
    CHANGWON(16, "창원시"),
    TONGYEONG(17, "통영시"),
    HADONG(18, "하동군"),
    HAMAN(19, "함안군"),
    HAMYANG(20, "함양군"),
    HAPCHEON(21, "합천군")
}

// 전북특별자치도 (AreaCode_37)
enum class AreaCode_37(val districtCode: Int, val districtName: String) {
    GOCHANG(1, "고창군"),
    GUNSAN(2, "군산시"),
    GIMJE(3, "김제시"),
    NAMWON(4, "남원시"),
    MUJU(5, "무주군"),
    BUAN(6, "부안군"),
    SUNCHANG(7, "순창군"),
    WANJU(8, "완주군"),
    IKSAN(9, "익산시"),
    IMSIL(10, "임실군"),
    JANGSU(11, "장수군"),
    JEONJU(12, "전주시"),
    JEOJU(13, "정읍시"),
    JINAN(14, "진안군")
}

// 전라남도 (AreaCode_38)
enum class AreaCode_38(val districtCode: Int, val districtName: String) {
    GANGJIN(1, "강진군"),
    GOHEUNG(2, "고흥군"),
    GOKSEONG(3, "곡성군"),
    GWANGYANG(4, "광양시"),
    GURYE(5, "구례군"),
    NAKSAN(6, "나주시"),
    DAMYANG(7, "담양군"),
    MOKPO(8, "목포시"),
    MUAN(9, "무안군"),
    BOSEONG(10, "보성군"),
    SUNCHEON(11, "순천시"),
    SINAN(12, "신안군"),
    YEOSU(13, "여수시"),
    YEONGGWANG(16, "영광군"),
    YEONGAM(17, "영암군"),
    WANDO(18, "완도군"),
    JANGSEONG(19, "장성군"),
    JANGHEUNG(20, "장흥군"),
    JINDO(21, "진도군"),
    HAMPEONG(22, "함평군"),
    HAENAM(23, "해남군"),
    HWASUN(24, "화순군")
}

// 제주도 (AreaCode_39)
enum class AreaCode_39(val districtCode: Int, val districtName: String) {
    NAM_JEJU(1, "남제주군"),
    BUK_JEJU(2, "북제주군"),
    SEOGWIPO(3, "서귀포시"),
    JEJU(4, "제주시")
}

// 로그인 결과
enum class LoginResult(val number:Int, val str:String){
    LOGIN_RESULT_SUCCESS(1, "로그인 성공"),
    LOGIN_RESULT_ID_NOT_EXIST(2, "존재하지 않는 아이디"),
    LOGIN_RESULT_PASSWORD_INCORRECT(3, "잘못된 비밀번호"),
    LOGIN_RESULT_SIGN_OUT_MEMBER(4, "탈퇴한 회원"),
}

// 사용자 상태
enum class UserState(val number:Int, val str:String){
    // 정상
    USER_STATE_NORMAL(1, "정상"),
    // 탈퇴
    USER_STATE_SIGN_OUT(2, "탈퇴")
}
enum class MainScreenName{
    // 시작 화면
    MAIN_SCREEN_START,
    // 로그인 화면
    MAIN_SCREEN_USER_LOGIN,
    // 회원 가입 1 화면, 회원 정보 입력 받기
    MAIN_SCREEN_USER_SIGN_UP_STEP1,
    // 회원 가입 2 화면, 프로필 이미지 입력 받기
    MAIN_SCREEN_USER_SIGN_UP_STEP2,
    // 회원 가입 3 화면, 카톡 가입 후 닉네임 받는 화면
    MAIN_SCREEN_USER_SIGN_UP_STEP3,
    // 검색 화면
    MAIN_SCREEN_SEARCH,
    //검색 결과 화면
    MAIN_SCREEN_SEARCH_RESULT,
    // 프로필 편집 화면
    MAIN_SCREEN_USER_INFO,
    // 내 여행 화면
    MAIN_SCREEN_MY_TRIP,
    // 내 저장 화면
    MAIN_SCREEN_MY_INTERESTING,
    // 내 리뷰 화면
    MAIN_SCREEN_MY_REVIEW,
    // 내 여행기 화면
    MAIN_SCREEN_MY_TRIP_NOTE,
    // 상세 화면
    MAIN_SCREEN_DETAIL,
    // 구글 지도 화면
    MAIN_SCREEN_GOOGLE_MAP,
    // 리뷰 작성 화면
    MAIN_SCREEN_DETAIL_REVIEW_WRITE,
    // 리뷰 수정 화면
    MAIN_SCREEN_DETAIL_REVIEW_MODIFY,
    // 인기 도시 화면
    MAIN_SCREEN_POPULAR_CITY,


}

enum class BotNavScreenName{
    // 홈 화면
    BOT_NAV_SCREEN_HOME,
    // 일정 화면
    BOT_NAV_SCREEN_SCHEDULE,
    // 여행기 화면
    BOT_NAV_SCREEN_TRIP_NOTE,
    // 내정보
    BOT_NAV_SCREEN_MY_INFO,
}

enum class ScheduleScreenName{
    // 일정 추가 화면
    SCHEDULE_ADD_SCREEN,
    // 일정 지역 선택 화면
    SCHEDULE_CITY_SELECT_SCREEN,
    // 일정 상세 화면
    SCHEDULE_DETAIL_SCREEN,
    // 랜덤 일정 상세 화면
    SCHEDULE_DETAIL_RANDOM_SCREEN,
    // 일정 항목 선택 화면
    SCHEDULE_SELECT_ITEM_SCREEN,
    // 함께 하는 친구 목록 화면
    SCHEDULE_DETAIL_FRIENDS_SCREEN,
    // 일정 항목 후기 화면
    SCHEDULE_ITEM_REVIEW_SCREEN,
}

enum class TripNoteScreenName{
    // 여행기 상세 화면
    TRIP_NOTE_DETAIL,
    // 여행기 작성 화면
    TRIP_NOTE_WRITE,
    // 여행기 일정 불러오기 화면
    TRIP_NOTE_SCHEDULE,
    // 타인 일정 리스트 보기 화면
    TRIP_NOTE_OTHER_SCHEDULE,
    // 일정 담을 여행 선택하기 화면
    TRIP_NOTE_SELECT_DOWN,
}

enum class RotateMapScreenName{
    // 지도 룰렛 화면
    ROTATE_MAP_SCREEN,

}

enum class RouletteScreenName{
    ROULETTE_GRAPH, // ✅ 추가된 네비게이션 그래프 이름
    // 도시 룰렛 화면
    ROULETTE_CITY_SCREEN,
    // 룰렛 도시 항목 추가 화면
    ROULETTE_CITY_SELECT_SCREEN,
    // 룰렛 일정 항목 화면
    ROULETTE_ITEM_SCREEN,
    // 룰렛 일정 항목 추가 화면
    ROULETTE_ITEM_SELECT_SCREEN,
}

enum class TripItemCat2(val catCode: String, val catName: String) {
    // 🌍 자연 및 관광 자원
    NATURE_TOURISM("A0101", "자연관광지"),
    TOURISM_RESOURCE("A0102", "관광자원"),

    // 🏛️ 역사 및 문화 관광지
    HISTORICAL_TOURISM("A0201", "역사관광지"),
    RESORT_TOURISM("A0202", "휴양관광지"),
    EXPERIENCE_TOURISM("A0203", "체험관광지"),
    INDUSTRIAL_TOURISM("A0204", "산업관광지"),
    ARCHITECTURAL_STRUCTURE("A0205", "건축/조형물"),
    CULTURAL_FACILITY("A0206", "문화시설"),
    FESTIVAL("A0207", "축제"),
    PERFORMANCE_EVENT("A0208", "공연/행사"),

    // 🏅 레포츠 관광
    SPORTS_OVERVIEW("A0301", "레포츠소개"),
    LAND_SPORTS("A0302", "육상 레포츠"),
    WATER_SPORTS("A0303", "수상 레포츠"),
    AIR_SPORTS("A0304", "항공 레포츠"),
    COMPOSITE_SPORTS("A0305", "복합 레포츠");

    companion object {
        // ✅ 코드 값으로 enum 찾기
        fun fromCodeTripItemCat2(code: String): TripItemCat2? {
            return values().find { it.catCode == code }
        }
    }
}

enum class RestaurantItemCat3(val catCode: String, val catName: String) {
    // 🍽️ 음식점 분류
    KOREAN_FOOD("A05020100", "한식"),
    WESTERN_FOOD("A05020200", "서양식"),
    JAPANESE_FOOD("A05020300", "일식"),
    CHINESE_FOOD("A05020400", "중식"),
    UNIQUE_RESTAURANT("A05020700", "이색음식점"),
    CAFE_TEA_HOUSE("A05020900", "카페/전통찻집"),
    CLUB("A05021000", "클럽");

    companion object {
        // ✅ 코드 값으로 enum 찾기
        fun fromCodeRestaurantItemCat3(code: String): RestaurantItemCat3? {
            return values().find { it.catCode == code }
        }
    }
}

enum class AccommodationItemCat3(val catCode: String, val catName: String) {
    // 🏨 숙박 시설 유형
    TOURIST_HOTEL("B02010100", "관광호텔"),
    CONDOMINIUM("B02010500", "콘도미니엄"),
    YOUTH_HOSTEL("B02010600", "유스호스텔"),
    PENSION("B02010700", "펜션"),
    MOTEL("B02010900", "모텔"),
    HOMESTAY("B02011000", "민박"),
    GUEST_HOUSE("B02011100", "게스트하우스"),
    HOME_STAY("B02011200", "홈스테이"),
    SERVICED_RESIDENCE("B02011300", "서비스드레지던스"),
    HANOK("B02011600", "한옥");

    companion object {
        // ✅ 코드 값으로 enum 찾기
        fun fromCodeAccommodationItemCat3(code: String): AccommodationItemCat3? {
            return values().find { it.catCode == code }
        }
    }
}

enum class PopularCityTap(val num:Int, val str:String){
    POPULAR_CITY_TAP_HOME(0, "홈"),
    POPULAR_CITY_TAP_ATTRACTION(1, "관광지"),
    POPULAR_CITY_TAP_RESTAURANT(2, "식당"),
    POPULAR_CITY_TAP_ACCOMMODATION(3, "숙소"),
    POPULAR_CITY_TAP_TRIP_NOTE(4,"여행기")
}
