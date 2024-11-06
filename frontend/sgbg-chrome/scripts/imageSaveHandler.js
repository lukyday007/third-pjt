// 이미지 저장 DTO 변수 선언 및 초기화
let imageSaveRequestDto = {
  sourceUrl: '',
  imageUrl: '',
  directoryId: 0,
}

// 저장할 Dto의 directory Id값 설정
function setImageSaveRequestDto(newDirectoryId) {
  imageSaveRequestDto.directoryId = newDirectoryId
}

// 이미지 저장 함수
async function handleSaveImage(requestBody) {
  // requestBody를 깊은 복사하여 개별 요청마다 독립된 객체 생성
  const requestBodyCopy = JSON.parse(JSON.stringify(requestBody))

  let isSaved = false
  let imgSrc = requestBodyCopy.imageUrl

  try {
    const response = await postSaveImage(requestBodyCopy)

    if (!response.ok) {
      throw new Error('이미지 저장 실패')
    }
  } catch (e) {
    return showAlertModal(imgSrc, isSaved)
  }

  isSaved = true
  imgSrc = requestBodyCopy.imageUrl

  return showAlertModal(imgSrc, isSaved)
}

// 이미지 저장 API
async function postSaveImage(requestBody) {
  try {
    // 액세스 토큰 스토리지에서 불러오기
    const accessToken = await getAccessTokenFromStorage()

    const POST_IMAGE_URL = BASE_API_URL + '/images/web'

    const response = await fetch(POST_IMAGE_URL, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    })

    return response
  } catch (e) {
    return e
  }
}
