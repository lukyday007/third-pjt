// 현재 탭의 URL 정보 받아오기
async function getCurrentTab() {
  let queryOptions = { active: true, lastFocusedWindow: true }
  let [tab] = await chrome.tabs.query(queryOptions)

  return tab
}

// 메세지 리스너 정의 - 현재 탭 정보 가져오기
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === 'getCurrentTab') {
    getCurrentTab()
      .then((tab) => {
        sendResponse({ tab })
      })
      .catch((e) => {
        console.error('탭 정보 불러오기 실패', e)
        sendResponse({ error: e.message })
      })

    // 비동기 처리를 위해 sendResponse 동작이 일어나기 전까지는 true를 반환
    return true
  }
})

// 메세지 리스너 정의 - 구글 로그인 요청
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  console.log('background.js -> message 받음')
  if (message.action === 'startGoogleLogin') {
    console.log('background.js -> startGoogleLogin 받음')

    // chrome.runtime.onMessage.addListener 내에서는 비동기 함수 처리가 안돼서, 별도로 구분
    handleGoogleLogin(sendResponse)

    // 비동기 처리를 위해 sendResponse 동작이 일어나기 전까지는 true를 반환
    return true
  }
})

// 로그인 처리 함수
async function handleGoogleLogin(sendResponse) {
  try {
    // Oauth 코드 가져오기
    const oauthCode = await fetchOauthCode()

    // 차후 별도의 반환값이 없도록 수정
    if (oauthCode) {
      sendResponse({ success: true, oauthCode: oauthCode })
    } else {
      sendResponse({ success: false })
    }

    // 코드로 로그인 요청
  } catch (e) {
    sendResponse({ success: false, error: e.message })
  }
}

// 구글 소셜 로그인 동작 - 백엔드에 주소를 받아와서 구글 서비스에 로그인
async function fetchOauthCode() {
  // 로그인 요청
  const SPRING_LOGIN_URL = 'http://localhost:8080/api/oauth2/google/authorize?platform=extension'

  // 구글 로그인 URL을 백엔드 서버에 요청
  const GOOGLE_OAUTH_URL = await fetch(SPRING_LOGIN_URL)
    .then((response) => {
      console.log(response)
      return response.text()
    })
    .then((data) => {
      console.log(data)
      return data
    })
    .catch((e) => console.log())

  // 팝업에서 요청을 보낸 뒤, 새 창에서 크롬 로그인 동작 시작
  return new Promise((resolve, reject) => {
    chrome.identity.launchWebAuthFlow(
      {
        url: GOOGLE_OAUTH_URL,
        interactive: true,
      },
      // 새 창에서 구글 로그인 동작 후 redirect url에서 Google oauth code 파싱
      function (redirectUrl) {
        if (redirectUrl) {
          // 성공 여부 확인 console
          console.log('Google 로그인 성공')
          console.log('redirectUrl: ', redirectUrl)

          // redirect URI에서 code 파싱
          const oauthCode = new URL(redirectUrl).searchParams.get('code')

          // Oauth2 Code가 있다면
          if (oauthCode) {
            resolve(oauthCode)
          } else {
            reject(new Error('URL에 oauth Code가 담겨있지 않음'))
          }
        } else {
          reject(new Error('로그인 실패 혹은 취소'))
        }
      }
    )
  })
}

// Google Oauth Code로 JWT Token을 받아오는 함수 (access : Response body, refresh : Cookie)

// JWT 토큰을 chrome storage에 저장
function storeJWT(accessToken) {
  chrome.storage.local.set({ accessToken: accessToken }, function () {
    console.log('JWT token 저장 완료:', accessToken)
    return accessToken
  })
}
