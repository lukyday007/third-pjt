import styled from "styled-components"
import Searchbar from "../components/Searchbar"
const s = {
  Containter: styled.div`
    border: solid 1px blue;
    margin: 0;
  `,
}

const ImgPage = () => {
  return (
    <s.Containter>
      <Searchbar />
      <p>이미지페이지</p>
    </s.Containter>
  )
}
export default ImgPage
