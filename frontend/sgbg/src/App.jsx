import MainPage from "./pages/MainPage"
import "./asset/pretendard.css"
import styled from "styled-components"
import GlobalStyle from "./GlobalStyles"
import SideBar from "./components/SideBar"
import SearchBar from "./components/SearchBar"
import ImgPage from "./pages/ImgPage"

import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"

const s = {
  Container: styled.div`
    display: flex;
    height: 100vh;
  `,
  ContentArea: styled.div`
    display: flex;
    flex-direction: column;
    flex: 1 0 auto;
    width: 100%;
  `,
  Test: styled.div`
    flex: 1 0 auto;
    display: flex;
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
          <s.Test>
            <Routes>
              <Route path="/" element={<MainPage />} />
              <Route path="/image" element={<ImgPage />} />
            </Routes>
          </s.Test>
        </s.ContentArea>
        {/* <BrowserRouter> */}
        {/* </BrowserRouter> */}
        {/* <MainPage /> */}
        {/* <ImgPage /> */}
      </s.Container>
    </div>
  )
}

export default App
