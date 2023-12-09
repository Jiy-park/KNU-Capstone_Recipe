# KNU CAPSTONE DESIGN -Talking Recipe-
# 강원대학교 캠스톤 디자인 제출용 애플리케이션.

# 토킹 레시피 설명
핸즈 프리 레시피 어플리케이션.  
토킹 레시피는 Text To Speech(이하 TTS), Speech To Text(이하 STT) 기술을 활용하여 요리 중 손을 사용할 수 없는 상황에서도 음성을 통해 조작해 편리한 환경을 제공해준다.  
토킹 레시피는 TTS를 통해 레시피의 과정을 읽어주며, 사용자의 설정에 따라 on/off 또는 속도, 목소리의 톤 등을 설정할 수 있다.
또한 STT를 통해 사용자의 음성을 받아들여 레시피의 이전, 다음 과정으로 이동이 가능하며 정지, 다시 읽기가 가능하다.  
조리식품의 레시피 DB API 를 통해 기본 탑재 레시피 3000개를 포함하며, 자체 로그인 또는 구글 로그인 기능을 통해 자신만의 레시피를 공유할 수 있다.
식품 영양 성분 분석 API를 통해 레시피의 영양 성분을 분석해 알려줄 수 있다.  
[소개 영상](https://youtu.be/EyVXPPh2V8Y?si=fq5TYXPU6-FZHi34)

<details>
  <summary>
    
  # 토킹 레시피 소개 이미지
  
  </summary>
  
![talking_recipe_intro](https://github.com/Jiy-park/KNU-Capstone_Recipe/assets/79889934/646f9dee-4bb6-4106-b329-61f57aabc775)

</details>

<details>
  <summary>
    
# 토킹레시피 화면 구성
    
  </summary>
  
  ![talking_recipe_flow](https://github.com/Jiy-park/KNU-Capstone_Recipe/assets/79889934/1b4d3817-3c52-4eca-8e2c-a1e7a53be846)
  
</details>

# 담당 역할
- 4인 1팀 팀장
- 데이터베이스 설계 개발
- Firebase를 통한 데이터 관리
- 애플리케이션 전반적인 구현
- 애플리케이션 내 레시피, 유저 검색 및 필터를 적용한 검색 구현
- 각 팀원의 개발 파트 프로젝트 파일에 통합 (Java->Kotilin 코드 변환) 

# 사용 기술
- Android
- Kotlin
- Java
- Firebase-Realtime Database
- Firebase-Storage
- Firebase-Authentication
- Retrofit2

# 사용 API
- [조리식품 레시피 DB API](https://www.foodsafetykorea.go.kr/api/newDatasetDetail.do)
- [식품 영양 성분 API](https://api.edamam.com/)
- [영양 선분 번역용 NAVER PAPAGO API](https://developers.naver.com/products/papago/nmt/nmt.md)
