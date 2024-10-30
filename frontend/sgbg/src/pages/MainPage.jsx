import styled from "styled-components"

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
    </s.Container>
  )
}

export default MainPage
