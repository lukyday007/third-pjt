import styled from "styled-components"
import ImgPage from "./ImgPage"

const s = {
  Containter: styled.div`
    border: solid 1px red;
    margin: 0;
  `,
}

const MainPage = () => {
  console.log("")

  return (
    <s.Containter>
      <h1>Mainpage</h1>
      <p>asdf</p>
      <span>asdf</span>
      -------------
      <ImgPage />
    </s.Containter>
  )
}

export default MainPage
