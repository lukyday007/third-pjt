import { useEffect, useRef, useState } from "react"
import styled from "styled-components"

const s = {
  Overlay: styled.div`
    position: fixed;

    left: 0;
    top: 0;

    display: flex;
    justify-content: center;
    align-items: center;

    width: 100vw;
    height: 100vh;
    background-image: linear-gradient(
      rgba(51, 51, 51, 0.5),
      rgba(51, 51, 51, 0.5)
    );
  `,
  Container: styled.div`
    padding: 24px 0 0;
    border-radius: 8px;
    width: 320px;
    background-color: #ffffff;
  `,
  Title: styled.div`
    padding: 0 24px;
    font-size: 24px;
  `,
  InputArea: styled.div`
    margin: 16px 0;
    padding: 0 24px;
  `,
  InputBox: styled.input`
    padding: 0 16px;
    height: 48px;
    width: calc(100% - 40px);
    border: 1px solid black;
    border-radius: 8px;
    font-size: 24px;
  `,
  ButtonArea: styled.div`
    padding: 0 20px 24px 24px;
    display: flex;
    justify-content: end;
    right: 0;
  `,
  Button: styled.div`
    margin: 0 10px;
    display: flex;
    justify-content: center;
    align-items: center;

    width: 60px;
    height: 40px;
    border: 1px solid #ffffff;

    cursor: pointer;
  `,
}

const CreateFolderModal = ({ toggleFunction, createFunction }) => {
  const [directoryName, setDirectoryName] = useState("")
  const inputRef = useRef(null)

  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus()
    }
  }, [])

  // input 요소 감지 이벤트
  const handleInputChange = (event) => {
    setDirectoryName(event.target.value)
  }

  // 엔터 이벤트 감지
  const handleKeyDown = (event) => {
    if (event.key === "Enter") {
      createFunction(directoryName)
    }

    if (event.key === "Escape") {
      toggleFunction()
    }
  }

  return (
    <s.Overlay>
      <s.Container>
        <s.Title>새 폴더</s.Title>
        <s.InputArea>
          <s.InputBox
            placeholder="제목없는 폴더"
            value={directoryName}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            ref={inputRef}
          ></s.InputBox>
        </s.InputArea>
        <s.ButtonArea>
          <s.Button onClick={toggleFunction}>취소</s.Button>
          <s.Button onClick={() => createFunction(directoryName)}>
            만들기
          </s.Button>
        </s.ButtonArea>
      </s.Container>
    </s.Overlay>
  )
}

export default CreateFolderModal
