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
    background: white;
    padding: 20px;
    border-radius: 8px;
    max-width: 500px;
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
}

const ImgDetailModal = ({ imageId, onClose }) => {
  const [imageDetail, setImageDetail] = useState(null)

  useEffect(() => {
    fetchImageDetail(imageId)
  }, [])

  const fetchImageDetail = async (id) => {
    const imageId = id
    try {
      const response = await getImageDetail(imageId, (resp) => {
        const data = resp.data
        return data
      })

      setImageDetail(response)
    } catch (e) {
      console.log(e)
    }
  }

  return (
    <s.Overlay onClick={onClose}>
      {imageDetail && (
        <s.Container onClick={(e) => e.stopPropagation()}>
          <s.CloseButton onClick={onClose}>X</s.CloseButton>
          <div>
            <img
              src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${imageDetail?.imageUrl}`}
              alt="Selected"
            />
            <p>이미지 세부 정보</p>
          </div>
        </s.Container>
      )}
    </s.Overlay>
  )
}

export default ImgDetailModal
