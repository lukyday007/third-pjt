import React, { useState } from "react"
import styled from "styled-components"

const s = {
  ToggleContainer: styled.div`
    display: flex;
    border: 2px solid #ccc;
    border-radius: 20px;
    overflow: hidden;
  `,
  ToggleItem: styled.button`
    padding: 10px 20px;
    font-size: 12px;
    background-color: ${(props) => (props.active ? "black" : "white")};
    color: ${(props) => (props.active ? "white" : "black")};
    border: none;
    border-radius: 20px;
    cursor: pointer;
    transition: background-color 0.3s, color 0.3s;

    &:hover {
      background-color: ${(props) => (props.active ? "#333" : "#ddd")};
    }
  `,
}

const ImgToggle = () => {
  const [active, setActive] = useState("latest")

  return (
    <s.ToggleContainer>
      <s.ToggleItem
        active={active === "latest"}
        onClick={() => setActive("latest")}
      >
        최신순
      </s.ToggleItem>
      <s.ToggleItem
        active={active === "random"}
        onClick={() => setActive("random")}
      >
        랜덤순
      </s.ToggleItem>
    </s.ToggleContainer>
  )
}

export default ImgToggle
