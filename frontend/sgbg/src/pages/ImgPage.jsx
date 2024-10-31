import styled from "styled-components"
import SearchBox from "../components/SearchBox"
import ImgToggleButton from "../components/ImgToggleButton"
import ImgList from "../components/ImgList"

const s = {
  Containter: styled.div`
    display: flex;
    width: 100%;
    flex-direction: column;
    padding: 0 20px;
    gap: 20px;
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
        <ImgToggleButton />
      </s.Header>
      <ImgList />
    </s.Containter>
  )
}
export default ImgPage
