import React from "react"
import styled from "styled-components"
import HomeIcon from "../asset/images/SideBar/HomeIcon.svg?react"
import SideBarToggleIcon from "../asset/images/SideBar/SideBarToggleIcon.svg?react"

const s = {
  Container: styled.div`
    border: solid 1px #e1e3e1;
    width: 300px;
  `,
  PointerCursor: styled.span`
    cursor: pointer;
  `,
}

const SideBar = () => {
  return (
    <s.Container>
      <HomeIcon style={{ cursor: "pointer" }} />
      <SideBarToggleIcon style={{ cursor: "pointer" }} />
      <h1>야호</h1>
    </s.Container>
  )
}

export default SideBar
