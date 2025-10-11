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
| 📝 `Docs` `:memo:`                     | 문서 수정 (README 등)      |
| ♻️`Refactor` `:recycle:`               | 기능 변경 없이 코드 구조 개선         |
| 🚀 `Deploy` `:rocket:`                 | 배포                        |
| ⚡️ `Performance`   `:zap:`             | 성능 향상                     |
| 🧪 `Test`  `:test_tube:`               | 테스트 코드 추가/수정              |
| 🏗️ `Build` `:building_construction:`  | 빌드/외부 의존성 관련              |
| 📦 `Chore`  `:package:`                | 기타 관리용 (ex: .gitignore)   |
| ⏪ `Revert`  `:rewind:`                 | 이전 커밋 되돌리기                |
| 🚑️ `!HOTFIX` `:ambulance:`            | 긴급 버그 수정                  |
| 🔥 `Delete` `:fire:`                   | 코드/파일 삭제                  |
| 💬 `Comment` `:speech_balloon:`        | 주석 추가/변경                  |
| 🏷️ `Rename` `:label:`                 | 파일/폴더 이름 변경               |
| 👽️ `!BREAKING CHANGE` `:alien:`       | API 변경 등 큰 변경             |
| 🌱 `Add` `:seedling:`                  | 시드 파일 추가/업데이트             |
| 💚 `CI` `:green_heart:`                | CI 관련 수정                  |

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
## 3. 공통 예외
| 예외 클래스                                | HTTP 상태 코드                | 메시지                | 처리 방법               |
| ------------------------------------- | ------------------------- | ------------------ | ------------------- |
| `IllegalArgumentException`            | 400 Bad Request           | 예외 메시지 그대로         | 잘못된 인자 전달 시         |
| `InsufficientAuthenticationException` | 401 Unauthorized          | "인증 정보가 부족합니다."    | JWT 만료/인증 실패        |
| `AccessDeniedException`               | 403 Forbidden             | "권한이 없습니다."        | 권한 부족 시             |
| `EntityNotFoundException`             | 404 Not Found             | "존재하지 않는 리소스입니다."  | DB 조회 실패 시          |
| `Exception`                           | 500 Internal Server Error | "서버 오류가 발생했습니다."   | 기타 예외               |

---

## 4. Git Rebase 작업 가이드

$git checkout develop   

$git fetch origin 

$git reset --hard origin/develop (선택)
 
$git checkout 내브랜치   

1. develop 브랜치 기준으로 rebase 하기,
   - `git rebase origin/develop`

2. 충돌이 났다면 해결 후 
   - ` git add .`
   - ` git rebase --continue`
     
3. 최종 커밋하고 원격 저장소에 푸시  
   - `git add .`
   - `git commit -m` 
   - `git push origin 브랜치명`  

⚠️rebase 취소 명령어
` git rebase --abort `

⚠️ 중요 공지
git rebase develop 명령어 실행 전에,
매 기능 push할 때마다 새로운 브랜치를 생성하면 오류를 방지할 수 있습니다.

