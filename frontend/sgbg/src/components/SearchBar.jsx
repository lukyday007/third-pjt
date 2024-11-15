import { useContext, useEffect } from "react"
import { useLocation, useNavigate } from "react-router-dom"
import { AppContext } from "../contexts/AppContext"

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
  const { folderName, searchKeywords } = useContext(AppContext)
  const location = useLocation()
  const navigate = useNavigate()
  const isHome = location.pathname === "/"

  useEffect(() => {
    if (isHome && Boolean(searchKeywords.length)) {
      navigate("/image")
    }
  }, [searchKeywords])

  return (
    <s.Container isHome={isHome}>
      {!isHome && <s.Title>{folderName}</s.Title>}
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
