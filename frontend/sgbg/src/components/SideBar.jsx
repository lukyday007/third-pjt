import React, { useEffect, useRef, useState } from "react"
import styled from "styled-components"
import HomeIcon from "../asset/images/SideBar/HomeIcon.svg?react"
import SideBarToggleIcon from "../asset/images/SideBar/SideBarToggleIcon.svg?react"

import AllImagesIcon from "../asset/images/SideBar/AllImagesIcon.svg?react"
import CommonFolderIcon from "../asset/images/SideBar/CommonFolderIcon.svg?react"
import CreateFolderIcon from "../asset/images/SideBar/CreateFolderIcon.svg?react"
import DefaultFolderIcon from "../asset/images/SideBar/DefaultFolderIcon.svg?react"
import SettingsIcon from "../asset/images/SideBar/SettingsIcon.svg?react"
import TrashBinIcon from "../asset/images/SideBar/TrashBinIcon.svg?react"

import TestImage from "../asset/images/TestImage.png"
import { useNavigate } from "react-router-dom"
import {
  deleteDirectory,
  getDirectoryList,
  patchDirectoryName,
  postCreateDirectory,
} from "../lib/api/directory-api"
import CreateFolderModal from "./CreateFolderModal"
import { getUserInfo } from "../lib/api/user-api"
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
  DragFolderArea: styled.div`
    position: fixed;
    display: flex;
    align-items: center;
    margin-top: 8px;
    cursor: pointer;
    left: ${(props) => `${props.$positionX}px`};
    top: ${(props) => `${props.$positionY - 20}px`};
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
  DragDropArea: styled.div`
    height: 2px;
    width: 100%;
  `,
}

const SideBar = () => {
  const [isSideBarOpen, setIsSideBarOpen] = useState(true)
  // 디렉토리 리스트 정보
  const [directoryInfos, setDirectoryInfos] = useState([])
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
  const [changeNameTarget, setChangeNameTarget] = useState(0)
  const [changeNameText, setChangeNameText] = useState("")
  const [prevNameText, setPrevNameText] = useState("")
  // 선택할 input 태그를 위한 ref
  const inputRef = useRef(null)
  // 드래그 요소 관리
  const [isDragging, setIsDragging] = useState(false)
  const [dragPosition, setDragPosition] = useState({ X: 0, Y: 0 })
  const [dragDirectoryName, setDragDirectoryName] = useState("")
  const dragElementRef = useRef(null)

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
  const handleFolderClick = (id) => {
    navigate(`/image/${id}`)
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
    setDirectoryInfos(fetchedDirectoryInfos)
    console.log(directoryInfos, "디렉토리 정보조회")
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
  const handleRightClick = (event, id) => {
    // 우클릭 이벤트 제한
    event.preventDefault()

    const currentX = event.clientX
    const currentY = event.clientY

    if (currentX && currentY) {
      setRightClickModalPosition({ X: currentX, Y: currentY })
    }

    if (id) {
      setSelectedDirectory(id)
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
  const handleDragFolder = (event) => {
    console.log(event)
  }

  // 드래그 시작 관리
  const handleDragStart = (event, directoryInfo) => {
    // 고스트 이미지 제거
    const img = new Image()
    img.src = ""
    event.dataTransfer.setDragImage(img, 0, 0)
    setDragDirectoryName(directoryInfo.directoryName)
    setIsDragging(true)
    handleDragFolder(event)
  }

  // 드래그 중 위치 업데이트
  const handleDrag = (event) => {
    if (isDragging) {
      setDragPosition({ X: event.clientX, Y: event.clientY })
    }
  }

  // 드래그 종료 핸들러
  const handleDragEnd = () => {
    setIsDragging(false)
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
          <s.FolderArea onClick={() => handleFolderClick(0)}>
            <DefaultFolderIcon />
            <s.FolderTitle>기본폴더</s.FolderTitle>
          </s.FolderArea>
          <s.FolderCaption>내 폴더</s.FolderCaption>
          <s.DragDropArea />
          {isRightClickModalOpen && (
            <FolderRightClickModal
              toggleFunction={toggleRightClickModal}
              position={rightClickModalPosition}
              changeFunction={handleChangeDirectoryName}
              openFunction={() => handleFolderClick(selectedDirectory)}
              deleteFunction={handleDeleteDirectory}
            />
          )}
          {directoryInfos &&
            directoryInfos.map((directoryInfo) => (
              <>
                <s.FolderArea
                  key={directoryInfo.directoryId}
                  onClick={() => handleFolderClick(directoryInfo.directoryId)}
                  onContextMenu={(event) =>
                    handleRightClick(event, directoryInfo.directoryId)
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
                <s.DragDropArea />
              </>
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
          <s.FolderArea>
            <SettingsIcon />
            <s.FolderTitle>설정</s.FolderTitle>
          </s.FolderArea>

          {isDragging && dragPosition.X && dragPosition.Y && (
            <s.DragFolderArea
              $positionX={dragPosition.X}
              $positionY={dragPosition.Y}
              key={`dragFolder`}
            >
              <DefaultFolderIcon />
              <s.FolderTitle>{dragDirectoryName}</s.FolderTitle>
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
