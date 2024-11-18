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
import SettingPage from "./pages/SettingPage"
import { useContext, useEffect } from "react"
import { AppContext, AppProvider } from "./contexts/AppContext"
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
  MainContent: styled.div`
    flex: 1 0 auto;
    padding-top: 90px;
    overflow-y: auto;
  `,
}

function App() {
  const location = useLocation()
  const isLoginPage =
    location.pathname === "/login" || location.pathname === "/login-callback"
  const isSettingPage = location.pathname === "/setting"
  // 우클릭 이벤트 제한
  useEffect(() => {
    document.addEventListener("contextmenu", handleContextMenu)
  }, [])

  const handleContextMenu = (event) => {
    event.preventDefault()
  }

  const { isAuthenticated } = useContext(AppContext)

  if (!isAuthenticated && !isLoginPage) {
    return <Navigate to="/login" />
  }

  // if (isAuthenticated && isLoginPage) {
  //   return <Navigate to="/" />
  // }

  return (
    <div className="App">
      {/* <AppProvider> */}
      <GlobalStyle />
      <s.Container>
        {!isLoginPage && <SideBar />}
        {/* <SideBar /> */}
        <s.ContentArea>
          {!isLoginPage && !isSettingPage && <SearchBar />}
          {/* <SearchBar /> */}
          <s.MainContent>
            <Routes>
              <Route path="/" element={<MainPage />} />
              <Route path="image" element={<ImgPage />}>
                <Route path=":id" />
              </Route>
              <Route path="login" element={<LoginPage />} />
              <Route path="login-callback" element={<LoginCallBackPage />} />
              <Route path="setting" element={<SettingPage />} />
            </Routes>
          </s.MainContent>
        </s.ContentArea>
      </s.Container>
      {/* </AppProvider> */}
    </div>
  )
}

export default App
