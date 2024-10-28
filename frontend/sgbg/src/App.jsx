import MainPage from "./pages/MainPage"
import "./asset/pretendard.css"
import styled from "styled-components"
import GlobalStyle from "./GlobalStyles"
import SideBar from "./components/SideBar"

const s = {
  Container: styled.div`
    display: flex;
    height: 100vh;
  `,
}

function App() {
  return (
    <div className="App">
      <GlobalStyle />
      <s.Container>
        <SideBar />
        <MainPage />
      </s.Container>
    </div>
  )
}

export default App
