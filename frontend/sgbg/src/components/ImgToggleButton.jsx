import React, { useContext } from "react"
import { AppContext } from "../contexts/AppContext"
import styled from "styled-components"

// ToggleContainer 스타일 정의
const ToggleContainer = styled.label`
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  min-width: 90px;
  padding: 6px;
  border: 3px solid #343434;
  border-radius: 20px;
  background: #343434;
  font-weight: bold;
  color: #343434;
  cursor: pointer;
  overflow: hidden;
  gap: 12px;
`

const ToggleBackground = styled.div`
  position: absolute;
  width: 50%;
  height: 100%;
  left: ${({ isLatest }) => (isLatest === 2 ? "50%" : "0%")};
  border-radius: 20px;
  background: white;
  transition: all 0.3s;
`

const ToggleText = styled.div`
  z-index: 1;
  font-size: 12px;
  text-align: center;
  color: ${({ isLatest, position }) =>
    (isLatest === 2 && position === "right") ||
    (isLatest === 0 && position === "left")
      ? "#343434"
      : "white"};
  transition: color 0.3s;
`

const ImgToggleButton = () => {
  const { isLatest, toggleLatest } = useContext(AppContext)

  return (
    <ToggleContainer onClick={toggleLatest}>
      <ToggleBackground isLatest={isLatest} />
      <ToggleText isLatest={isLatest} position="left">
        최신순
      </ToggleText>
      <ToggleText isLatest={isLatest} position="right">
        랜덤순
      </ToggleText>
    </ToggleContainer>
  )
}

export default ImgToggleButton
