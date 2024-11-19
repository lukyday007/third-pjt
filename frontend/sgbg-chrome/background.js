const BASE_API_URL = 'https://k11b205.p.ssafy.io/api'

// 현재 탭의 URL 정보 받아오기
// async function getCurrentTab() {
//   let queryOptions = { active: true, lastFocusedWindow: true }
//   let [tab] = await chrome.tabs.query(queryOptions)

//   return tab
// }

// 우클릭 컨텍스트 메뉴바 항목 추가
chrome.runtime.onInstalled.addListener(() => {
  // 우클릭 메뉴 생성
  chrome.contextMenus.create({
    id: 'saveImage',
    title: '싱글벙글에 이미지 저장',
    contexts: ['image'],
  })

  // 메뉴 클릭시 동작 정의
  chrome.contextMenus.onClicked.addListener((info, tab) => {
    const sourceUrl = tab.url
    const imageUrl = info.srcUrl

    if (info.menuItemId === 'saveImage' && sourceUrl && imageUrl) {
      const saveImageRequestBody = {
        sourceUrl: sourceUrl,
        imageUrl: imageUrl,
        directoryId: 0,
      }

      // 현재 탭에서 content.js의 함수 실행
      chrome.scripting.executeScript({
        target: { tabId: tab.id },
        func: (saveImageRequestBody) => {
          // content.js의 함수 실행
          handleSaveImage(saveImageRequestBody)
        },
        // 함수에 전달할 인자 설정
        args: [saveImageRequestBody],
      })
    }
  })
})

// 메세지 리스너 정의 - 팝업 열기
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === 'openPopup') {
    chrome.action.openPopup()
  }
})

// 메세지 리스너 정의 - 구글 로그인 요청
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === 'startGoogleLogin') {
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
      return response.text()
    })
    .then((data) => {
      return data
    })
    .catch((e) => {
      throw new Error('구글 로그인  URL 불러오기 실패', e)
    })

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

    if (!response.ok) {
      throw new Error({ message: 'jwt token fetch error' })
    }

    const data = await response.json()
    const accessToken = data['access-token']

    return accessToken
  } catch (e) {
    throw new Error('fetchJwtToken 에러: ', e)
  }
}

// JWT 토큰을 chrome storage에 저장
function setAccessTokenAtStorage(accessToken) {
  try {
    chrome.storage.local.set({ accessToken: accessToken }, function () {})
    return true
  } catch (e) {
    throw new Error('액세스 토큰 로컬 스토리지 등록 실패', e)
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
    throw new Error('fetchUserInfo 에러: ', e)
  }
}

// 유저 정보 저장
async function setUserInfoAtStorage(userInfo) {
  try {
    chrome.storage.local.set({ userInfo: userInfo }, () => {})
    return true
  } catch (e) {
    throw new Error('유저 정보 설정 실패', e)
    return false
  }
}

// 로그아웃 메세지 리스너 정의
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === 'postLogout') {
    postLogout()
  }
})

async function postLogout() {
  const accessToken = await getAccessTokenFromStorage()

  const LOGOUT_URL = BASE_API_URL + '/users/logout'

  fetch(LOGOUT_URL, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })
}
