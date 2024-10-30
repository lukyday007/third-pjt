// 현재 탭의 URL 정보 받아오기
async function getCurrentTab() {
  let queryOptions = { active: true, lastFocusedWindow: true }
  let [tab] = await chrome.tabs.query(queryOptions)

  return tab
}

// 메세지 리스너 정의
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
