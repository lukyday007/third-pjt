// 폴더 정보 변수
let directoryInfos = []

let newDirectoryName = ''

// 스토리지에서 액세스 토큰을 불러와서 반환
async function getAccessTokenFromStorage() {
  return new Promise((resolve, reject) => {
    const result = chrome.storage.local.get('accessToken', (result) => {
      if (chrome.runtime.lastError) {
        reject(new Error('액세스 토큰 불러오기 실패'))
      } else if (!result.accessToken) {
        reject(new Error('액세스 토큰이 저장되지 않았습니다. '))
      } else {
        resolve(result.accessToken)
      }
    })
  })
}

// 디렉토리 목록 조회 API
async function fetchDirectoryList() {
  try {
    // 액세스 토큰 스토리지에서 불러오기
    const accessToken = await getAccessTokenFromStorage()

    const response = await fetch(BASE_API_URL + '/directories', {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    })

    const data = await response.json()
    const directoryInfos = data.directories

    return directoryInfos
  } catch (e) {
    return e
  }
}

// 디렉토리 생성 API
async function postCreateDirectory(directoryName) {
  try {
    const accessToken = await getAccessTokenFromStorage()

    const requestBody = {
      directoryName: directoryName,
    }

    const response = await fetch(BASE_API_URL + '/directories', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    })

    if (response.ok) {
    } else {
      throw new Error('디렉토리 생성 실패')
    }

    data = await response.json()

    return data
  } catch (e) {
    return e
  }
}
