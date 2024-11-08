import MainPage from "./pages/MainPage"
import "./asset/pretendard.css"
import styled from "styled-components"
import GlobalStyle from "./GlobalStyles"
import SideBar from "./components/SideBar"
import SearchBar from "./components/SearchBar"
import ImgPage from "./pages/ImgPage"
import { useLocation, Navigate, Route, Routes } from "react-router-dom"
import LoginPage from "./pages/LoginPage"
import LoginCallBackPage from "./pages/LoginCallBackPage"

const s = {
  Container: styled.div`
    display: flex;
    height: 100vh;
  `,
  ContentArea: styled.div`
    display: flex;
    flex: 1 0 auto;
    flex-direction: column;
  `,
}

function App() {
  const location = useLocation()
  const isLoginPage =
    location.pathname === "/login" || location.pathname === "/login-callback"
  return (
    <div className="App">
      <GlobalStyle />
      <s.Container>
        {!isLoginPage && <SideBar />}
        {/* <SideBar /> */}
        <s.ContentArea>
          {!isLoginPage && <SearchBar />}
          {/* <SearchBar /> */}
          <Routes>
            <Route path="/" element={<MainPage />} />
            <Route path="image" element={<ImgPage />}>
              <Route path=":id" />
            </Route>
            <Route path="login" element={<LoginPage />} />
            <Route path="login-callback" element={<LoginCallBackPage />} />
          </Routes>
        </s.ContentArea>
      </s.Container>
    </div>
  )
}

export default App
