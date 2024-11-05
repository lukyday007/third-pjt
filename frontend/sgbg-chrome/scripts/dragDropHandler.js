let startX = 0
let startY = 0
let isDragging = false
const thresholdDistance = 50

// 드래그 & 드롭 관련 기능 초기화
function initializeDragAndDrop() {
  document.addEventListener('dragstart', handleDragStart)
  document.addEventListener('drag', handleDrag)
  document.addEventListener('dragenter', handleDragEnter)
  document.addEventListener('dragover', handleDragOver)
  document.addEventListener('drop', handleDrop)
  document.addEventListener('dragleave', handleDragLeave)
  document.addEventListener('dragend', handleDragEnd)
}

function handleDragStart(event) {
  // 이미지라면 드래그 중 감지 변수를 true로 설정하고, 현재 커서 위치를 저장
  if (event.target.tagName === 'IMG') {
    isDragging = true
    startX = event.clientX
    startY = event.clientY
  }

  console.log(event.clientX, event.clientY)
}

function handleDrag(event) {
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
}

// 드래그 입장 이벤트 - droppable 영역을 확인해서 drag-over 클래스 추가
function handleDragEnter(event) {
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
}

// 드래그 오버 이벤트 - 드래구 중 커서 변경
function handleDragOver(event) {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    event.preventDefault()
    dropArea.style.cursor = 'copy'
  }
}

// 요소 드롭 이벤트
function handleDrop(event) {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    if (dropArea.id === 'directory-create-area') {
      fetchDirectoryList()
      showCreateFolderModal(event)
    } else if (dropArea.classList.contains('modal-folder-area') || dropArea.classList.contains('modal-drop-box')) {
      if (dropArea.dataset.directoryId === '0') {
        testImageAlert(undefined, false)
      } else {
        testImageAlert(undefined, true)
      }
    }
    event.preventDefault()
    console.log(dropArea)
    initDrag(dropArea)
  }
}

// 드래그 퇴장 이벤트 - 추가했던 drag-over 요소 삭제
function handleDragLeave(event) {
  // 드롭 가능 영역 감지
  const dropArea = event.target.closest('.modal-droppable')

  if (dropArea) {
    // 중첩 카운터에 -1을 하고, 카운터가 0이 되면 drag-over 클래스 삭제
    dropArea.dataset.dragCounter--

    if (dropArea.dataset.dragCounter == 0) {
      initDrag(dropArea)
    }
  }
}

// 드래그 끝 이벤트
// drop 이벤트 후에도 drag-end 이벤트가 반드시 호출된다.
// drop -> dragover -> dragend 순으로 종료되어
// dragend 이벤트에서 cursor 및 스타일 관련 이벤트 처리가 필요하다.
// 차후 여기에 이미지 저장 이벤트 추가
function handleDragEnd(event) {
  console.log('dragend', isDragging, startX, startY)
  isDragging = false
  startX = 0
  startY = 0
  console.log('dragend', isDragging, startX, startY)

  // 커서 및 drag-over 초기화
  document.querySelectorAll('.modal-droppable').forEach((dropArea) => {
    initDrag(dropArea)
  })

  // Overlay 삭제
  removeExistingOverlay()
}

initializeDragAndDrop()
