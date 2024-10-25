import MainPage from "./pages/MainPage"
import "./asset/pretendard.css"
import styled from "styled-components"
import GlobalStyle from "./GlobalStyles"

function App() {
  return (
    <div className="App">
      <GlobalStyle />
      <MainPage />
    </div>
  )
}

export default App
