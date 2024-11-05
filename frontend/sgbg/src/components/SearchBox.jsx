import React, { useState } from "react"
import styled from "styled-components"
import SearchIcon from "../asset/images/SearchBox/searchIcon.svg?react"
import KeywordIcon from "../asset/images/SearchBox/keywordIcon.svg?react"
import KeywordCancleIcon from "../asset/images/SearchBox/keywordCancleIcon.svg?react"

// 더미 데이터 예시
const dummyKeywords = [
  "강",
  "강아지",
  "강한 용사 여호와",
  "강릉",
  "강원과학고",
  "강풍",
  "가보",
]

// 태그 컬러 일단 여기에...
const colorPairs = [
  { background: "rgba(251, 216, 210, 1)", text: "rgba(239, 92, 68, 1)" },
  { background: "rgba(252, 229, 214, 1)", text: "rgba(240, 148, 86, 1)" },
  { background: "rgba(255, 231, 194, 1)", text: "rgba(255, 153, 0, 1)" },
  { background: "rgba(255, 238, 194, 1)", text: "rgba(255, 184, 0, 1)" },
  { background: "rgba(228, 251, 210, 1)", text: "rgba(143, 239, 67, 1)" },
  { background: "rgba(206, 238, 223, 1)", text: "rgba(50, 182, 122, 1)" },
  { background: "rgba(194, 228, 214, 1)", text: "rgba(4, 145, 86, 1)" },
  { background: "rgba(194, 244, 255, 1)", text: "rgba(0, 209, 255, 1)" },
  { background: "rgba(210, 236, 251, 1)", text: "rgba(67, 176, 239, 1)" },
  { background: "rgba(199, 233, 249, 1)", text: "rgba(23, 165, 231, 1)" },
  { background: "rgba(210, 219, 251, 1)", text: "rgba(67, 105, 239, 1)" },
  { background: "rgba(225, 194, 255, 1)", text: "rgba(128, 0, 255, 1)" },
  { background: "rgba(231, 210, 251, 1)", text: "rgba(153, 67, 239, 1)" },
  { background: "rgba(250, 210, 251, 1)", text: "rgba(235, 67, 239, 1)" },
  { background: "rgba(255, 194, 216, 1)", text: "rgba(255, 0, 92, 1)" },
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
  `,
  SearchInput: styled.input`
    background-color: inherit;
    border: none;
    outline: none;
    font-size: 16px;

    /* 크롬 기본 스타일 초기화 (기본 x 스타일 제거) */
    &::-webkit-search-decoration,
    &::-webkit-search-cancel-button,
    &::-webkit-search-results-button,
    &::-webkit-search-results-decoration {
      display: none;
    }
  `,

  KeywordArea: styled.div`
    display: flex;
    align-items: center;
    gap: 7px;
  `,
  KeywordItem: styled.div`
    display: flex;
    align-items: center;
    gap: 3px;
    white-space: nowrap;
    background-color: ${(props) => props.bgColor};
    color: ${(props) => props.textColor};
    padding: 5px 10px;
    border-radius: 8px;
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
    ${(props) =>
      props.first &&
      `
      background-color: #e0f7fa;
      font-weight: bold;
    `}
  `,
}

const SearchBox = () => {
  const [query, setQuery] = useState("")
  const [filteredKeywords, setFilteredKeywords] = useState([])
  const [searchKeywords, setSearchKeywords] = useState([])

  const handleSearch = (e) => {
    const input = e.target.value
    setQuery(input)

    // 검색어가 입력될 때마다 더미 데이터를 필터링
    // 나중에는 검색어 입력할때마다 api
    if (input) {
      const results = dummyKeywords.filter((keyword) => keyword.includes(input))
      setFilteredKeywords(results)
    } else {
      setFilteredKeywords([])
    }
  }

  // 엔터해서 키워드 검색
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && filteredKeywords.length > 0) {
      const firstKeyword = filteredKeywords[0]

      if (searchKeywords.includes(firstKeyword)) {
        setSearchKeywords(
          searchKeywords.filter((keyword) => keyword !== firstKeyword)
        )
      } else {
        const { background, text } = getRandomColorPair()
        setSearchKeywords([
          ...searchKeywords,
          { keyword: firstKeyword, background, text },
        ])
      }
      setQuery("")
      setFilteredKeywords([])
    } else if (
      e.key === "Backspace" &&
      query === "" &&
      searchKeywords.length > 0
    ) {
      setSearchKeywords(searchKeywords.slice(0, -1))
    }
  }

  const removeTag = () => {
    setSearchKeywords(searchKeywords.filter((_, i) => i !== index))
  }

  const getRandomColorPair = () => {
    const randomIndex = Math.floor(Math.random() * colorPairs.length)
    return colorPairs[randomIndex]
  }

  return (
    <s.Container>
      <s.SearchArea>
        <SearchIcon />
        {searchKeywords.length > 0 && (
          <s.KeywordArea>
            {searchKeywords.map((item, index) => {
              return (
                <s.KeywordItem
                  key={index}
                  bgColor={item.background}
                  textColor={item.text}
                >
                  <KeywordCancleIcon
                    onClick={removeTag}
                    bgColor={item.background}
                    textColor={item.text}
                  />
                  {item.keyword}
                </s.KeywordItem>
              )
            })}
          </s.KeywordArea>
        )}
        <s.SearchInput
          type="search"
          placeholder={searchKeywords.length > 0 ? "" : "싱글벙글한 이미지"}
          value={query}
          onChange={handleSearch}
          onKeyDown={handleKeyDown}
        />
      </s.SearchArea>
      {filteredKeywords.length > 0 && (
        <s.ResultsArea>
          {filteredKeywords.map((keyword, index) => (
            <s.ResultItem key={index} first={index === 0}>
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
