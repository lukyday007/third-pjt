import { useState, useEffect, useRef } from "react"
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
  const [selectedImageKey, setSelectedImageKey] = useState(null)
  const [selectedImageId, setSelectedImageId] = useState(null)
  const [items, setItems] = useState([])
  const [currentPage, setCurrentPage] = useState(1)
  const [prevPage, setPrevPage] = useState(0)
  const [totalPage, setTotalPage] = useState(null)
  const [isFetching, setIsFetching] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const selectedImageRef = useRef(null)
  const selectedImageKeyRef = useRef(selectedImageKey)
  const itemsRef = useRef(items)

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
    selectedImageKeyRef.current = selectedImageKey
    console.log("selectedImageKey", selectedImageKey)
  }, [selectedImageKey])

  //
  useEffect(() => {
    itemsRef.current = items
  }, [items])

  //
  useEffect(() => {
    // 컴포넌트 로드시 keydown 이벤트 리스너 추가
    window.addEventListener("keydown", handleKeyDown)

    return () => {
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [])

  // 이미지 클릭
  const handleImageClick = (image) => {
    // 동일한 이미지 클릭시 이미지 선택 해제
    if (!selectedImageKey === image.key) {
      setSelectedImageKey(null)
      return
    }

    setSelectedImageId(image.imageId)
    setIsModalOpen(true)
    setSelectedImageKey(image.key) // 원래 이미지 클릭에 있던 코드
  }

  // 모달 열기 닫기
  const closeModal = () => {
    setIsModalOpen(false)
  }

  // 방향키 로직 추가
  const handleKeyDown = (event) => {
    const currentKey = selectedImageKeyRef.current

    if (!currentKey) {
      console.log("!selectedImageKey", currentKey)
      return
    }

    if (event.key === "ArrowRight") {
      // 오른쪽 화살표
      selectNextImage("right")
      return
    }

    if (event.key === "ArrowLeft") {
      selectNextImage("left")
      return
    }
  }

  // 다음 요소를 선택하는 함수
  const selectNextImage = (direction) => {
    // Ref를 통해 함수 호출 시점의 Key값과 items 배열 선택
    const currentImageKey = selectedImageKeyRef.current
    const currentItems = itemsRef.current

    // items 배열에서의 현재 인덱스 배열 선택
    const imageIndex = currentItems.findIndex(
      (item) => item.key === currentImageKey
    )

    const listSize = currentItems.length

    if (direction === "right") {
      const nextIndex = imageIndex + 1

      if (nextIndex > listSize - 1) return

      setSelectedImageKey(currentItems[nextIndex].key)
      setSelectedImageId(currentItems[nextIndex].imageId)
    } else if (direction === "left") {
      const nextIndex = imageIndex - 1

      if (nextIndex < 0) return

      setSelectedImageKey(currentItems[nextIndex].key)
      setSelectedImageId(currentItems[nextIndex].imageId)
    }

    console.log(imageIndex)
    console.log(currentItems[imageIndex + 1])
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
            isSelected={item.key === selectedImageKey}
            onClick={() => handleImageClick(item)}
            data-grid-groupkey={item.groupKey}
          />
        ))}
      </MasonryInfiniteGrid>

      {/* 이미지 상세 모달 */}
      {isModalOpen && (
        <ImgDetailModal imageId={selectedImageId} onClose={closeModal} />
      )}
    </>
  )
}

export default ImgList
