# BookChat - Android
<img width="100%" height="100%" alt="bookchat_introduce" src="https://github.com/Kova700/BookChat_Android/assets/81726145/8a61b1cc-6cff-4320-bddb-83234cf8d4e5"> <br>

### What is BookChat? 
- 책의 난이도가 자신과 맞지 않는 사용자

- 책의 특정 부분이 이해되지 않는 사용자

- 책에 대한 자신의 생각을 다른 사람들과 나누고 싶은 사용자

- 한 책을 같이 스터디 할 수 있는 환경이 필요한 사용자

이런 사용자들이 모여 책의 특정 부분에 대해 독서 토론 혹은 질문을 채팅으로 나눌 수 있는 플랫폼

***
### Challenge Goal
- Coroutine을 이용한 비동기작업 (도전)
- 적절한 애니메이션 UI 제작 (도전) 
- MVVM 모델 (도전)
- 기기 OS버전별 분기 작업 (도전)
- Clean한 Code (도전)
- 오직 터미널을 이용한 Git 사용 (도전)
- Figma를 이용한 앱 디자인 (도전)
  - 도전했다가 디자이너님 영입 후 디자인 수정

***
### Commit Message
* **feat** (feature)                              : 새로운 기능에 대한 커밋
* **fix** (bug fix)                               : 버그 수정에 대한 커밋
* **config** (configuration)                      : 설정 정보에 대한 커밋
* **docs** (documentation)                        : 문서 수정에 대한 커밋
* **style** (formatting, missing semi colons, …)  : 코드 스타일 혹은 포맷 등에 관한 커밋
* **refactor**                                    : 코드 리팩토링에 대한 커밋
* **test** (when adding missing tests)            : 테스트 코드 수정에 대한 커밋
* **chore** (maintain)                            : 그 외 자잘한 수정에 대한 커밋

## *****Tech Stack***** 
| 구분 | Tech |
|:---|:---------------------------------------------------------------------------|
| Language | Kotlin |
| Architecture | MVVM, Clean-Architecture, Multi-Module |
| Network | Retrofit2 |
| DataBase | Room, DataStore |
| Asynchronous | Coroutine, Flow |
| DI | Dagger-Hilt |
| Serialization | Kotlinx-Serialization |
| Build |Version Catalog, Precompiled-Script-Plugin   |
| ETC | OAuth, OIDC, FCM, Glide, [Stomp](https://stomp.github.io), WebSocket, [Krossbow](https://github.com/joffrey-bion/krossbow) |

</br>

## *****Module Dependency Graph***** 
### Abstract
![bookchat_abstract_dependency_graph](https://github.com/user-attachments/assets/eaefd499-cb17-41a7-92d6-e9f416a12374)

</br>

### Detail ([크게보기](https://github.com/user-attachments/assets/c77bf141-cb87-45ad-97d2-b57ece050535))
![project dot](https://github.com/user-attachments/assets/c77bf141-cb87-45ad-97d2-b57ece050535)

</br>

***
### ScreenShots
<p align="center">
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/9b918da2-c474-4400-b6dd-b70472700ad3" width="24%"/>
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/a9475915-ca4f-4e9b-acd3-868dcba3446a" width="24%"/>
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/0509f162-9252-4e29-98c9-499ef42e0037" width="24%"/>
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/a35cf3a7-a974-47f7-8875-cadabd4bde11" width="24%"/>
</p>

<p align="center">
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/9a73096e-d918-4efd-8703-f132ee7069aa" width="25%"/>
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/3556a892-812c-41c3-ad05-b93c2a98f9a9" width="25%"/>
<img src="https://github.com/Kova700/BookChat_Android/assets/81726145/fb61738a-00c2-45cf-bea5-68b7aca0a1b6" width="25%"/>
</p>

</br>

## *****Primary Feature summary***** 
- WebSocket을 이용한 다수 사용자가 참여할 수 있는 오픈 채팅방
  - 소켓 끊김 사이에 발생한 채팅방 변경사항 및 채팅 동기화 기능
  - 전송 실패한 채팅 자동 재전송 기능
  - 방장/ 부방장 권한 위임 및 박탈 기능
  - 유저 강퇴 기능
  - ID 매핑 방식을 통한 서비스 내 유저 닉네임 변경 시 모든 메시지에 실시간 반영
  - RoomDB를 이용한 채팅방 및 채팅 Offline Data 구성
  - 채팅 내 상하 방향 Paging 기능
  - 주요했던 채팅을 이미지로 저장할 수 있는 채팅 캡처 기능
- FCM을 이용한 채팅 Notification
- 단일 기기만 허용하는 서비스 구조
  - FCM을 이용한 강제 로그아웃기능 
- OAuth2.0를 이용한 Google/ Kakao 소셜로그인
- JWT 토큰을 이용한 유저 인가 (Authorization)
  - OIDC를 통해 가져온 유저정보를 바탕으로 만든 서비스 자체 JWT 토큰
  - OkHttp3의 Http Interceptor를 이용한 토큰 자동 갱신
  - KeyStore에 저장된 Key를 이용한 토큰 대칭키 암호화
- 도서, 채팅방 검색 기능
- 도서의 독서상태를 기억할 수 있는 서재 기능 
  - ViewPager2를 이용한 독서예정, 독서중, 독서완료 각 페이지 구성
  - Coroutine을 활용한 LongClick Swipe Animation
  - BottomSheet를 이용한 도서의 읽고 있던 페이지를 기록할 수 있는 책갈피 기능
- 도서 별점 등록 및 독후감 기능
- 독서 중에 생겼던 고민을 기록할 수 있는 고민기록 기능
  - ItemTouchHelper를 이용한 Item Swipe Animation
- 서버 점검 시 Remote-Config를 이용한 일시적 서비스 접속 제한 기능

</br>

## *****Video***** 
로그인
|화면|
|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/be2e4cfb-5b13-45e7-92f3-9bb5c01d5d14">|

회원가입
|닉네임 중복 체크|이미지 선택|독서 취향 선택|
|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/d83e36a5-7634-4a38-b775-e0df5f1620a3">|<img width="240" src="https://github.com/user-attachments/assets/87f96fe0-25de-44fe-9517-87a1cec198f9">|<img width="240" src="https://github.com/user-attachments/assets/8698dc39-2031-49f1-991d-12d7d9d3ef27">|

메인 화면
|화면|
|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/3910f47d-94a9-414c-a13e-0db78bc83511">|

서재 [독서 예정]
|[독서예정] 삭제|[독서중]으로 이동|
|:-----:|:-----:|
|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/c0aa4b27-a54c-4f0c-a4e5-40d86e65457f">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/78fcdf91-7ebb-4103-a7d7-a6b7c29e3db2">|

서재[독서 중]
|책갈피|[독서중] 삭제|[독서완료]로 이동|[고민기록]으로 이동
|:-----:|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/127036aa-3593-4806-8bb1-c3e8bebeceba">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/01625726-42e3-49fc-823d-11c88dbdf540">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/d1803821-4c99-42a1-9550-521d8b82d79c">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/3748c0dc-c66e-49f0-b5b8-cfcf5386208b">

서재[독서 완료]
|[독서 완료] 삭제|[고민기록]으로 이동|[독후감]으로 이동 </br>(작성된 독후감 X)|[독후감]으로 이동 </br>(작성된 독후감 O)|
|:-----:|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/624d311d-571e-4d86-ab72-9810a3adfb7a">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/4ef42617-bf58-42c7-8864-2094b171eb19">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/948b7944-9358-483e-97b3-34ede765b33e">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/ae9fd2e6-edc9-4e2b-8be6-78713e9640ae">

독후감
|작성|작성 중 이탈|수정|삭제|
|:-----:|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/ecb267a2-923b-457a-855a-3aed5a938684">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/b03a5d29-c1a7-4fba-96eb-f9e591d07564">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/f64abaca-5672-43f2-b717-d3d3d505f8cf">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/1366a362-f7f5-44a2-9827-94d505a9f601">|

고민기록[폴더]
|폴더 추가|폴더 삭제|폴더명 수정|
|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/8eef88a9-a1a5-4a32-83ef-a0c9e39b77ea">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/ae523a03-75ad-4770-8850-9bb8828efce3">|<img width="240" src="https://github.com/Kova700/BookChat_Android/assets/81726145/f9d85cc0-111f-4f39-b469-95257f704913">|

고민기록
|고민 기록 추가|고민 기록 삭제|고민 기록 수정|
|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/4f88b6d0-3fdf-4d2a-b63d-458a1b5efeb8">|<img width="240" src="https://github.com/user-attachments/assets/aed57e93-36b0-4c7f-87d1-b0fcd06de5a2">|<img width="240" src="https://github.com/user-attachments/assets/7334093f-0b8e-4569-95e4-26a461f843ff">|

검색
|검색 필터|검색|도서 클릭|채팅방 클릭|
|:-----:|:-----:|:-----:|:-----:|
|||||

채팅방 목록
|꾹누르기|스와이프|상단고정|방장이 삭제한 채팅방|
|:-----:|:-----:|:-----:|:-----:|
|||||

채팅방 생성
|이미지 선택|도서 선택|생성|추가된 목록|
|:-----:|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/039ab941-ffd9-4f07-a2f1-1a6d935ffee4">|<img width="240" src="https://github.com/user-attachments/assets/a5e79ae5-cbba-4e66-b422-8cefd37d5191">|<img width="240" src="https://github.com/user-attachments/assets/576f3d65-2d32-430e-86d5-fa757a35c312">|<img width="240" src="https://github.com/user-attachments/assets/5f922ca0-cadb-4c5f-8ea0-edc757b2799b">|

채팅방
|채팅 캡처|읽었던 영역 표시|하단 스크롤 버튼|
|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/5011bdd6-944a-4aa6-bf78-1355f811e16f">|<img width="240" src="https://github.com/user-attachments/assets/d786f465-1006-4456-b8ae-ceb05078df88">|<img width="240" src="https://github.com/user-attachments/assets/6b2d441a-b07e-4d95-ba26-d588e179cfd4">|

실시간 채팅
|송신|수신-채팅방|수신-Notification|수신-Notification-음소거|
|:-----:|:-----:|:-----:|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/f8cadda9-f445-4dee-8d8d-b02834d60e36">|<img width="240" src="https://github.com/user-attachments/assets/b738773c-e99c-4a81-b4b1-744124037281">|<img width="240" src="https://github.com/user-attachments/assets/1696c652-7f40-4bd5-acf1-cc058ccf6cfd">|<img width="240" src="https://github.com/user-attachments/assets/943dd1b1-c60b-49f6-89b6-74bfe22b5040">|

채팅방 설정
|채팅방 세팅|방장 변경|부방장 변경|
|:-----:|:-----:|:-----:|
||||

마이페이지
|화면|
|:-----:|
|<img width="240" src="https://github.com/user-attachments/assets/aa73ba9c-25ce-44f5-bb93-41bdc62c7969">|

</br>

### App Design ([Figma](https://www.figma.com/file/h6ZwMa9QzWDn1TyCJmawsb/BookChat?node-id=693%3A2079)) 
![image](https://github.com/Kova700/BookChat_Android/assets/81726145/c7cefba2-1013-4d08-b6fd-c5655fc5a734)
이전 디자인 ([Figma](https://www.figma.com/file/h6ZwMa9QzWDn1TyCJmawsb/BookChat?type=design&node-id=0%3A1&mode=design&t=0ceQ3aaZuYh3IsM4-1)) 

</br>

## *****Team***** 
- [Android Developer](https://github.com/Kova700)
- [BackEnd Developer](https://github.com/geneaky)
- [Designer](https://kse0313.creatorlink.net/ABOUT)
