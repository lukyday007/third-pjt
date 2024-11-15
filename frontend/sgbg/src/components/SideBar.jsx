import React, {
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
  useContext,
} from "react"
import { useNavigate } from "react-router-dom"
import styled from "styled-components"
import { AppContext } from "../contexts/AppContext"

import HomeIcon from "../asset/images/SideBar/HomeIcon.svg?react"
import SideBarToggleIcon from "../asset/images/SideBar/SideBarToggleIcon.svg?react"

import AllImagesIcon from "../asset/images/SideBar/AllImagesIcon.svg?react"
import CommonFolderIcon from "../asset/images/SideBar/CommonFolderIcon.svg?react"
import CreateFolderIcon from "../asset/images/SideBar/CreateFolderIcon.svg?react"
import DefaultFolderIcon from "../asset/images/SideBar/DefaultFolderIcon.svg?react"
import SettingsIcon from "../asset/images/SideBar/SettingsIcon.svg?react"
import TrashBinIcon from "../asset/images/SideBar/TrashBinIcon.svg?react"

import TestImage from "../asset/images/TestImage.png" // ~보라미~

import {
  deleteDirectory,
  getDirectoryList,
  patchDirectoryName,
  patchDirectorySequence,
  postCreateDirectory,
} from "../lib/api/directory-api"
import { getUserInfo } from "../lib/api/user-api"
import CreateFolderModal from "./CreateFolderModal"
import FolderRightClickModal from "./FolderRightClickModal"

const s = {
  Test: styled.div`
    width: 356px;
    min-width: 356px;
    display: ${(props) => (props.$isopen === true ? "flex" : "none")};
  `,
  Testt: styled.div`
    width: 76px;
    min-width: 76px;
    display: ${(props) => (props.$isopen === true ? "none" : "flex")};
  `,
  Container: styled.div`
    z-index: 10;
    position: fixed;
    background-color: #ffffff;
    border-right: solid 1px #e1e3e1;
    min-width: 300px;
    width: 300px;
    padding: 28px;
    height: 100%;
    flex-direction: column;
    display: ${(props) => (props.$isopen === true ? "flex" : "none")};
    overflow-y: scroll;
  `,
  HomeIconArea: styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
  `,
  Email: styled.span`
    font-size: 14px;
    margin-left: 8px;
  `,
  UserInfoArea: styled.div`
    display: flex;
    align-items: center;
    margin-top: 24px;
  `,
  FolderCaption: styled.div`
    font-size: 14px;
    color: #838383;
    margin-top: 32px;
  `,
  FolderTitle: styled.span`
    font-size: 16px;
    color: #000000;
    margin-left: 8px;
  `,
  FolderInput: styled.input`
    padding: 0 10px;
    height: 100%;
    font-size: 16px;
    border: 1px solid #cccccc;
    border-radius: 4px;
    margin-left: 8px;
  `,
  FolderArea: styled.div`
    display: flex;
    align-items: center;
    margin-top: 8px;
    cursor: pointer;
  `,
  DragFolderArea: styled.div.attrs((props) => ({
    style: {
      left: `${props.positionx}px`,
      top: `${props.positiony - 20}px`,
    },
  }))`
    position: fixed;
    display: flex;
    align-items: center;
    margin-top: 8px;
    cursor: pointer;
    width: 280px;
    background-color: #ffffff;
    border-radius: 4px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  `,
  ClosedSidebar: styled.div`
    z-index: 10;
    background-color: #ffffff;
    border-right: solid 1px #e1e3e1;
    position: fixed;
    height: 100%;
    padding: 35px 28px 0 28px;
    display: ${(props) => (props.$isopen === true ? "none" : "inline")};
  `,
  DragDropArea: styled.div.attrs((props) => ({
    style: {
      backgroundColor: props.isClosest ? "#cccccc" : "",
    },
  }))`
    height: 2px;
    width: 100%;
  `,
}

const SideBar = () => {
  // 디렉토리 이름 상태
  const { setFolderName } = useContext(AppContext)
  // 사이드바 열림 여부
  const { isSideBarOpen, setIsSideBarOpen } = useContext(AppContext)
  // const [isSideBarOpen, setIsSideBarOpen] = useState(true)
  // 디렉토리 리스트 정보v
  const [directoryInfos, setDirectoryInfos] = useState([])
  const [directorySequence, setDirectorySequence] = useState([])
  // 새폴더 모달 열림 여부
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  // 유저 정보
  const [userInfo, setUserInfo] = useState({})
  // 우클릭 모달 열림 여부
  const [isRightClickModalOpen, setIsRightClickModalOpen] = useState(false)
  const [rightClickModalPosition, setRightClickModalPosition] = useState({
    X: 0,
    Y: 0,
  })
  // 현재 선택된 디렉토리
  const [selectedDirectory, setSelectedDirectory] = useState(0)
  const [selectedDirectoryName, setSelectedDirectoryName] = useState("")
  const [changeNameTarget, setChangeNameTarget] = useState(0)
  const [changeNameText, setChangeNameText] = useState("")
  const [prevNameText, setPrevNameText] = useState("")
  // 선택할 input 태그를 위한 ref
  const inputRef = useRef(null)
  // 드래그 요소 관리
  const [isDragging, setIsDragging] = useState(false)
  const [dragPosition, setDragPosition] = useState({ X: 0, Y: 0 })
  const [dragDirectoryInfo, setDragDirectoryInfo] = useState(null)
  // 드래그 드롭 관련 변수
  const dragDropAreaRefs = useRef([])
  const [dropAreaPositions, setDropAreaPositions] = useState([])
  const [closestDropAreaIndex, setClosestDropAreaIndex] = useState(null)

  // 컴포넌트가 로드될 때 요청
  useEffect(() => {
    fetchDirectoryInfos()
    fetchUserInfo()
  }, [])

  useEffect(() => {}, [dragPosition])

  // changeNameTarget 변경을 감지해서 focus 이동
  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus()
    }
  }, [changeNameTarget])

  // directoryInfos가 업데이트 된 후 위치 초기화
  useLayoutEffect(() => {
    if (directoryInfos && directoryInfos.length > 0) {
      initDropAreaPositions()
    }
  }, [directoryInfos])

  const toggleSideBar = () => {
    setIsSideBarOpen((prev) => !prev)
  }

  const toggleCreateModal = () => {
    setIsCreateModalOpen((prev) => !prev)
  }

  const toggleRightClickModal = () => {
    setIsRightClickModalOpen((prev) => !prev)
  }

  const navigate = useNavigate()

  const handleHomeButtonClick = () => {
    navigate(`/`)
  }

  const handleImageButtonClick = () => {
    navigate(`/image`)
  }
  const handleEmailClick = () => {
    navigate(`/login`)
  }

  const handleBasicClick = () => {
    navigate(`/login`)
  }

  const handleFolderClick = (id, name) => {
    console.log("1")
    setFolderName(name)
    navigate(`/image/${id}`)
    // console.log(id, name, "아디랑 이름")
  }

  const handleSettingClick = () => {
    navigate(`/setting`)
  }
  const createNewFolder = async (directoryName) => {
    toggleCreateModal()

    let fetchedData = await postCreateDirectory(
      { directoryName },
      (resp) => {
        return resp.data
      },
      (error) => {
        console.log("error", error)
      }
    )

    if (!fetchedData) {
      return
    }

    const fetchedDirectoryInfos = fetchedData.directories

    setDirectoryInfos(fetchedDirectoryInfos)
  }

  // 디렉토리 목록 조회 함수
  const fetchDirectoryInfos = async () => {
    let fetchedData = await getDirectoryList(
      (resp) => {
        return resp.data
      },
      (error) => {
        console.log("error", error)
      }
    )

    if (!fetchedData) {
      return
    }

    const fetchedDirectoryInfos = fetchedData.directories
    // 새로운 디렉토리 순서 저장
    const newDirectorySequence = fetchedDirectoryInfos.map((directoryInfo) => {
      return directoryInfo.directoryId
    })

    setDirectoryInfos(fetchedDirectoryInfos)
    setDirectorySequence(newDirectorySequence)
  }

  // 유저 정보 조회 함수
  const fetchUserInfo = async () => {
    getUserInfo(
      (resp) => {
        setUserInfo(resp.data)
      },
      (error) => {
        console.log("error", error)
      }
    )
  }

  // 우클릭 관리
  const handleRightClick = (event, id, name) => {
    console.log(directoryInfos, "디렉토리 정보조회")
    // 우클릭 이벤트 제한
    event.preventDefault()

    const currentX = event.clientX
    const currentY = event.clientY

    if (currentX && currentY) {
      setRightClickModalPosition({ X: currentX, Y: currentY })
    }

    if (id) {
      setSelectedDirectory(id)
      setSelectedDirectoryName(name)
      toggleRightClickModal()
    }
  }

  // 폴더 삭제
  const handleDeleteDirectory = async () => {
    if (!confirm("폴더를 삭제하시겠습니까?")) return

    await deleteDirectory(selectedDirectory, (resp) => {
      alert("완료")
      fetchDirectoryInfos()
    })
    setSelectedDirectory(0)
  }

  // 폴더 이름 바꾸기
  const handleChangeDirectoryName = async () => {
    const targetDirectoryName = directoryInfos.find(
      (directory) => directory.directoryId === selectedDirectory
    )?.directoryName

    setPrevNameText(targetDirectoryName)
    setChangeNameText(targetDirectoryName)
    setChangeNameTarget(selectedDirectory)
  }

  // 폴더명 input 변경 이벤트
  const handleChangeInput = (event) => {
    const newText = event.target.value
    setChangeNameText(newText)
  }

  // 드래그 관리
  const handleDragFolder = (event) => {}

  // 드래그 시작 관리
  const handleDragStart = (event, directoryInfo) => {
    // 고스트 이미지 제거
    const img = new Image()
    img.src = ""
    event.dataTransfer.setDragImage(img, 0, 0)
    setDragDirectoryInfo(directoryInfo)
    setIsDragging(true)
    handleDragFolder(event)
  }

  // 드래그 중 위치 업데이트
  const handleDrag = (event) => {
    if (isDragging) {
      const newPosition = { X: event.clientX, Y: event.clientY }
      if (newPosition.X !== 0 && newPosition.Y !== 0) {
        setDragPosition(newPosition)
        findClosestDropArea(newPosition)
      }
    }
  }

  // 드래그 종료 핸들러
  const handleDragEnd = () => {
    if (dragDirectoryInfo && closestDropAreaIndex !== null) {
      const draggedDirectoryId = dragDirectoryInfo.directoryId
      const dropIndex = closestDropAreaIndex

      updateDirectorySequence(draggedDirectoryId, dropIndex)
    }

    setIsDragging(false)
    setClosestDropAreaIndex(null)
  }

  // 디렉토리 순서 변경 처리 함수
  const updateDirectorySequence = async (draggedDirectoryId, dropIndex) => {
    // 기존 sequence 복사
    const newSequence = [...directorySequence]
    // sequence에서 현재 드래그한 디렉토리의 index값 찾기
    const draggedIndex = newSequence.indexOf(draggedDirectoryId)

    // 없는 경우 리턴
    if (draggedIndex === -1) {
      return
    }

    // 드래그한 디렉토리 제거
    newSequence.splice(draggedIndex, 1)

    // dropIndex 조정
    let adjustedDropIndex = dropIndex
    if (draggedIndex < dropIndex) {
      adjustedDropIndex = dropIndex - 1
    }

    // 위치 변경이 없는 경우 끝
    if (adjustedDropIndex === draggedIndex) {
      return
    }

    // 새로운 위치에 디렉토리 삽입
    newSequence.splice(adjustedDropIndex, 0, draggedDirectoryId)

    const response = await patchDirectorySequence(
      { directorySequence: newSequence },
      (resp) => {
        return resp
      },
      (error) => {
        console.log(error)
      }
    )
    const fetchedDirectoryInfos = response.data?.directories

    console.log(fetchedDirectoryInfos, newSequence)

    // 반환값이 없거나 빈 배열인 경우 retrun
    if (!fetchedDirectoryInfos) {
      return
    }

    // 변경된 반환값이 올 경우 수정
    setDirectoryInfos(fetchedDirectoryInfos)
    setDirectorySequence(newSequence)
  }

  // 폴더 이름 변경 키다운 이벤트
  const handleKeyDown = async (event) => {
    if (event.key === "Enter") {
      if (changeNameText === prevNameText) return

      const requestDto = {
        directoryId: changeNameTarget,
        directoryName: changeNameText,
      }

      await patchDirectoryName(requestDto)
      await fetchDirectoryInfos()
      initChangeDirectoryName()
    }

    if (event.key === "Escape") {
      initChangeDirectoryName()
    }
  }

  // 이름 변경 이벤트 초기화
  const initChangeDirectoryName = () => {
    setPrevNameText("")
    setChangeNameText("")
    setChangeNameTarget(0)
  }

  // 각 DropArea의 위치 감지
  const initDropAreaPositions = () => {
    // 각 dropArea 감지 및 위치 저장
    // ref로 참조되지 못한 null 값 제거
    const validAreas = dragDropAreaRefs.current.filter((area) => area !== null)

    const newDropAreaPositions = validAreas.map((area) => {
      const rect = area.getBoundingClientRect()
      return { element: area, x: rect.x, y: rect.y }
    })
    setDropAreaPositions(newDropAreaPositions)
  }

  // 가장 가까운 DropArea를 찾아서 반환하는 함수
  const findClosestDropArea = (position) => {
    let closestAreaIndex = null
    let minDistance = Infinity

    dropAreaPositions.forEach(({ element, x, y }, index) => {
      const distance = Math.hypot(position.X - x, position.Y - y)
      if (distance < minDistance) {
        minDistance = distance
        closestAreaIndex = index
      }
    })
    setClosestDropAreaIndex(closestAreaIndex)
  }

  return (
    <>
      <s.Test $isopen={isSideBarOpen}>
        <s.Container $isopen={isSideBarOpen}>
          <s.HomeIconArea>
            <HomeIcon
              onClick={handleHomeButtonClick}
              style={{ cursor: "pointer" }}
            />
            <SideBarToggleIcon
              onClick={toggleSideBar}
              style={{ cursor: "pointer" }}
            />
          </s.HomeIconArea>
          <s.UserInfoArea>
            <img
              onClick={handleImageButtonClick}
              src={userInfo.profileImagePath}
              alt="borami"
              width="36px"
              style={{ borderRadius: "50%" }}
            />
            <s.Email onClick={handleEmailClick}>{userInfo.email}</s.Email>
          </s.UserInfoArea>
          <s.FolderCaption onClick={handleBasicClick}>기본</s.FolderCaption>
          <s.FolderArea>
            <AllImagesIcon />
            <s.FolderTitle>전체 이미지</s.FolderTitle>
          </s.FolderArea>
          <s.FolderArea onClick={() => handleFolderClick(0, "기본폴더")}>
            <DefaultFolderIcon />
            <s.FolderTitle>기본폴더</s.FolderTitle>
          </s.FolderArea>
          <s.FolderCaption>내 폴더</s.FolderCaption>
          <s.DragDropArea
            ref={(el) => (dragDropAreaRefs.current[0] = el)}
            isClosest={closestDropAreaIndex === 0}
          />
          {isRightClickModalOpen && (
            <FolderRightClickModal
              toggleFunction={toggleRightClickModal}
              position={rightClickModalPosition}
              changeFunction={handleChangeDirectoryName}
              openFunction={() =>
                handleFolderClick(selectedDirectory, selectedDirectoryName)
              }
              deleteFunction={handleDeleteDirectory}
            />
          )}
          {directoryInfos &&
            directoryInfos.map((directoryInfo, index) => (
              <React.Fragment key={index}>
                <s.FolderArea
                  key={directoryInfo.directoryId}
                  onClick={() =>
                    handleFolderClick(
                      directoryInfo.directoryId,
                      directoryInfo.directoryName
                    )
                  }
                  onContextMenu={(event) =>
                    handleRightClick(
                      event,
                      directoryInfo.directoryId,
                      directoryInfo.directoryName
                    )
                  }
                  onDragStart={(event) => handleDragStart(event, directoryInfo)}
                  onDrag={(event) => handleDrag(event)}
                  onDragEnd={handleDragEnd}
                  draggable
                >
                  <CommonFolderIcon />
                  {directoryInfo.directoryId === changeNameTarget ? (
                    <s.FolderInput
                      onClick={(event) => event.stopPropagation()}
                      value={changeNameText}
                      onChange={handleChangeInput}
                      ref={inputRef}
                      onKeyDown={handleKeyDown}
                      onBlur={() => {
                        setChangeNameText("")
                        setChangeNameTarget(0)
                      }}
                    />
                  ) : (
                    <s.FolderTitle>{directoryInfo.directoryName}</s.FolderTitle>
                  )}
                </s.FolderArea>
                <s.DragDropArea
                  key={index}
                  ref={(el) => (dragDropAreaRefs.current[index + 1] = el)}
                  isClosest={closestDropAreaIndex === index + 1}
                />
              </React.Fragment>
            ))}
          <s.FolderCaption>관리</s.FolderCaption>
          <s.FolderArea onClick={toggleCreateModal}>
            <CreateFolderIcon />
            <s.FolderTitle>폴더 만들기</s.FolderTitle>
          </s.FolderArea>
          {isCreateModalOpen && (
            <CreateFolderModal
              toggleFunction={toggleCreateModal}
              createFunction={createNewFolder}
            />
          )}
          <s.FolderArea>
            <TrashBinIcon />
            <s.FolderTitle>휴지통</s.FolderTitle>
          </s.FolderArea>
          <s.FolderArea onClick={handleSettingClick}>
            <SettingsIcon />
            <s.FolderTitle>설정</s.FolderTitle>
          </s.FolderArea>

          {isDragging && dragPosition.X !== 0 && dragPosition.Y !== 0 && (
            <s.DragFolderArea
              positionx={dragPosition.X}
              positiony={dragPosition.Y}
              key={`dragFolder`}
            >
              <DefaultFolderIcon />
              <s.FolderTitle>{dragDirectoryInfo?.directoryName}</s.FolderTitle>
            </s.DragFolderArea>
          )}
        </s.Container>
      </s.Test>

      <s.Testt $isopen={isSideBarOpen}>
        <s.ClosedSidebar $isopen={isSideBarOpen}>
          <SideBarToggleIcon
            onClick={toggleSideBar}
            style={{ cursor: "pointer" }}
          />
        </s.ClosedSidebar>
      </s.Testt>
    </>
  )
}

export default SideBar
