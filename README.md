<div align=center>

<img style="border-radius: 10%;" src="./images/introduction.png" alt="NotFound">


## 세상 모든 반려동물을 한 자리에서! 왈왈🐶

<b>반려동물과 일상의 추억을 기억하고 싶으신적 없으신가요? <br/>
왈왈은 반려동물과 함께할 수 있는 소소한 미션을 통해 추억을 기록하고, 다른 반려동물의 일상을 발견할 수 있는
펫 커뮤니티 서비스입니다 </b>

<br/>

[![App Store](https://img.shields.io/badge/AppStore-0D96F6.svg?style=flat&logo=app-store&logoColor=white&link=https://play.google.com/store/apps/details?id=com.dpm.spot)](https://apps.apple.com/kr/app/%EC%99%88%EC%99%88/id6553981069)
[![Instagram](http://img.shields.io/badge/Instagram-E4405F?style=flat&logo=instagram&logoColor=white&link=https://www.instagram.com/walwal._.official/)](https://www.instagram.com/walwal._.official/)
<br>
[![서비스 소개](http://img.shields.io/badge/서비스_소개-%23000000?style=flat&logo=notion&logoColor=white&link=https://yapp-workspace.notion.site/5-8a385156703047aabf1e3706f4753cc6?pvs=4)](https://yapp-workspace.notion.site/5-8a385156703047aabf1e3706f4753cc6?pvs=4)
[![서버파트 개발 로그](http://img.shields.io/badge/개발_로그-12100E?style=flat&logo=medium&logoColor=white&link=https://medium.com/@olderstonebed)](https://medium.com/@olderstonebed)
[![메이커 로그](http://img.shields.io/badge/메이커_로그-0000FF?style=flat&logo=Pinboard&logoColor=white&link=https://dis.qa/e6J)](https://dis.qa/e6J)

</div>

<br/>

## ✨ IA(Information Architecture)
<img src="./images/IA.png">

## 📌 Package Architecture
왈왈 서버 패키지 아키텍처는 레이어드 아키텍처로 구성하였습니다. <br/> 
위 소개한 IA에서 큰 규모의 기능이 정의되지 않아, 당장은 클린 아키텍처 또는 헥사고날 아키텍처의 도입이 필요하지 않다고 판단하였습니다. 
<br/>

```
// 프로젝트 전체 구조
├── src.main.java.com.depromeet.stonebed
│   ├── domain
│   ├── global
│   ├── infra

// domain 패키지는 비즈니스 로직을 담당하며 레이어드 아키텍처 구성
│   ├── domain
│   └── auth
│   └── common
│   └── fcm
│   └── feed
│   └── follow
│   └── image
│   └── member
│   └── mission
│   └── missionHistory
│   └── missionRecordBoost
│   └── sqs

// global 패키지는 전역 설정 담당
│   ├── global
│   └── annotation
│   └── config
│   └── common
│   └── error
│   └── filter
│   └── interceptor
│   └── security
│   └── util

// infra는 외부 연동 및 클라우드 구성 담당
│   ├── infra
│   └── config
│   └── properties
// ...
```

## 💻 Tech Stack
<img src="./images/tech-stack.png">

## 🏛️ System Architecture
<img src="./images/cloud-architecture.png">

### 📦 CI/CD
- Github Actions
- Docker Hub
- Docker compose

## 🖥️ Monitoring
모니터링은 Prometheus를 사용하여 서버의 상태에 대한 메트릭 수집과, Grafana를 사용하여 모니터링 대시보드를 구성하였습니다. <br/>
또한, MySQL에 대한 SlowQuery 발생 시 Slack Webhook을 통한 알림 전송을 Lambda 함수로 작성하여 구성하였습니다.<br/>
Loki, Promtail을 사용하여 로그 수집 및 저장을 구성하였습니다.

<div align="center">

<h2> 🧑‍💻 Server Developer </h2>
<div style="display: inline-block;">

<table>
  <tr>
    <th>차윤범</th>
    <th>노관옥</th>
    <th>박윤찬</th>
  </tr>
  <tr>
    <td><a href="https://github.com/char-yb"><img style="border-radius: 20%;" src="https://avatars.githubusercontent.com/u/68099546?v=4" width=100px alt="_" /></a></td>
    <td><a href="https://github.com/kwanok"><img src="https://avatars.githubusercontent.com/u/61671343?v=4" width=100px alt="_" /></a></td>
    <td><a href="https://github.com/dbscks97"><img style="border-radius: 20%;" src="https://avatars.githubusercontent.com/u/75676309?v=4" width=100px alt="_" /></a></td>
  </tr>
  <tr>
    <td><strong>Server</strong> (Leader)</td>
    <td><strong>Server</strong></td>
    <td><strong>Server</strong></td>
  </tr>
</table>

</div>

</div>


<div align=center>
    <img src="./images/app_qr.png" width="570">
</div>

### 
