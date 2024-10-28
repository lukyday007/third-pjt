import styled from "styled-components"
import SearchBox from "../components/SearchBox"
import ImgToggle from "../components/ImgToggle"
import ImgList from "../components/ImgList"
const s = {
  Containter: styled.div`
    border: solid 1px blue;
    padding: 0 10px;
  `,
  Header: styled.div`
    display: flex;
    margin: 0;
    width: 100%;
    display: flex;
    gap: 50px;
    align-items: center;
  `,
  Title: styled.div``,
}

const ImgPage = () => {
  return (
    <s.Containter>
      <s.Header>
        <s.Title>싱글벙글한 이미지</s.Title>
        <SearchBox />
        <ImgToggle />
      </s.Header>
      <ImgList />
    </s.Containter>
  )
}
export default ImgPage
