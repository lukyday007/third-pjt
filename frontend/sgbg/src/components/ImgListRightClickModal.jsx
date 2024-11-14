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
    margin: 4px;
    padding: 5px;

    background-color: #ffffff;

    font-size: 18px;

    cursor: pointer;

    &:hover {
      font-weight: bold;
      color: #000000;
      background-color: #cccccc;
    }
  `,
  MenuText: styled.div``,
}

const ImgListRightClickModal = ({
  position,
  openFunction,
  toggleFunction,
  deleteFunction,
}) => {
  return (
    <s.Overlay onClick={toggleFunction} onContextMenu={toggleFunction}>
      <s.Container $positionX={position.X} $positionY={position.Y}>
        <s.MenuTextArea onClick={openFunction}>열기</s.MenuTextArea>
        <s.MenuTextArea onClick={deleteFunction}>이미지 삭제</s.MenuTextArea>
      </s.Container>
    </s.Overlay>
  )
}

export default ImgListRightClickModal
