프론트 전달 내용은 이렇게 정리하면 됩니다.

**API 변경 여부**
- 기존 API 그대로 사용하면 됩니다.
- URL 변경 없음: `POST /api/emergency-jurisdictions/resolve`
- 요청/응답 필드 변경 없음.

**요청 형식**
```json
{
  "postcode": "04123",
  "address": "서울특별시 마포구 월드컵로 1",
  "addressDetail": "101동 101호"
}
```

**응답 형식**
```json
{
  "success": true,
  "data": {
    "district": "마포구",
    "jurisdiction": "마포소방서",
    "stationName": "마포소방서",
    "centerName": "공덕119안전센터",
    "stationAddress": "서울특별시 마포구 만리재옛길 82 (공덕동)",
    "latitude": null,
    "longitude": null
  },
  "message": "요청이 성공했습니다."
}
```

**프론트에서 참고할 점**
- 주소 입력 후 기존처럼 `resolve` API를 호출하면 됩니다.
- `district`, `jurisdiction` 값을 회원가입/시설 등록 요청에 그대로 넣으면 됩니다.
- 일부 지역은 기존 테스트/샘플과 대표 센터가 달라질 수 있습니다.
  - 예: 부산 해운대구 → `기장소방서`
  - 예: 제주 제주시 → `동부소방서`
- 세종 주소는 `district`가 `세종시`로 내려옵니다.
- `latitude`, `longitude`는 현재 데이터에 없어서 `null`일 수 있습니다. UI에서 필수값으로 처리하지 않으면 됩니다.

**에러 처리**
- 매칭 실패 시 기존과 동일하게 `404`가 내려옵니다.
```json
{
  "success": false,
  "error": {
    "code": "EMERGENCY_JURISDICTION_NOT_FOUND",
    "message": "..."
  }
}
```

