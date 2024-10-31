import styled from "styled-components"
import SearchBox from "./SearchBox"
import ImgToggleButton from "./ImgToggleButton"

const s = {
  Container: styled.div`
    display: flex;
    /* padding: 20px 60px; */ // 홈일때
    padding: 20px; // 다른페이지
    align-items: center;
    gap: 50px;
  `,
  Title: styled.div``,
  SearchButton: styled.button`
    color: white;
    border-radius: 8px;
    padding: 14px 12px;
    background-color: black;
    font-size: 16px;
  `,
}

const SearchBar = () => {
  return (
    <s.Container>
      <s.Title>싱글벙글한 이미지</s.Title>
      <SearchBox />
      {/* <s.SearchButton>싱글벙글 검색</s.SearchButton> 홈에서 */}
      <ImgToggleButton />
    </s.Container>
  )
}
export default SearchBar
