import styled from "styled-components"
import ImgList from "../components/ImgList"

const s = {
  Container: styled.div`
    display: flex;
    flex-direction: column;
    padding: 0 20px;
    gap: 20px;
  `,
  Header: styled.div`
    display: flex;
    margin: 0;
    width: 100%;
    display: flex;
    gap: 50px;
    align-items: center;
  `,
  Title: styled.div``,
}

const ImgPage = () => {
  return (
    <s.Container>
      <ImgList />
    </s.Container>
  )
}
export default ImgPage
