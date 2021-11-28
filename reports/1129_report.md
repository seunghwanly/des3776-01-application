# Updates
- User Information을 받을 수 있는 입력 란이 추가되었습니다
- User Information은 현재 이름, 나이, 질병 그리고 복용하는 약으로 되어있습니다
- AVD 내 keyboard dismiss issue가 있었는 데, data wipe-out으로 해결하였습니다
- EditText 내에서 입력 시 selection issue를 해결했습니다
  
  해결방안
  ``` kotlin
  userNameInputText.setSelection(s.length)
  ```

# Screenshots
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763372-fb29a5ac-49a9-4262-a167-db8ef939f68f.png"/>
INTRO 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763375-b10e03e9-0716-49d9-9e84-6f7450201700.png"/>
입력 폼 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763379-3e6c4f01-26b3-4dd1-9c04-2e22626df842.png"/>
파일 선택 화면 
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763381-1465a400-f9b0-49ac-a627-40c9efb82bf4.png"/>
파일 선택 후 기다리는 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763382-37dbce35-fb7b-4ee0-839b-ff24280dc1d8.png"/>
입력 받은 결과 출력 화면

![스크린샷 2021-11-28 오후 7 00 30](https://user-images.githubusercontent.com/22142225/143763485-4dd15499-e333-4bd9-ac4a-1cf81b9d29db.png)
서버 실행 화면
