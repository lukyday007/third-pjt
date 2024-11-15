import { useContext, useEffect } from "react"
import { useLocation, useNavigate } from "react-router-dom"
import { AppContext } from "../contexts/AppContext"

import styled from "styled-components"
import SearchBox from "./SearchBox"
import ImgToggleButton from "./ImgToggleButton"

const s = {
  Container: styled.div`
    display: flex;
    margin-left: 1px;
    padding: ${({ isHome }) => (isHome ? "20px 60px" : "20px")};
    align-items: center;
    gap: 50px;
    position: fixed;
    top: 0;
    left: ${({ isSideBarOpen }) => (isSideBarOpen ? "356px" : "76px")};
    right: 0;
    background-color: #fff;
    z-index: 10;
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
  const { folderName, searchKeywords, isSideBarOpen } = useContext(AppContext)
  const location = useLocation()
  const navigate = useNavigate()
  const isHome = location.pathname === "/"

  useEffect(() => {
    if (isHome && Boolean(searchKeywords.length)) {
      navigate("/image")
    }
  }, [searchKeywords])

  return (
    <s.Container isHome={isHome} isSideBarOpen={isSideBarOpen}>
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
