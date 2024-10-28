import styled from "styled-components"
import ImgPage from "./ImgPage"

const s = {
  Container: styled.div`
    /* border: solid 1px red; */
    width: 100%;
    margin: 0;
  `,
}

const MainPage = () => {
  return (
    <s.Container>
      <h1>Mainpage</h1>
      <p>asdf</p>
      <span>asdf</span>
      -------------
      <ImgPage />
    </s.Container>
  )
}

export default MainPage
