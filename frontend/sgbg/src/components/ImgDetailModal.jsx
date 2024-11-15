import { useContext, useEffect, useState } from "react"
import styled from "styled-components"
import { getImageDetail } from "../lib/api/image-api"
import { AppContext } from "../contexts/AppContext"
import { useParams } from "react-router-dom"

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
  Overlay: styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  `,
  Container: styled.div`
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: white;
    height: 800px;
    width: 700px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.1);
    overflow-y: hidden;
  `,
  CloseButton: styled.div`
    border: none;
    font-size: 18px;
    position: absolute;
    top: 10px;
    right: 10px;
    cursor: pointer;
  `,
  ImageArea: styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #cccccc;
    height: 600px;
    width: 600px;
  `,
  DetailImage: styled.img`
    max-height: 100%;
    max-width: 100%;
  `,
  InfoArea: styled.div`
    width: 650px;
    font-size: 18px;
  `,
  KeyWordArea: styled.div`
    margin: 10px 0;
    display: flex;
    align-items: center;
    width: 100%;
    height: 35px;
  `,
  KeyWordText: styled.div`
    width: 80px;
  `,
  KeyWord: styled.div`
    display: flex;
    align-items: center;
    margin-right: 5px;
    padding: 4px;
    gap: 5px;
    white-space: nowrap;
    background-color: #cccccc;
    color: #000000;
    padding: 5px 10px;
    border-radius: 8px;
    cursor: pointer;
  `,
  SourceArea: styled.div`
    display: flex;
    flex-direction: row;
    margin: 10px 0;
    width: 100%;
    height: 35px;
  `,
  SourceText: styled.div`
    width: 80px;
    white-space: nowrap;
  `,
  SourceUrl: styled.div`
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 550px;
    cursor: pointer;
  `,
  ButtonArea: styled.div`
    display: flex;
    flex-direction: row;

    width: 100%;
  `,
  Button: styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 10px;
    padding: 5px 10px;

    border-radius: 8px;
    background-color: #cccccc;
    cursor: pointer;
  `,
}

const ImgDetailModal = ({ imageId, onClose, saveFunction }) => {
  const [imageDetail, setImageDetail] = useState(null)
  // context 사용 : 키워드
  const { searchKeywords, setSearchKeywords } = useContext(AppContext)
  const parmas = useParams()

  useEffect(() => {
    fetchImageDetail(imageId)
  }, [imageId])

  useEffect(() => {
    // 컴포넌트 로드시 keydown 이벤트 리스너 추가
    window.addEventListener("keydown", handleKeyDown)

    return () => {
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [])

  const fetchImageDetail = async (id) => {
    const imageId = id
    try {
      const response = await getImageDetail(
        imageId,
        (resp) => {
          const data = resp.data
          return data
        },
        (error) => {
          console.log(error)
        }
      )
      setImageDetail(response)
    } catch (e) {
      console.log(e)
    }
  }

  const handleKeyDown = (event) => {
    if (event.key === "Escape") {
      onClose()
    }
  }

  // Url 클릭시 원본 주소 창 열기
  const handleSourceClick = (url) => {
    window.open(url)
  }

  // 키워드 클릭시 해당 키워드로 이동
  const handleKeyWordClick = (keyword) => {
    const { background, text } = getRandomColorPair()
    setSearchKeywords([{ keyword, background, text }])
    onClose()
  }

  // 키워드 색깔 랜덤으로 고르기
  const getRandomColorPair = () => {
    const randomIndex = Math.floor(Math.random() * colorPairs.length)
    return colorPairs[randomIndex]
  }

  return (
    <s.Overlay onClick={onClose}>
      {imageDetail && (
        <s.Container onClick={(e) => e.stopPropagation()}>
          <s.CloseButton onClick={onClose}>X</s.CloseButton>
          <s.ImageArea>
            <s.DetailImage
              src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${imageDetail?.imageUrl}`}
              alt="Selected"
            />
          </s.ImageArea>
          <s.InfoArea>
            <s.KeyWordArea>
              <s.KeyWordText>키워드</s.KeyWordText>

              {imageDetail?.keywords.map((keyword, index) => {
                return (
                  <s.KeyWord
                    key={index}
                    onClick={() => handleKeyWordClick(keyword)}
                  >
                    {keyword}
                  </s.KeyWord>
                )
              })}
            </s.KeyWordArea>
            <s.SourceArea>
              <s.SourceText>원본 Url</s.SourceText>
              <s.SourceUrl
                onClick={() => handleSourceClick(imageDetail.sourceUrl)}
              >
                {imageDetail.sourceUrl}
              </s.SourceUrl>
            </s.SourceArea>
            <s.ButtonArea>
              {!parmas.id ? (
                <s.Button onClick={saveFunction}>이미지 저장</s.Button>
              ) : (
                <></>
              )}
              <s.Button>URL 복사</s.Button>
              <s.Button>이미지 복사</s.Button>
            </s.ButtonArea>
          </s.InfoArea>
        </s.Container>
      )}
    </s.Overlay>
  )
}

export default ImgDetailModal
