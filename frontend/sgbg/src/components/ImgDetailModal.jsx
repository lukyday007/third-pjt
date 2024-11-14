import { useEffect, useState } from "react"
import styled from "styled-components"
import { getImageDetail } from "../lib/api/image-api"

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
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: white;
    padding: 20px;
    border-radius: 8px;
    max-width: 800px;
    height: 600px;
    width: 100%;
    box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.1);
  `,
  CloseButton: styled.button`
    background: transparent;
    border: none;
    font-size: 18px;
    position: absolute;
    top: 10px;
    right: 10px;
    cursor: pointer;
  `,
  DetailImage: styled.img``,
  KeyWordArea: styled.div``,
  KeyWord: styled.div``,
  SourceText: styled.span`
    width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  `,
}

const ImgDetailModal = ({ imageId, onClose }) => {
  const [imageDetail, setImageDetail] = useState(null)

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

  const handleSourceClick = (url) => {
    window.open(url)
  }

  return (
    <s.Overlay onClick={onClose}>
      {imageDetail && (
        <s.Container onClick={(e) => e.stopPropagation()}>
          <s.CloseButton onClick={onClose}>X</s.CloseButton>
          <s.DetailImage
            src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${imageDetail?.imageUrl}`}
            alt="Selected"
          />
          {imageDetail?.keywords.map((keyword, index) => {
            return <s.KeyWord key={index}>{keyword}</s.KeyWord>
          })}
          <s.SourceText
            onClick={() => handleSourceClick(imageDetail.sourceUrl)}
          >
            {imageDetail.sourceUrl}
          </s.SourceText>
        </s.Container>
      )}
    </s.Overlay>
  )
}

export default ImgDetailModal
