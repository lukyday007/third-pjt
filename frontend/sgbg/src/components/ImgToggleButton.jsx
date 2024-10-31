import React, { useState } from "react"
import styled from "styled-components"

// ToggleContainer 스타일 정의
const ToggleContainer = styled.label`
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  width: fit-content;
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
  left: ${({ isChecked }) => (isChecked ? "50%" : "0%")};
  border-radius: 20px;
  background: white;
  transition: all 0.3s;
`

const ToggleText = styled.div`
  z-index: 1;
  font-size: 12px;
  text-align: center;
  color: ${({ isChecked, position }) =>
    (isChecked && position === "right") || (!isChecked && position === "left")
      ? "#343434"
      : "white"};
  transition: color 0.3s;
`

const ImgToggleButton = () => {
  const [isChecked, setIsChecked] = useState(false)

  const toggle = () => setIsChecked((prev) => !prev)

  return (
    <ToggleContainer onClick={toggle}>
      <ToggleBackground isChecked={isChecked} />
      <ToggleText isChecked={isChecked} position="left">
        최신순
      </ToggleText>
      <ToggleText isChecked={isChecked} position="right">
        랜덤순
      </ToggleText>
    </ToggleContainer>
  )
}

export default ImgToggleButton
