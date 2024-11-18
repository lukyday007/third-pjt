import React, { useContext, useEffect, useState } from "react"
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
import { AppContext } from "../contexts/AppContext"

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

const rankingColors = [
  { background: "rgba(251, 216, 210, 1)", text: "rgba(239, 92, 68, 1)" },
  { background: "rgba(255, 231, 194, 1)", text: "rgba(255, 153, 0, 1)" },
  { background: "rgba(255, 238, 194, 1)", text: "rgba(255, 184, 0, 1)" },
  { background: "rgba(206, 238, 223, 1)", text: "rgba(50, 182, 122, 1)" },
  { background: "rgba(210, 236, 251, 1)", text: "rgba(67, 176, 239, 1)" },
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
    height: 40px;
  `,
  TitleButtonLight: styled.button`
    background-color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    padding: 8px 16px;
    border-radius: 8px;
    margin-top: 36px;
    cursor: pointer;
    height: 40px;
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
    cursor: pointer;
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
  ImageArea: styled.div`
    display: flex;
  `,
  NewImageArea: styled.div``,
}

const MainPage = () => {
  const navigate = useNavigate()
  const [rankingKeyword, setRankingKeyword] = useState()
  const [newImage, setNewImage] = useState({})
  const [randomImage, setRandomImage] = useState({})
  const params = new URLSearchParams(window.location.search)
  const code = encodeURIComponent(params.get("code"))
  const googleLogin = async (code) => {
    googleSignIn(
      code,
      (resp) => {
        console.log("resp", resp.data["access-token"])
        localStorage.setItem("accessToken", resp.data["access-token"])
        window.dispatchEvent(new Event("localStorageUpdate"))
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
        // console.log(resp.data.keywords, "결과야 잘 오니")
      },
      (error) => {
        console.log("error", error)
      }
    )
  }
  useEffect(() => {
    fetchRankingKeword()
    fetchNewImage()
    fetchRandomImage()
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
      0,
      (resp) => {
        console.log(resp.data.imageList[0])
        setNewImage(resp.data.imageList[0])
      },
      (error) => {
        console.error(error)
      }
    )
  }

  const fetchRandomImage = async () => {
    getFeedImages(
      1,
      1,
      "",
      2,
      (resp) => {
        console.log(resp.data.imageList[0])
        setRandomImage(resp.data.imageList[0])
      },
      (error) => {
        console.error(error)
      }
    )
  }

  const handleLoginClick = () => {
    navigate("/login")
  }

  const handleNewImageClick = () => {
    navigate("/image")
    setIsLatest(0)
  }

  const handleRandomImageClick = () => {
    navigate("/image")
    setIsLatest(2)
  }

  const handleKeywordClick = (keyword, background, text) => {
    navigate("/image")
    setKeywords(keyword)
    setIsLatest(0)
    setSearchKeywords([{ keyword, background, text }])
  }

  const {
    isLatest,
    toggleLatest,
    setIsLatest,
    setKeywords,
    setSearchKeywords,
  } = useContext(AppContext)

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
              const RankingColor = rankingColors[index] || rankingColors[0]
              return (
                <s.Keyword
                  key={index}
                  onClick={() =>
                    handleKeywordClick(
                      keyword.keyword,
                      RankingColor.background,
                      RankingColor.text
                    )
                  }
                >
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
        <s.ImageArea style={{ marginLeft: "64px" }}>
          <s.NewImageArea style={{ marginRight: "64px" }}>
            <s.TitleButton
              onClick={handleNewImageClick}
              style={{ marginBottom: "16px" }}
            >
              최신 싱글벙글
            </s.TitleButton>
            <div>
              <img
                onClick={handleNewImageClick}
                height={"300px"}
                width={"450px"}
                src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${newImage.imageUrl}`}
                style={{
                  objectFit: "contain",
                  objectPosition: "left center",
                  cursor: "pointer",
                }}
              />
            </div>
          </s.NewImageArea>
          <s.NewImageArea>
            <s.TitleButtonLight
              onClick={handleRandomImageClick}
              style={{ marginBottom: "16px" }}
            >
              랜덤 싱글벙글
            </s.TitleButtonLight>
            <div>
              <img
                onClick={handleRandomImageClick}
                height={"300px"}
                width={"450px"}
                src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${randomImage.imageUrl}`}
                style={{
                  objectFit: "contain",
                  objectPosition: "left center",
                  cursor: "pointer",
                }}
              />
            </div>
          </s.NewImageArea>
        </s.ImageArea>
      </div>
    </>
  )
}

export default MainPage
