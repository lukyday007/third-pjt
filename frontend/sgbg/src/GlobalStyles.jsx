import { createGlobalStyle } from "styled-components"

const GlobalStyle = createGlobalStyle`
  * {
    margin: 0;
    padding: 0;
    font-family: 'Pretendard';
    font-weight: 500;
  }

  *::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Edge */
  }
`

export default GlobalStyle
