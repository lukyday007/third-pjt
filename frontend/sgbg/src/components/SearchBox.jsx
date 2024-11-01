import React, { useState } from "react"
import styled from "styled-components"
import SearchIcon from "../asset/images/SearchBox/searchIcon.svg?react"
import KeywordIcon from "../asset/images/searchBox/keywordIcon.svg?react"
import KeywordCancleIcon from "../asset/images/searchBox/keywordCancleIcon.svg?react"

// 더미 데이터 예시
const dummyKeywords = [
  "강",
  "강아지",
  "강낭콩",
  "강릉",
  "강원과학고",
  "강풍",
  "가보",
]

const s = {
  Container: styled.div`
    display: flex;
    flex-direction: column;
    padding: 16px 12px;
    background-color: rgba(245, 245, 245, 1);
    border-radius: 16px;
    gap: 10px;
    flex: 1 0 auto;
    position: relative;
  `,
  SearchArea: styled.div`
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  `,
  SearchInput: styled.input`
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
  ResultsArea: styled.div`
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 60px;
    left: 0;
    width: 100%;
    background-color: rgba(245, 245, 245, 1);
    border-radius: 8px;
    max-height: 300px;
    overflow-y: auto;
    z-index: 10;
  `,
  ResultItem: styled.div`
    display: flex;
    align-items: center;
    padding: 8px 12px;
    gap: 5px;
    cursor: pointer;
    &:hover {
      background-color: #f0f0f0;
    }
  `,
}

const SearchBox = () => {
  const [query, setQuery] = useState("")
  const [filteredKeywords, setFilteredKeywords] = useState([])

  const handleSearch = (e) => {
    const input = e.target.value
    setQuery(input)

    // 검색어가 입력될 때마다 더미 데이터를 필터링
    if (input) {
      const results = dummyKeywords.filter((keyword) => keyword.includes(input))
      setFilteredKeywords(results)
    } else {
      setFilteredKeywords([])
    }
  }

  return (
    <s.Container>
      <s.SearchArea>
        <SearchIcon />
        <s.SearchInput
          type="search"
          placeholder="싱글벙글한 이미지"
          value={query}
          onChange={handleSearch}
        />
      </s.SearchArea>
      {filteredKeywords.length > 0 && (
        <s.ResultsArea>
          {filteredKeywords.map((keyword, index) => (
            <s.ResultItem key={index}>
              <KeywordIcon />
              {keyword}
            </s.ResultItem>
          ))}
        </s.ResultsArea>
      )}
    </s.Container>
  )
}

export default SearchBox
