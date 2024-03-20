### 🙌🏻 서비스명 : 모임 (MOIM)

### 😁 팀원
 
|<img src="img/bellwin_image.jpg" height="150">|<img src="img/jaeseok_image.jpg" height="150">|<img src="" height="150">|<img src="img/eunji_image.jpg" height="150">|
|:---:|:---:|:---:|:---:|
| [한종승(팀장)](https://github.com/BellWin98) | [신재석(팀원)](https://github.com/MrKeeplearning) | [배소영(팀원)](https://github.com/qoth-0) | [장은지(팀원)](https://github.com/Jang-Eun-Ji) |
</div>

<br>

## 📝 서비스 개요

### 1. 소개

> MOIM을 통해 일정을 우선순위에 따라 관리할 수 있고, 모임을 효율적으로 개설할 수 있습니다.

<br>

### 2. 목적 및 필요성
 - 등록된 일정들을 중요도와 긴급도에 따라 분류하고 싶다!
 - 사람들의 모임 시간을 조율하기가 너무 어렵다!

<br>

### 3. 주요 기능

- **모임 기능**
  - 호스트가 모임을 개설하면, 지정된 게스트들에게 모임 참여 요청 알림이 전송됩니다.
  - 호스트는 희망하는 모임 일정과 모임 시간의 범위를 설정할 수 있습니다.
  - 게스트들이 모임을 수락하면 일정 자동 추천 알고리즘을 통해 게스트들의 캘린더를 분석해서 모임 가능한 일정 중 가장 빠른 날짜와 시간을 추천합니다.

- **일정 등록/조회 기능**
  - 사용자는 중요도/긴급도에 따라 일정을 등록할 수 있습니다.
  - 등록된 일정을 4분면 매트릭스로 조회할 수 있습니다.
  - 4분면에 표시된 일정을 다른 분면으로 옮길 수 있습니다.
  - 일정을 월/주/일 단위로 조회할 수 있습니다.
 
- **알림 기능**
  - 등록된 일정이 임박했을 때, 일정 등록 시 설정했던 알림 시간에 알림을 전송할 수 있습니다. (10분 전, 1시간 전, 1일 전 등)
  - 모임 일정 자동 추천 알고리즘을 통해 모임 가능 일정이 추천되면 호스트와 게스트에게 모임 확정 알림이 전송됩니다.
 
- **키워드 검색 기능**
  - 사용자는 키워드(제목+메모)을 기준으로 일정을 검색할 수 있습니다.
  - 키워드 검색을 통해 일정 목록과 상세 내용을 확인할 수 있습니다.

<br>

## 📝 요구 사항 정의서
![스크린샷 2024-03-20 112313](https://github.com/HanHwa-Team1-Final-Project/Team1-BE/assets/60949121/9e2640bf-e4fc-4898-97f8-affe643ea9d0)

[요구사항 정의서 바로가기](https://docs.google.com/spreadsheets/d/e/2PACX-1vSTZa75qmkYYk5VZ6VWxN_7RTGlplStcqsQiXdpS9FOU4cicxamdVKAI-RX7qicB0TpfyUe9uzkrLFd/pubhtml?gid=0&single=true)

<br>

## ⚙️ ERD 
![Moim_erd](https://github.com/HanHwa-Team1-Final-Project/Team1-BE/assets/60949121/12dec41b-ceca-43df-bf21-18614d57c160)



<br>

## 💻 기술 스택

📱 **Front-End :** <img src="https://img.shields.io/badge/Vue-4FC08D.svg?&style=flat-square&logo=Vuedotjs&logoColor=white">

📀 **Back-end :** 
<img src="https://img.shields.io/badge/Java17-007396.svg?&style=flat-square&logo=Java&logoColor=white">
<img src="https://img.shields.io/badge/SpringBoot-6DB33F.svg?&style=flat-square&logo=SpringBoot&logoColor=white">
<img src="https://img.shields.io/badge/SpringDataJpa-6DB33F.svg?&style=flat-square&logo=SpringDataJpa&logoColor=white">
<img src="https://img.shields.io/badge/SpringSecurity-6DB33F.svg?&style=flat-square&logo=SpringSecurity&logoColor=white">
<img src="https://img.shields.io/badge/Gradle-02303A.svg?&style=flat-square&logo=Gradle&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000.svg?&style=flat-square&logo=jsonwebtokens&logoColor=white">

💾 **Infra & DB :**
<img src="https://img.shields.io/badge/MariaDB-4479A1?style=flat-square&logo=MariaDB&logoColor=white">
<img src="https://img.shields.io/badge/AmazonEC2-FF9900?style=flat-square&logo=AmazonEC2&logoColor=white">
<img src="https://img.shields.io/badge/AmazonRDS-527FFF?style=flat-square&logo=AmazonRDS&logoColor=white">
<img src="https://img.shields.io/badge/AmazonS3-569A31?style=flat-square&logo=AmazonS3&logoColor=white">
<img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white">

🚀 **CI/CD :**
<img src="https://img.shields.io/badge/GithubActions-2088FF?style=flat-square&logo=GithubActions&logoColor=white">
<img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white">
<img src="https://img.shields.io/badge/NGINX-009639?style=flat-square&logo=NGINX&logoColor=white">

<br>

🏠 기술 사용 이유

| **Java 17** | **Spring Boot** |
| --- | --- |
| 설명1 | 설명2 |

| **Spring Data Jpa** | **Redis** |
| --- | --- |
| 설명3 | 설명4 |

<br>

## 🔊 협업 툴
![github](https://img.shields.io/badge/Github-181717.svg?&style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000.svg?&style=for-the-badge&logo=Notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B.svg?&style=for-the-badge&logo=Slack&logoColor=white)
![intellijidea](https://img.shields.io/badge/IntelliJidea-000000.svg?&style=for-the-badge&logo=intellijidea&logoColor=white)
![visualstudiocode](https://img.shields.io/badge/VScode-007ACC.svg?&style=for-the-badge&logo=visualstudiocode&logoColor=white)

<br>

## 🗓️ WBS
![스크린샷 2024-03-20 112909](https://github.com/HanHwa-Team1-Final-Project/Team1-BE/assets/60949121/480031b5-b299-478a-9496-92b03e669301)

[WBS 바로가기](https://docs.google.com/spreadsheets/d/e/2PACX-1vSTZa75qmkYYk5VZ6VWxN_7RTGlplStcqsQiXdpS9FOU4cicxamdVKAI-RX7qicB0TpfyUe9uzkrLFd/pubhtml?gid=1560823417&single=true)

<br>

## API 명세서
여기에 api 명세서 이미지 넣기

<br>

## 📄 커밋 컨벤션 및 Github Flow

| 이름 | 설명 |
| --- | --- |
| feat | 기능 커밋 |
| fix | 오류 수정 커밋 |
| refactor | 패키지 구조 수정 |
| test | 테스트 커밋 |
| chore | 그 외 자잘한 수정 |

![Github Flow](https://cdn.hashnode.com/res/hashnode/image/upload/v1668070000889/rvf5Hx764.png)

<br>

## 🚀 시스템 아키텍쳐

여기에 아키텍쳐 이미지 삽입

<br>


