import React, { useState } from "react"
import styled from "styled-components"
import HomeIcon from "../asset/images/SideBar/HomeIcon.svg?react"
import SideBarToggleIcon from "../asset/images/SideBar/SideBarToggleIcon.svg?react"

import AllImagesIcon from "../asset/images/SideBar/AllImagesIcon.svg?react"
import CommonFolderIcon from "../asset/images/SideBar/CommonFolderIcon.svg?react"
import CreateFolderIcon from "../asset/images/SideBar/CreateFolderIcon.svg?react"
import DefaultFolderIcon from "../asset/images/SideBar/DefaultFolderIcon.svg?react"
import SettingsIcon from "../asset/images/SideBar/SettingsIcon.svg?react"
import TrashBinIcon from "../asset/images/SideBar/TrashBinIcon.svg?react"

import TestImage from "../asset/images/TestImage.svg?react"

const s = {
  Container: styled.div`
    border-right: solid 1px #e1e3e1;
    width: 300px;
    padding: 28px;
    flex-direction: column;
    display: ${(props) => (props.$isopen === true ? "flex" : "none")};
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
  FolderArea: styled.div`
    display: flex;
    align-items: center;
    margin-top: 8px;
    cursor: pointer;
  `,
  ClosedSidebar: styled.div`
    padding: 35px 28px 0 28px;
    display: ${(props) => (props.$isopen === true ? "none" : "inline")};
  `,
}

const SideBar = () => {
  const [isSideBarOpen, setIsSideBarOpen] = useState(true)

  const toggleSideBar = () => {
    setIsSideBarOpen((prev) => !prev)
  }

  return (
    <>
      <s.Container $isopen={isSideBarOpen}>
        <s.HomeIconArea>
          <HomeIcon style={{ cursor: "pointer" }} />
          <SideBarToggleIcon
            onClick={toggleSideBar}
            style={{ cursor: "pointer" }}
          />
        </s.HomeIconArea>
        <s.UserInfoArea>
          <TestImage />
          <s.Email>kke0402@naver.com</s.Email>
        </s.UserInfoArea>
        <s.FolderCaption>기본</s.FolderCaption>
        <s.FolderArea>
          <AllImagesIcon />
          <s.FolderTitle>전체 이미지</s.FolderTitle>
        </s.FolderArea>
        <s.FolderArea>
          <DefaultFolderIcon />
          <s.FolderTitle>기본폴더</s.FolderTitle>
        </s.FolderArea>
        <s.FolderCaption>내 폴더</s.FolderCaption>
        <s.FolderArea>
          <CommonFolderIcon />
          <s.FolderTitle>싱글벙글한 이미지</s.FolderTitle>
        </s.FolderArea>
        <s.FolderCaption>관리</s.FolderCaption>
        <s.FolderArea>
          <CreateFolderIcon />
          <s.FolderTitle>폴더 만들기</s.FolderTitle>
        </s.FolderArea>
        <s.FolderArea>
          <TrashBinIcon />
          <s.FolderTitle>휴지통</s.FolderTitle>
        </s.FolderArea>
        <s.FolderArea style={{ marginTop: "auto" }}>
          <SettingsIcon />
          <s.FolderTitle>설정</s.FolderTitle>
        </s.FolderArea>
      </s.Container>
      <s.ClosedSidebar $isopen={isSideBarOpen}>
        <SideBarToggleIcon
          onClick={toggleSideBar}
          style={{ cursor: "pointer" }}
        />
      </s.ClosedSidebar>
    </>
  )
}

export default SideBar
