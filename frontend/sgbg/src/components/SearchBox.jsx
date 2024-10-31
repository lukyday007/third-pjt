import styled from "styled-components"
import SearchIcon from "../asset/images/SearchBox/searchIcon.svg?react"

const s = {
  Container: styled.div`
    background-color: rgba(245, 245, 245, 1);
    display: flex;
    padding: 16px 12px;
    gap: 10px;
    flex: 1 0 auto;
    border-radius: 16px;
  `,
  SearchBox: styled.input`
    background-color: inherit;
    border-color: transparent;
    outline: none;
    font-size: 16px;
    width: 100%;

    /* 크롬 기본 스타일 초기화 (기본 x 스타일 제거) */
    &::-webkit-search-decoration,
    &::-webkit-search-cancel-button,
    &::-webkit-search-results-button,
    &::-webkit-search-results-decoration {
      display: none;
    }
  `,
}

const SearchBox = () => {
  return (
    <s.Container>
      <SearchIcon />
      <s.SearchBox type="search" placeholder="싱글벙글한 이미지" />
    </s.Container>
  )
}

export default SearchBox
