import styled from "styled-components"
import SearchIcon from "../asset/images/SearchBox/searchIcon.svg?react"

const s = {
  Container: styled.div`
    box-shadow: 0px 0px 8px rgb(0, 0, 0, 0.6);
    display: flex;
    padding: 18px 13px;
    gap: 10px;
    border-radius: 16px;
    width: 100%;
  `,
  Input: styled.input`
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
      <s.Input type="search" />
    </s.Container>
  )
}

export default SearchBox
