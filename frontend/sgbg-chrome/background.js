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

    return true
  }
})

// 메세지 리스너 정의 - 구글 로그인 요청
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  console.log('background.js -> message 받음')
  if (message.action === 'startGoogleLogin') {
    console.log('background.js -> startGoogleLogin 받음')
    startGoogleLogin(sendResponse)

    return true
  }
})

// 로그인 시작 버튼
async function startGoogleLogin(callback) {
  const SPRING_LOGIN_URL = 'http://localhost:8080/api/oauth2/google/authorize'

  const GOOGLE_OAUTH_URL = await fetch(SPRING_LOGIN_URL)
    .then((response) => {
      console.log(response)
      return response.text()
    })
    .then((data) => {
      console.log(data)
      return data
    })
    .catch((e) => e)

  chrome.identity.launchWebAuthFlow(
    {
      url: GOOGLE_OAUTH_URL,
      interactive: true,
    },
    function (redirectUrl) {
      if (redirectUrl) {
        console.log('성공')
        console.log('redirectUrl: ', redirectUrl)
        const token = new URL(redirectUrl).searchParams.get('token')
        if (token) {
          storeJWT(token)
          callback({ success: true, token })
        } else {
          callback({ success: false })
        }
      } else {
        callback({ success: false })
      }
    }
  )
}

function storeJWT(token) {
  chrome.storage.local.set({ jwtToken: token }, function () {
    console.log('JWT token 저장 완료')
  })
}
