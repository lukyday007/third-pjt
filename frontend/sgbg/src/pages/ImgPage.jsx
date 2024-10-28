import styled from "styled-components"
import SearchBox from "../components/SearchBox"
const s = {
  Containter: styled.div`
    border: solid 1px blue;
    margin: 0;
    width: 100%;
    display: flex;
    gap: 15px;
  `,
  Title: styled.div`
    border: solid 1px red;
    max-width: ;
  `,
}

const ImgPage = () => {
  return (
    <s.Containter>
      <s.Title>싱글벙글 페이지</s.Title>
      <SearchBox />
      <p>이미지페이지</p>
    </s.Containter>
  )
}
export default ImgPage
