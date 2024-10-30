// 구글 로그인 버튼
const loginBtn = document.querySelector('.login-btn')

// 앱 열기 버튼
const openAppBtn = document.querySelector('#app-btn')

// 프로필 버튼
const profileBtn = document.querySelector('#profile-btn')

if (loginBtn) {
  loginBtn.addEventListener('click', setLoggedIn)
}
if (profileBtn) {
  profileBtn.addEventListener('click', setLoggedOut)
}
if (openAppBtn) {
  openAppBtn.addEventListener('click', openApp)
}

// 앱 열기 이벤트 클릭 시 발생으로 정의
openAppBtn.addEventListener('click', openApp)

// 구글 로그인 버튼
loginBtn.addEventListener('click', setLoggedIn)

// 임시 로그아웃 버튼 (프로필 버튼)
profileBtn.addEventListener('click', setLoggedOut)

// 구글 로그인 버튼 이벤트
function setLoggedIn() {
  //
  chrome.storage.local.set({ isLoggedIn: true })
  setButtonsLoggedIn()
}

// 개발 임시 로그아웃 이벤트
function setLoggedOut() {
  chrome.storage.local.set({ isLoggedIn: false })
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
      setButtonsLoggedIn()
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
