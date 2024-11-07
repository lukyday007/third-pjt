// 구글 로그인 버튼
const loginBtn = document.querySelector('.login-btn')

// 앱 열기 버튼
const openAppBtn = document.querySelector('#app-btn')

// 프로필 버튼
const profileBtn = document.querySelector('#profile-btn')

if (loginBtn) {
  // 구글 로그인 버튼
  loginBtn.addEventListener('click', startLogin)
}
if (profileBtn) {
  // 임시 로그아웃 버튼 (프로필 버튼)
  profileBtn.addEventListener('click', setLoggedOut)
}
if (openAppBtn) {
  // 앱 열기 이벤트 클릭 시 발생으로 정의
  openAppBtn.addEventListener('click', openApp)
}

// 구글 로그인 이벤트
async function setLoggedIn() {
  try {
    const { email, profileImagePath } = await getUserInfoFromStorage()

    const profileBtn = document.querySelector('#profile-btn')
    const profileImg = profileBtn.querySelector('.pop-logo')
    const profileTitle = profileBtn.querySelector('.pop-title')

    profileImg.src = profileImagePath
    profileTitle.innerText = email
  } catch (e) {
    throw new Error('로그인 실패', e)
  }

  chrome.storage.local.set({ isLoggedIn: true })
  setButtonsLoggedIn()
}

// 개발 임시 로그아웃 이벤트
function setLoggedOut() {
  // 로그인 상태 제거
  chrome.storage.local.set({ isLoggedIn: false })
  // 유저 정보 삭제
  chrome.storage.local.remove('userInfo', () => {})
  // 액세스 토큰 삭제
  chrome.storage.local.remove('accessToken', () => {})
  // 서버에 로그아웃 요청
  chrome.runtime.sendMessage({ action: 'postLogout' })
  // 버튼 로그아웃 설정
  setButtonsLoggedOut()
}

// 앱 열기 버튼 이벤트
function openApp() {
  const message = 'openAppBtn Clicked'

  alert(message)
}

// 로그인 상태에 따라 continue with Google 혹은 프로필 띄우기
document.addEventListener('DOMContentLoaded', function () {
  chrome.storage.local.get('isLoggedIn', ({ isLoggedIn }) => {
    if (isLoggedIn) {
      setLoggedIn()
    } else {
      setButtonsLoggedOut()
    }
  })
})

// 로그인 상태 display 변경
function setButtonsLoggedIn() {
  document.querySelector('.login-btn').style.display = 'none'
  document.querySelector('#profile-btn').style.display = 'flex'
}

// 로그아웃 상태 display 변경
function setButtonsLoggedOut() {
  document.querySelector('.login-btn').style.display = 'block'
  document.querySelector('#profile-btn').style.display = 'none'
}

// 구글 로그인 이벤트 시작 (background.js로 message 전달)
function startLogin() {
  chrome.runtime.sendMessage({ action: 'startGoogleLogin' }, (response) => {
    if (chrome.runtime.lastError) {
      console.error('Runtime error: ', chrome.runtime.lastError)
      return
    }

    if (response && response.success) {
      setLoggedIn()
    } else {
      console.error('JWT fetch 실패')
    }
  })
}

async function getUserInfoFromStorage() {
  try {
    const userInfo = await new Promise((resolve, reject) => {
      chrome.storage.local.get('userInfo', (result) => {
        if (chrome.runtime.lastError) {
          reject(new Error('userInfo 없음'))
        }

        resolve(result.userInfo)
      })
    })

    return userInfo
  } catch (e) {
    return new Error({ message: 'userInfo 없음' })
  }
}
