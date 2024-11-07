import React, { useEffect } from "react"
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

const rotateImage = keyframes`
  100% {
    transform: rotate(360deg);
  }
`

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
}

const MainPage = () => {
  const navigate = useNavigate()

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

  useEffect(() => {
    if (code !== "null") {
      googleLogin(code)
    }
  }, [code])

  return (
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
        <s.Keyword>
          <FirstKeywordIcon />
          <s.KeywordText>싱글벙글</s.KeywordText>
          <KeywordIncreaseIcon style={{ marginLeft: "auto" }} />
        </s.Keyword>
        <s.Keyword>
          <SecondKeywordIcon />
          <s.KeywordText>싱글벙글</s.KeywordText>
          <KeywordIncreaseIcon style={{ marginLeft: "auto" }} />
        </s.Keyword>
        <s.Keyword>
          <ThirdKeywordIcon />
          <s.KeywordText>싱글벙글</s.KeywordText>
          <KeywordDecreaseIcon style={{ marginLeft: "auto" }} />
        </s.Keyword>
        <s.Keyword>
          <FourthKeywordIcon />
          <s.KeywordText>싱글벙글</s.KeywordText>
          <KeywordDecreaseIcon style={{ marginLeft: "auto" }} />
        </s.Keyword>
        <s.Keyword>
          <FifthKeywordIcon />
          <s.KeywordText>싱글벙글</s.KeywordText>
          <KeywordIncreaseIcon style={{ marginLeft: "auto" }} />
        </s.Keyword>
      </s.KeywordArea>
    </s.Container>
  )
}

export default MainPage
