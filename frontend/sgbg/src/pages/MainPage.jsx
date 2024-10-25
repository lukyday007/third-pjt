import styled from "styled-components"

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
    </s.Containter>
  )
}

export default MainPage
