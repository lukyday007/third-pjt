import { useState, useEffect } from "react"
import styled from "styled-components"
import { MasonryInfiniteGrid } from "@egjs/react-infinitegrid"
import "./styles.css"
import { getMyImages } from "../lib/api/image-api"
import { useParams } from "react-router-dom"
import ImgDetailModal from "./ImgDetailModal"

const s = {
  Image: styled.img.attrs((props) => ({
    "data-is-selected": props.isSelected ? "true" : "false",
  }))`
    box-sizing: border-box;
    width: 100%;
    border-radius: 8px;
    border: solid 1px rgba(229, 229, 229, 1);
    border: ${(props) =>
      props.isSelected ? "3px solid rgba(255, 184, 0, 1)" : "none"};
    &:hover {
      border: ${(props) =>
        props.isSelected
          ? "3px solid rgba(255, 184, 0, 1)"
          : "3px solid rgba(255, 238, 194, 1)"};
    }
  `,
}

function getItemsWithImages(images, groupKey) {
  return images.map((image, index) => ({
    groupKey,
    key: groupKey * 100 + index,
    imageUrl: image.imageUrl,
    imageId: image.imageId,
  }))
}

const Item = ({ imageUrl, isSelected, onClick }) => (
  <div className="item" onClick={onClick}>
    <div className="thumbnail">
      <s.Image
        src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${imageUrl}`}
        alt="User image"
        isSelected={isSelected}
      />
    </div>
  </div>
)

const ImgList = () => {
  const [selectedImageId, setSelectedImageId] = useState(null)
  const [items, setItems] = useState([])
  const [currentPage, setCurrentPage] = useState(1)
  const [prevPage, setPrevPage] = useState(0)
  const [totalPage, setTotalPage] = useState(null)
  const [isFetching, setIsFetching] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [selectedImage, setSelectedImage] = useState(null)

  const params = useParams()

  const fetchMyImages = async () => {
    if (isFetching || (totalPage !== null && currentPage > totalPage)) return
    setIsFetching(true)

    getMyImages(
      params.id,
      currentPage,
      10,
      "",
      0,
      false,
      (resp) => {
        const imageList = resp.data.imageList
        const totalPage = resp.data.totalPage
        console.log("response", resp.data, "currentPage", currentPage)
        const newItems = getItemsWithImages(imageList, currentPage)
        setItems((prevItems) => [...prevItems, ...newItems])
        setCurrentPage((prevPage) => prevPage + 1)
        setTotalPage(totalPage)
        setIsFetching(false)
      },
      (error) => {
        console.log("error", error)
        setIsFetching(false)
      }
    )
  }

  useEffect(() => {
    setItems([])
    setCurrentPage(1)
    setTotalPage(null)
  }, [params.id])

  // 선택 이미지 변경시 알림
  useEffect(() => {
    console.log(selectedImageId)
  }, selectedImageId)

  // 이미지 클릭
  const handleImageClick = (image) => {
    setSelectedImage(image)
    setIsModalOpen(true)
    setSelectedImageId(image.key) // 원래 이미지 클릭에 있던 코드
  }

  // 모달 열기 닫기
  const closeModal = () => {
    setIsModalOpen(false)
    setSelectedImage(null)
  }

  return (
    <>
      <MasonryInfiniteGrid
        className="container"
        gap={5}
        align={"stretch"}
        maxStretchColumnSize={360}
        useFirstRender={true}
        onRequestAppend={(e) => {
          fetchMyImages()
        }}
        onRenderComplete={(e) => {
          // console.log(e)
        }}
      >
        {items.map((item) => (
          <Item
            key={item.key}
            imageUrl={item.imageUrl}
            isSelected={item.key === selectedImageId}
            onClick={() => handleImageClick(item)}
            data-grid-groupkey={item.groupKey}
          />
        ))}
      </MasonryInfiniteGrid>

      {/* 이미지 상세 모달 */}
      {isModalOpen && (
        <ImgDetailModal image={selectedImage} onClose={closeModal} />
      )}
    </>
  )
}

export default ImgList
