import MainPage from "./pages/MainPage"
import "./asset/pretendard.css"
import styled from "styled-components"
import GlobalStyle from "./GlobalStyles"
import SideBar from "./components/SideBar"
import SearchBar from "./components/SearchBar"
import ImgPage from "./pages/ImgPage"

const s = {
  Container: styled.div`
    display: flex;
    height: 100vh;
  `,
  ContentArea: styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
  `,
}

function App() {
  return (
    <div className="App">
      <GlobalStyle />
      <s.Container>
        <SideBar />
        <s.ContentArea>
          <SearchBar />
          {/* <MainPage /> */}
          <ImgPage />
        </s.ContentArea>
      </s.Container>
    </div>
  )
}

export default App
