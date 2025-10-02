# KKUBEURAKKO_BE
꾸브라꼬 안양점 백엔드 개발 레포지토리

## 1. 커밋 컨벤션

### ✅ 커밋 유형 지정
- 커밋 유형은 영어 대문자로 작성
- 깃모지 끝나고 한 칸 띄우고 입력하기 
  ```bash
  git commit -m ":sparkles: Feat: 기능추가"
  
| 커밋 유형                               | 의미                        |
| ----------------------------------- | ------------------------- |
| ✨ `Feat`  `:sparkles:`                | 새로운 기능 추가                 |
| 🐛 `Fix` `:bug:`                       | 버그 수정                     |
| 📝 `Docs` `:memo:`                     | 문서 수정 (README, 주석 등)      |
| 💄 `Style` `:lipstick:`                | 코드 포맷팅, 세미콜론 누락, 기능 변화 없음 |
| ♻️`Refactor` `:recycle:`               | 기능 변경 없이 코드 구조 개선         |
| 🚀 `Deploy` `:rocket:`                 | 배포                        |
| ⚡️ `Performance`   `:zap:`             | 성능 향상                     |
| 🧪 `Test`  `:test_tube:`               | 테스트 코드 추가/수정              |
| 🏗️ `Build` `:building_construction:`  | 빌드/외부 의존성 관련              |
| 📦 `Chore`  `:package:`                | 기타 관리용 (ex: .gitignore)   |
| ⏪ `Revert`  `:rewind:`                 | 이전 커밋 되돌리기                |
| 🚑️ `!HOTFIX` `:ambulance:`            | 긴급 버그 수정                  |
| 🔥 `Delete` `:fire:`                   | 코드/파일 삭제                  |
| 🎨 `Design` `:art:`                    | UI/CSS 관련 작업              |
| 💫 `Animation` `:dizzy:`               | 애니메이션/트랜지션 작업             |
| 💬 `Comment` `:speech_balloon:`        | 주석 추가/변경                  |
| 🏷️ `Rename` `:label:`                 | 파일/폴더 이름 변경               |
| 🚚 `Route` `:truck:`                   | 라우터 관련 설정 변경              |
| 👽️ `!BREAKING CHANGE` `:alien:`       | API 변경 등 큰 변경             |
| 🌱 `Add` `:seedling:`                  | 시드 파일 추가/업데이트             |
| 💚 `CI` `:green_heart:`                | CI 관련 수정                  |
| 🔖 `Tag` `:bookmark:`                  | 버전 태그                     |

--- 
## 2. 브랜치 전략

1.  **GitHub에 Issue 를 판다. → `이슈번호` 배정받음.**  
2.  **브랜치 `역할/#이슈번호` 형식으로 판다.**  
     ```
     - Feat/#1
     - Fix/#2
     - Refactor/#3
     ```

3. **프로젝트에서 새로운 브랜치에서 작업을 한다.**

4. **커밋시 이슈번호를 항상 붙힌다 항상!!**  
    ```
    [Feat] 책장뷰 UI 구현 #1
    ```

5. **해당 이슈 다 작업했다면 Pull Request 날린다**

---
## 3. Git Rebase
