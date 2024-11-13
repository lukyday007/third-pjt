import { useLocation } from "react-router-dom"
import styled from "styled-components"
import SearchBox from "./SearchBox"
import ImgToggleButton from "./ImgToggleButton"

const s = {
  Container: styled.div`
    display: flex;
    padding: ${({ isHome }) => (isHome ? "20px 60px" : "20px")};
    align-items: center;
    gap: 50px;
  `,
  Title: styled.div`
    white-space: nowrap;
    overflow: hidden;
    font-size: 16px;
    max-width: 500px;
  `,

  SearchButton: styled.button`
    color: white;
    border-radius: 8px;
    padding: 14px 12px;
    background-color: black;
    font-size: 16px;
  `,
}

const SearchBar = () => {
  const location = useLocation()
  const isHome = location.pathname === "/"
  return (
    <s.Container isHome={isHome}>
      {!isHome && <s.Title>폴더가 길어지면 검색창이 줄어들어요</s.Title>}
      <SearchBox />
      {isHome ? (
        <s.SearchButton>싱글벙글 검색</s.SearchButton>
      ) : (
        <ImgToggleButton />
      )}
    </s.Container>
  )
}
export default SearchBar
