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
  Input: styled.input`
    background-color: inherit;
    border-color: transparent;
    outline: none;
    font-size: 16px;
    width: 100%;
  `,
}

const SearchBox = () => {
  return (
    <s.Container>
      <SearchIcon />
      <s.Input type="search" placeholder="싱글벙글한 이미지" />
    </s.Container>
  )
}

export default SearchBox
