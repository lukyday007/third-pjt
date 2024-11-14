import React, { useEffect, useState } from "react"
import styled, { keyframes } from "styled-components"
import SingBung from "../asset/images/MainPage/SingBung.svg?react"
import FirstKeywordIcon from "../asset/images/MainPage/FirstKeywordIcon.svg?react"
import SecondKeywordIcon from "../asset/images/MainPage/SecondKeywordIcon.svg?react"
import ThirdKeywordIcon from "../asset/images/MainPage/ThirdKeywordIcon.svg?react"
import FourthKeywordIcon from "../asset/images/MainPage/FourthKeywordIcon.svg?react"
import FifthKeywordIcon from "../asset/images/MainPage/FifthKeywordIcon.svg?react"
import KeywordIncreaseIcon from "../asset/images/MainPage/KeywordIncreaseIcon.svg?react"
import KeywordDecreaseIcon from "../asset/images/MainPage/KeywordDecreaseIcon.svg?react"
import { replace, useLocation, useNavigate } from "react-router-dom"
import { googleSignIn } from "../lib/api/user-api"
import { getFeedImages } from "../lib/api/image-api"
import { getRankingKeywordList } from "../lib/api/keyword-api"

const rotateImage = keyframes`
  100% {
    transform: rotate(360deg);
  }
`

// 키워드 아이콘 목록
const keywordIcons = [
  FirstKeywordIcon,
  SecondKeywordIcon,
  ThirdKeywordIcon,
  FourthKeywordIcon,
  FifthKeywordIcon,
]

const s = {
  Container: styled.div`
    flex: 1 0 auto;
    display: flex;
    padding: 60px;
  `,
  Title: styled.div`
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 16px;
  `,
  TextWithTitle: styled.div`
    font-size: 20px;
    font-weight: 400;
    color: #666666;
    margin-bottom: 8px;
  `,
  TitleTextArea: styled.div``,
  TitleArea: styled.div`
    display: flex;
  `,
  TitleButton: styled.button`
    background-color: #000000;
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    padding: 8px 16px;
    border-radius: 8px;
    margin-top: 36px;
    cursor: pointer;
  `,
  TitleButtonLight: styled.button`
    background-color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    padding: 8px 16px;
    border-radius: 8px;
    margin-top: 36px;
    cursor: pointer;
  `,
  SingBungMove: styled.div`
    width: fit-content;
    height: fit-content;
    &:hover {
      animation: ${rotateImage} 1s linear infinite;
      transform-origin: 50% 50%;
    }
  `,
  KeywordTitle: styled.div`
    font-size: 16px;
    font-weight: 700;
    margin-bottom: 12px;
    margin-left: 4px;
  `,
  Keyword: styled.div`
    display: flex;
    align-items: center;
    padding: 12px;
    padding-right: 16px;
    border: solid 1px #e1e3e1;
    border-radius: 12px;
    width: 240px;
    margin-bottom: 16px;
  `,
  KeywordText: styled.span`
    font-size: 16px;
    margin-left: 12px;
  `,
  KeywordArea: styled.div`
    display: flex;
    flex-direction: column;
    margin-left: auto;
  `,
  ImageArea: styled.div``,
}

const MainPage = () => {
  const navigate = useNavigate()
  const [rankingKeyword, setRankingKeyword] = useState()
  const params = new URLSearchParams(window.location.search)
  const code = encodeURIComponent(params.get("code"))
  const googleLogin = async (code) => {
    googleSignIn(
      code,
      (resp) => {
        console.log("resp", resp.data["access-token"])
        localStorage.setItem("accessToken", resp.data["access-token"])
        navigate("/", { replace: true })
        history.replaceState(null, "", "/#/")
      },
      (error) => {
        console.log("error", error)
      }
    )
  }

  const fetchRankingKeword = async () => {
    getRankingKeywordList(
      (resp) => {
        setRankingKeyword(resp.data.keywords)
        console.log(resp.data.keywords, "결과야 잘 오니")
      },
      (error) => {
        console.log("error", error)
      }
    )
  }
  useEffect(() => {
    fetchRankingKeword()
  }, [])

  useEffect(() => {
    if (code !== "null") {
      googleLogin(code)
    }
  }, [code])

  const fetchNewImage = async () => {
    getFeedImages(
      1,
      1,
      "",
      1,
      (resp) => {
        console.log(resp.data)
      },
      (error) => {
        console.error(error)
      }
    )
  }

  const handleLoginClick = () => {
    navigate("/login")
  }

  return (
    <>
      <div>
        <s.Container>
          <s.TitleArea>
            <s.TitleTextArea>
              <s.Title>싱글벙글한 이미지를 찾아보아요</s.Title>
              <s.TextWithTitle>
                싱글벙글 이미지들을 키워드로 검색하고,
              </s.TextWithTitle>
              <s.TextWithTitle>
                실시간 인기 키워드와 랜덤 이미지로 더 많은 즐거움을 만나보세요.
              </s.TextWithTitle>
              <s.TitleButton>최신 싱글벙글</s.TitleButton>
            </s.TitleTextArea>
            <s.SingBungMove>
              <SingBung />
            </s.SingBungMove>
          </s.TitleArea>
          <s.KeywordArea>
            <s.KeywordTitle>실시간 싱글벙글</s.KeywordTitle>
            {/*실시간 검색 랭킹 */}
            {rankingKeyword?.map((keyword, index) => {
              // 아이콘 컴포넌트 선택
              const KeywordIcon = keywordIcons[index] || keywordIcons[0]
              return (
                <s.Keyword key={index}>
                  <KeywordIcon />
                  <s.KeywordText>{keyword.keyword}</s.KeywordText>
                  {keyword.isState === "up" ? (
                    <KeywordIncreaseIcon style={{ marginLeft: "auto" }} />
                  ) : keyword.isState === "down" ? (
                    <KeywordDecreaseIcon style={{ marginLeft: "auto" }} />
                  ) : null}
                </s.Keyword>
              )
            })}
          </s.KeywordArea>
        </s.Container>
        <s.ImageArea>
          <s.TitleButton>최신 싱글벙글</s.TitleButton>
          <s.TitleButtonLight>랜덤 싱글벙글</s.TitleButtonLight>
          <s.TitleButton onClick={handleLoginClick}>
            로그인하는버튼 (임시)
          </s.TitleButton>
        </s.ImageArea>
      </div>
    </>
  )
}

export default MainPage
