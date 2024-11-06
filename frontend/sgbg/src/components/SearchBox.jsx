import React, { useState } from "react"
import styled from "styled-components"
import SearchIcon from "../asset/images/SearchBox/searchIcon.svg?react"
import KeywordIcon from "../asset/images/SearchBox/keywordIcon.svg?react"

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

// 으악 검색 태그 아이콘 일단 여기에
const KeywordCancleIcon = ({ fillPrimary, fillSecondary, onClick }) => (
  <svg
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    onClick={onClick}
    style={{ cursor: "pointer" }}
  >
    <path
      fillRule="evenodd"
      clipRule="evenodd"
      d="M16.4571 4.9014C12.3356 4.61367 8.20051 4.57818 4.07477 4.79511C2.12048 4.89797 0.668482 6.58654 0.406196 8.54426C0.248482 9.74426 0.10791 11.0128 0.10791 12.3157C0.10791 13.6185 0.248482 14.8871 0.40791 16.0854C0.668482 18.0431 2.1222 19.7317 4.07477 19.8345C8.22334 20.0523 12.2759 20.0163 16.4553 19.7283C17.136 19.6811 17.7869 19.4309 18.3239 19.01C19.4659 18.1294 20.5499 17.176 21.5691 16.1557C22.1416 15.5694 22.6902 14.9403 23.0999 14.3214C23.4925 13.7265 23.8388 13.0237 23.8388 12.3157C23.8388 11.6077 23.4925 10.9031 23.0999 10.31C22.6471 9.65281 22.1346 9.03878 21.5691 8.47569C20.5499 7.45557 19.4665 6.50162 18.3256 5.61969C17.7736 5.19111 17.1256 4.94769 16.4571 4.89969V4.9014Z"
      fill={fillPrimary} // 첫 번째 색상
    />
    <path
      fillRule="evenodd"
      clipRule="evenodd"
      d="M10.8 11.1023C10.9228 10.9837 11.0207 10.8418 11.088 10.6849C11.1553 10.528 11.1907 10.3592 11.1921 10.1885C11.1935 10.0178 11.1609 9.84852 11.0961 9.69054C11.0314 9.53256 10.9359 9.38906 10.8151 9.2684C10.6943 9.14773 10.5507 9.05233 10.3927 8.98776C10.2346 8.92319 10.0653 8.89074 9.89461 8.8923C9.7239 8.89386 9.5552 8.92941 9.39837 8.99687C9.24154 9.06432 9.09972 9.16234 8.98117 9.28519L7.76746 10.4972L6.55546 9.28519C6.43775 9.15887 6.29581 9.05755 6.1381 8.98728C5.98038 8.91701 5.81013 8.87922 5.6375 8.87618C5.46486 8.87313 5.29339 8.90489 5.13329 8.96955C4.9732 9.03422 4.82777 9.13046 4.70568 9.25255C4.58359 9.37464 4.48734 9.52007 4.42268 9.68017C4.35801 9.84026 4.32626 10.0117 4.3293 10.1844C4.33235 10.357 4.37013 10.5273 4.44041 10.685C4.51068 10.8427 4.612 10.9846 4.73832 11.1023L5.95032 12.316L4.73832 13.528C4.51121 13.7718 4.38757 14.0941 4.39344 14.4272C4.39932 14.7603 4.53426 15.0781 4.76982 15.3137C5.00539 15.5493 5.32319 15.6842 5.65628 15.6901C5.98936 15.6959 6.31173 15.5723 6.55546 15.3452L7.76746 14.1332L8.97946 15.3452C9.09877 15.4647 9.24047 15.5596 9.39644 15.6244C9.55242 15.6892 9.71964 15.7226 9.88853 15.7227C10.0574 15.7229 10.2247 15.6898 10.3808 15.6253C10.5369 15.5608 10.6788 15.4662 10.7983 15.3469C10.9179 15.2276 11.0127 15.0859 11.0775 14.9299C11.1423 14.7739 11.1757 14.6067 11.1759 14.4378C11.176 14.2689 11.1429 14.1017 11.0784 13.9456C11.0139 13.7895 10.9193 13.6476 10.8 13.528L9.58803 12.316L10.8 11.1023Z"
      fill={fillSecondary} // 두 번째 색상
    />
  </svg>
)

const s = {
  Container: styled.div`
    display: flex;
    flex-direction: column;
    padding: 8px 12px;
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
    padding: 7px;
    height: 34px;

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
    gap: 5px;
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
    z-index: 1;
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

  const removeTag = (index) => {
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
                    onClick={() => removeTag(index)}
                    fillPrimary={item.text}
                    fillSecondary={item.background}
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
