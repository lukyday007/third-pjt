// 키 누름 동작 확인 변수
let isBacktickPressed = false

document.addEventListener('keydown', backTickKeyDownHandler)
document.addEventListener('keyup', backTickKeyUpHandler)
document.addEventListener('click', backTickClickHandler, true)

// ` 키를 눌렀을 때 설정
function backTickKeyDownHandler(event) {
  if (event.key === '`') {
    isBacktickPressed = true
  }
}

// ` 키를 뗐을 때 설정
function backTickKeyUpHandler(event) {
  if (event.key === '`') {
    isBacktickPressed = false
  }
}

// ` 키를 누른 채 클릭시 함수
async function backTickClickHandler(event) {
  // 백틱 키 눌림 여부 확인
  if (!isBacktickPressed) return

  // 클릭 동작 해제
  event.preventDefault()
  event.stopPropagation()

  // 클릭 대상의 이미지 여부 확인
  if (event.target.tagName !== 'IMG') return

  const newSourceUrl = await getCurrentTab()
  const newImgUrl = event.target.src

  if (newSourceUrl && newImgUrl) {
    imageSaveRequestDto.sourceUrl = newSourceUrl
    imageSaveRequestDto.imageUrl = newImgUrl

    handleSaveImage(imageSaveRequestDto)
  }
}
