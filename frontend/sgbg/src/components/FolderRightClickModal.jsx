import styled from "styled-components"

const s = {
  Overlay: styled.div`
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
  `,
  Container: styled.div`
    position: fixed;
    left: ${(props) => `${props.$positionX}px`};
    top: ${(props) => `${props.$positionY}px`};

    padding: 5px;

    border-radius: 4px;
    background-color: #ffffff;

    box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  `,
  MenuTextArea: styled.div`
    margin: 4px 0;
    padding: 5px 10px;

    background-color: #cccccc;

    cursor: pointer;
  `,
  MenuText: styled.div``,
}

const FolderRightClickModal = ({
  position,
  openFunction,
  toggleFunction,
  deleteFunction,
}) => {
  return (
    <s.Overlay onClick={toggleFunction} onContextMenu={toggleFunction}>
      <s.Container $positionX={position.X} $positionY={position.Y}>
        <s.MenuTextArea onClick={openFunction}>열기</s.MenuTextArea>
        <s.MenuTextArea>이름 바꾸기</s.MenuTextArea>
        <s.MenuTextArea onClick={deleteFunction}>폴더 삭제</s.MenuTextArea>
      </s.Container>
    </s.Overlay>
  )
}

export default FolderRightClickModal
