// 드래그시 계산할 마우스 이벤트
let startX = 0
let startY = 0

// 드래그 중인지를 감지할 변수
let isDragging = false
const thresholdDistance = 50

// 드래그 시작 이벤트
document.addEventListener('dragstart', (event) => {
  // 이미지라면 드래그 중 감지 변수를 true로 설정하고, 현재 커서 위치를 저장
  if (event.target.tagName === 'IMG') {
    isDragging = true
    startX = event.clientX
    startY = event.clientY
  }

  console.log(event.clientX, event.clientY)
})

// 드래그 중 이벤트
document.addEventListener('drag', (event) => {
  // 이미지가 아닌 경우는 제외
  if (event.target.tagName !== 'IMG') {
    return
  }

  if (!isDragging) {
    return
  }

  //   위치를 계속 감지하며 거리가 50px 이상인 경우 modal 창을 띄움
  if (Math.abs(startX - event.clientX) + Math.abs(startY - event.clientY) > thresholdDistance) {
    showModal(event)
    isDragging = false
  }
})

// 드래그 끝 이벤트
document.addEventListener('dragend', (event) => {
  console.log('dragend', isDragging, startX, startY)
  isDragging = false
  startX = 0
  startY = 0
  console.log('dragend', isDragging, startX, startY)

  const existingModal = document.querySelector('#save-modal')

  if (existingModal) {
    existingModal.remove()
  }
})

// 모달 표시 함수
function showModal(event) {
  // 모달이 있다면 기존 모달 제거
  const existingModal = document.querySelector('#save-modal')

  if (existingModal) {
    existingModal.remove()
  }

  // 로고 이미지 URL 정의
  const logoUrl = chrome.runtime.getURL('images/singlebungle.svg')

  //   모달 요소 추가
  const modal = document.createElement('div')

  //   모달 위치 및 모양 정의
  modal.id = 'save-modal'
  modal.style.position = 'fixed'
  modal.style.top = `${event.clientY}px`
  modal.style.left = `${event.clientX}px`
  modal.style.width = '600px'
  modal.style.height = '320px'
  modal.style.transform = 'translate(-50%, -50%)'
  modal.style.padding = '20px'
  modal.style.border = '1px solid #ffffff'
  modal.style.boxShadow = '0 0 15px rgba(0, 0, 0, 0.3)'
  modal.style.borderRadius = '20px'
  modal.style.backgroundColor = '#ffffff'
  modal.style.zIndex = '1000'

  modal.innerHTML = `
  <div class="container">
    <div class="drop-box">
      <div>
      </div>
    </div>
    <div>
      <div>
      </div>
      <div>
      </div>
    </div>
  </div>
  `

  const containerDiv = modal.querySelector('.container')
  const dropBoxDiv = modal.querySelector('.drop-box')

  //   이미지 요소 생성 및 모달에 추가
  const logo = document.createElement('img')
  logo.src = logoUrl
  logo.alt = '로고'
  logo.style.width = '250px'

  //   모달에 이미지 추가
  dropBoxDiv.appendChild(logo)

  document.body.appendChild(modal)
}
