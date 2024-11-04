const BASE_API_URL = 'http://localhost:8080/api'

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

    // 액세스 토큰 요청
    const accessToken = await fetchJwtToken(oauthCode)

    // 토큰 localStorage 저장
    const isJwtStored = setAccessTokenAtStorage(accessToken)

    if (!isJwtStored) {
      throw new Error({ message: 'jwt 저장 실패' })
    }

    const isUserInfoStored = await fetchUserInfo()

    if (!isUserInfoStored) {
      throw new Error({ message: 'userInfo 저장 실패' })
    }

    return sendResponse({ success: true })
  } catch (e) {
    sendResponse({ success: false, error: e.message })
  }
}

// 구글 소셜 로그인 동작 - 백엔드에 주소를 받아와서 구글 서비스에 로그인
async function fetchOauthCode() {
  // 로그인 요청
  const SPRING_LOGIN_URL = BASE_API_URL + '/oauth2/google/authorize?platform=extension'

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
async function fetchJwtToken(oauthCode) {
  // 토큰 요청
  const JWT_FETCH_URL = BASE_API_URL + '/oauth2/code/google'
  const params = {
    platform: 'extension',
    code: oauthCode,
  }

  // query Parameter 객체 문자열로 변환
  const queryString = new URLSearchParams(params).toString()

  // query Parameter를 합친 url로 변환
  const urlWithParams = `${JWT_FETCH_URL}?${queryString}`

  try {
    const response = await fetch(urlWithParams)
    console.log(response)

    if (!response.ok) {
      throw new Error({ message: 'jwt token fetch error' })
    }

    const data = await response.json()
    const accessToken = data['access-token']

    console.log('accessToken: ', accessToken)

    return accessToken
  } catch (e) {
    console.log('fetchJwtToken 에러: ', e)
  }
}

// JWT 토큰을 chrome storage에 저장
function setAccessTokenAtStorage(accessToken) {
  try {
    chrome.storage.local.set({ accessToken: accessToken }, function () {
      console.log('JWT token 저장 완료:', accessToken)
    })
    return true
  } catch (e) {
    console.log(e)
    return false
  }
}

// JWT 토큰을 chrome storage에서 읽기
async function getAccessTokenFromStorage() {
  try {
    const result = await chrome.storage.local.get('accessToken')

    const accessToken = result.accessToken

    if (!accessToken) {
      return new Error({ message: 'access Token 읽어오기 실패' })
    }

    console.log('스토리지에서 가져온 토큰: ', accessToken)

    return accessToken
  } catch (e) {
    return e
  }
}

// 유저 정보 가져오기
async function fetchUserInfo() {
  const FETCH_USER_INFO_URL = BASE_API_URL + '/users'
  const accessToken = await getAccessTokenFromStorage()

  try {
    const response = await fetch(FETCH_USER_INFO_URL, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    })
    const data = await response.json()

    if (data) {
      setUserInfoAtStorage(data)
    }

    return data
  } catch (e) {
    console.log('fetchUserInfo 에러: ', e)
  }
}

// 유저 정보 저장
async function setUserInfoAtStorage(userInfo) {
  try {
    chrome.storage.local.set({ userInfo: userInfo }, () => {
      console.log('userInfo 저장 완료: ', userInfo)
    })
    return true
  } catch (e) {
    console.log(e)
    return false
  }
}
