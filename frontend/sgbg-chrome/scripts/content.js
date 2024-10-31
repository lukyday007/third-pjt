// 로고 이미지 URL 정의 변수
let logoUrl
let logoSadUrl
let commonFolderIconUrl
let createFolderIconUrl

// 크롬 확장 경로의 이미지 파일 URl 로드
try {
  logoUrl = chrome.runtime.getURL('images/singlebungle.svg')
  logoSadUrl = chrome.runtime.getURL('images/singlebungle-sad.svg')
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
  { directoryId: 3, directoryName: '오싹오싹 모음' },
]

let imageSaveRequestDto = {
  webUrl: '',
  imageUrl: '',
  directoryId: 0,
}

// 드래그 동작 초기화 함수
function initDrag(dropArea) {
  dropArea.classList.remove('drag-over') // 드래그오버 클래스 초기화
  dropArea.style.cursor = '' // 커서 초기화
  delete dropArea.dataset.dragCounter // dragCounter 데이터셋 삭제
}

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
async function showModal(event) {
  // 현재 탭 정보를 받아와서 출력 (임시 확인용) ##################################################
  const currentTabUrl = await getCurrentTab()
  imageSaveRequestDto.webUrl = currentTabUrl
  console.log(imageSaveRequestDto)

  // // 모달이 있다면 기존 모달 제거
  // const existingModal = document.querySelector('#save-modal')

  // if (existingModal) {
  //   existingModal.remove()
  // }

  const existingOverlay = document.querySelector('#save-modal-overlay')

  if (existingOverlay) {
    existingOverlay.remove()
  }

  // overlay 요소 추가
  const overlay = document.createElement('div')
  overlay.id = 'save-modal-overlay'

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
      <div class="modal-drop-box modal-droppable" data-directory-id="0">
        <img src="" alt="" class="modal-drop-logo" />
        <span>여기에 파일을 드롭하세요</span>
        <div></div>
      </div>
    </section>
    <div class="modal-container-divider"></div>
    <section class="modal-directory-section">
      <div class="modal-directory-list">
        <div class="modal-folder-area modal-droppable" data-directory-id="0">
          <img src="" alt="" class="directory-img" />
          <span class="modal-folder-title">기본 폴더</span>
        </div>
      </div>
      <div class="modal-directory-divider"></div>
      <div class="modal-directory-add">
        <div class="modal-folder-area modal-droppable" id="directory-create-area">
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

  // 모달 폴더 리스트 내에 아이템 추가
  directoryInfos.forEach((directoryInfo) => {
    const folderArea = document.createElement('div')
    folderArea.className = 'modal-folder-area modal-droppable'
    folderArea.dataset.directoryId = directoryInfo.directoryId

    // 내부 구조 설정
    folderArea.innerHTML = `
      <img src="" alt="" class="directory-img"/>
      <span class="modal-folder-title"></span>
    `

    // 디렉토리 이름 설정
    const folderTitle = folderArea.querySelector('.modal-folder-title')
    folderTitle.innerText = directoryInfo.directoryName

    directoryList.appendChild(folderArea)
  })

  // 기본 폴더 아이콘 추가
  const imageElements = modal.querySelectorAll('.directory-img')

  imageElements.forEach((element) => {
    element.src = commonFolderIconUrl
  })

  // 폴더 만들기 아이콘 추가
  const createFolderIcon = modal.querySelector('.create-folder-icon')
  createFolderIcon.src = createFolderIconUrl

  // document.body.appendChild(modal)
  overlay.appendChild(modal)
  document.body.appendChild(overlay)
}

function showCreateFolderModal(event) {
  // 기존 모달 제거
  const existingModal = document.querySelector('#create-folder-modal')

  if (existingModal) {
    existingModal.remove()
  }

  // 모달 요소 추가
  const modal = document.createElement('div')

  const { left, top } = calcModalCoord(event.clientX, event.clientY)

  // 모달이 화면 밖으로 넘어가지 않도록 계산
  modal.style.top = `${top}px`
  modal.style.left = `${left}px`

  modal.id = 'create-folder-modal'

  // 폴더 만들기 모달 내부 구조 정의
  modal.innerHTML = `
    <input class="modal-create-folder-input" placeholder="새 폴더 이름을 입력하세요"></input>
    <img class="modal-create-folder-img" src="" alt="폴더 만들기" />
  `

  // 폴더 만들기 아이콘 추가
  const createFolderIcon = modal.querySelector('.modal-create-folder-img')
  createFolderIcon.src = createFolderIconUrl

  document.body.appendChild(modal)
}

// 현재 탭의 정보 url을 받아오기
// 탭 정보는 background.js 혹은 popup.js에서만 읽어올 수 있다.
// background.js에 메세지를 보내서 탭 정보를 받아온다

function getCurrentTab() {
  return new Promise((resolve, reject) => {
    chrome.runtime.sendMessage({ action: 'getCurrentTab' }, (response) => {
      if (response.tab) {
        console.log('현재 탭:', response.tab)
        resolve(response.tab.url)
      } else {
        console.log('탭 불러오기 실패')
        reject(new Error('탭 불러오기 실패'))
      }
    })
  })
}

// 알림 팝업 모달 띄우기
function showAlertModal(imgSrc, isSaved) {
  // 기존 모달 제거
  const existingModals = document.querySelectorAll('.alert-modal')
  existingModals.forEach((modal) => modal.remove())

  // 현재 시간으로 고유 ID 설정
  const modalId = 'alert-modal-' + Date.now()
  const modal = document.createElement('div')
  modal.id = modalId
  modal.className = 'alert-modal'

  modal.innerHTML = `
  <img src="" alt="" class="modal-response-img" />
  <div class="modal-response-box">
    <div class="modal-response-title-box">
      <img src="" alt="" class="modal-result-img" />
      <span class="modal-response-title"></span>
    </div>
    <span class="modal-response-text"></span>
  </div>
  `

  const responseImage = modal.querySelector('.modal-response-img')

  // 저장한 이미지 src로 띄우기
  // 현재는 임시로 logoUrl, 차후 API 완성되면 imgSrc 로 변경 ##################################################
  responseImage.src = logoUrl
  responseImage.alt = '저장 이미지'

  // 결과 이미지
  const resultImage = modal.querySelector('.modal-result-img')
  // 결과 제목
  const resultTitle = modal.querySelector('.modal-response-title')
  // 결과 메세지
  const resultMessage = modal.querySelector('.modal-response-text')

  if (isSaved === true) {
    // 성공시 동작
    resultImage.src = logoUrl
    resultTitle.innerText = '다운로드 완료'
    resultMessage.innerText = '이미지 저장이 완료됐어요'
  } else {
    resultImage.src = logoSadUrl
    resultTitle.innerText = '다운로드 실패'
    resultMessage.innerText = '이미지 저장을 실패했어요'
  }

  document.body.appendChild(modal)

  // 타이머 설정으로 알림창 삭제
  setTimeout(() => {
    const modalToRemove = document.getElementById(modalId)
    if (modalToRemove) {
      modalToRemove.remove()
    }
    // 2500 ms -> 2.5 초
  }, 2500)
}

function testImageAlert(imgSrc, isSaved) {
  showAlertModal(logoSadUrl, isSaved)
}
