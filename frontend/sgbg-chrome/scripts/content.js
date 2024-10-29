// 드래그시 계산할 마우스 이벤트
let startX = 0
let startY = 0

// 드래그 중인지를 감지할 변수
let isDragging = false
const thresholdDistance = 50

// 로고 이미지 URL 정의 변수
let logoUrl
let commonFolderIconUrl
let createFolderIconUrl

try {
  logoUrl = chrome.runtime.getURL('images/singlebungle.svg')
  commonFolderIconUrl = chrome.runtime.getURL('images/common-folder-icon.svg')
  createFolderIconUrl = chrome.runtime.getURL('images/create-folder-icon.svg')
} catch (e) {
  console.error('(싱글벙글) 이미지 URL 로드 실패: ', e)
}

// 폴더 임시 변수 -> 차후 api 요청해서 받는것으로 수정
let directoryInfos
directoryInfos = [
  { directoryId: 1, directoryName: '싱글벙글 모음' },
  { directoryId: 2, directoryName: '훌쩍훌쩍 모음' },
  { directoryId: 2, directoryName: '오싹오싹 모음' },
]

// 드래그 시작 이벤트 - 이미지 드래그 여부 확인 및 초기 위치 저장
document.addEventListener('dragstart', (event) => {
  // 이미지라면 드래그 중 감지 변수를 true로 설정하고, 현재 커서 위치를 저장
  if (event.target.tagName === 'IMG') {
    isDragging = true
    startX = event.clientX
    startY = event.clientY
  }

  console.log(event.clientX, event.clientY)
})

// 드래그 중 이벤트 - 거리 감지해서 modal을 띄우는 동작
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

// 드래그 입장 이벤트 - droppable 영역을 확인해서 drag-over 클래스 추가
document.addEventListener('dragenter', (event) => {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    // 중첩 카운팅 방식 사용 - 사용하지 않았을 때 하위 요소에 접근시 dragLeave 이벤트가 발생
    if (!dropArea.dataset.dragCounter) {
      // modal-droppable의 하위 요소에 접근할 때 카운터를 추가하고, 0이면 벗어나는 개념
      dropArea.dataset.dragCounter = 1
      dropArea.classList.add('drag-over')
      dropArea.style.cursor = 'copy'
    } else {
      dropArea.dataset.dragCounter++
    }
  }
})

// 드래그 오버 이벤트 - 드래구 중 커서 변경
document.addEventListener('dragover', (event) => {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    event.preventDefault()
    dropArea.style.cursor = 'copy'
  }
})

// 요소 드롭 이벤트
document.addEventListener('drop', (event) => {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    event.preventDefault()
    console.log(dropArea)
    dropArea.classList.remove('drag-over')
    dropArea.style.cursor = ''
    delete dropArea.dataset.dragCounter
  }
})

// 드래그 퇴장 이벤트 - 추가했던 drag-over 요소 삭제
document.addEventListener('dragleave', (event) => {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    // 중첩 카운터에 -1을 하고, 카운터가 0이 되면 drag-over 클래스 삭제
    dropArea.dataset.dragCounter--

    if (dropArea.dataset.dragCounter == 0) {
      dropArea.classList.remove('drag-over')
      dropArea.style.cursor = ''
      delete dropArea.dataset.dragCounter
    }
  }
})

// 드래그 끝 이벤트
// drop 이벤트 후에도 drag-end 이벤트가 반드시 호출된다.
// drop -> dragover -> dragend 순으로 종료되어
// dragend 이벤트에서 cursor 및 스타일 관련 이벤트 처리가 필요하다.
// 차후 여기에 이미지 저장 이벤트 추가
document.addEventListener('dragend', (event) => {
  console.log('dragend', isDragging, startX, startY)
  isDragging = false
  startX = 0
  startY = 0
  console.log('dragend', isDragging, startX, startY)

  // 커서 및 drag-over 초기화
  document.querySelectorAll('.modal-droppable').forEach((dropArea) => {
    dropArea.classList.remove('drag-over') // 드래그 관련 클래스 초기화
    dropArea.style.cursor = '' // 커서 초기화
    delete dropArea.dataset.dragCounter
  })

  const existingModal = document.querySelector('#save-modal')

  if (existingModal) {
    existingModal.remove()
  }
})

// 모달 위치 계산 함수
// 생성되는 모달이 창밖으로 넘어가지 않도록 조정
function calcModalCoord(clientX, clientY) {
  const modalWidth = 600
  const modalHeight = 320

  // 창 크기
  const vw = window.innerWidth
  const vh = window.innerHeight

  let top = clientY
  let left = clientX

  // 우측으로 넘어갈 때 조정
  if (left + modalWidth / 2 > vw) {
    left = vw - modalWidth / 2 - 20
  }

  // 좌측으로 넘어갈 때 조정
  if (left - modalWidth / 2 < 0) {
    left = modalWidth / 2 + 20
  }

  // 아래로 넘어갈 때 조정
  if (top + modalHeight / 2 > vh) {
    top = vh - modalHeight / 2 - 20
  }
  // 위로 넘어갈 때 조정
  if (top - modalHeight / 2 < 0) {
    top = modalHeight / 2 + 20
  }

  return { left, top }
}

// 모달 표시 함수
function showModal(event) {
  // 모달이 있다면 기존 모달 제거
  const existingModal = document.querySelector('#save-modal')

  if (existingModal) {
    existingModal.remove()
  }

  // 모달 요소 추가
  const modal = document.createElement('div')

  // 모달 위치 및 모양 정의
  modal.id = 'save-modal'

  const { left, top } = calcModalCoord(event.clientX, event.clientY)

  // 모달이 화면 밖으로 넘어가지 않도록 계산
  modal.style.top = `${top}px`
  modal.style.left = `${left}px`

  // 사전 정의된 HTML을 modal 내부에 추가 내부에 추가
  modal.innerHTML = `
  <div class="modal-container">
    <section class="modal-drop-section">
      <div class="modal-drop-box modal-droppable">
        <img src="" alt="" class="modal-drop-logo" />
        <span>여기에 파일을 드롭하세요</span>
        <div></div>
      </div>
    </section>
    <div class="modal-container-divider"></div>
    <section class="modal-directory-section">
      <div class="modal-directory-list">
        <div class="modal-folder-area modal-droppable">
          <img src="" alt="" class="directory-img" />
          <span class="modal-folder-title">기본 폴더</span>
        </div>
      </div>
      <div class="modal-directory-divider"></div>
      <div class="modal-directory-add">
        <div class="modal-folder-area modal-droppable">
          <img src="" alt="폴더 만들기" class="create-folder-icon"/>
          <span class="modal-folder-title">폴더 만들기</span>
        </div>
      </div>
    </section>
  </div>
  `

  //   이미지 요소 생성 및 모달에 추가
  const logo = modal.querySelector('.modal-drop-logo')
  logo.src = logoUrl
  logo.alt = '로고'

  // 디렉토리 리스트를 담는 요소
  const directoryList = modal.querySelector('.modal-directory-list')

  // 폴더 구조 추가
  directoryInfos.forEach((directoryInfo) => {
    const newElement = document.createElement('div')

    // 내부 구조 설정
    newElement.innerHTML = `
    <div class="modal-folder-area modal-droppable">
      <img src="" alt="" class="directory-img"/>
      <span class="modal-folder-title"></span>
    </div>`

    // 이미지 디렉토리 저장시 디렉토리 id 감지를 위한 id값 설정
    const folderArea = newElement.querySelector('.modal-folder-area')
    folderArea.setAttribute('id', directoryInfo.directoryId)

    // 디렉토리 이름 설정
    const folderTitle = newElement.querySelector('.modal-folder-title')
    folderTitle.innerText = directoryInfo.directoryName

    directoryList.appendChild(newElement)
  })

  // 기본 폴더 아이콘 추가
  const imageElements = modal.querySelectorAll('.directory-img')

  imageElements.forEach((element) => {
    element.src = commonFolderIconUrl
  })

  // 폴더 만들기 아이콘 추가
  const createFolderIcon = modal.querySelector('.create-folder-icon')
  createFolderIcon.src = createFolderIconUrl

  document.body.appendChild(modal)
}
