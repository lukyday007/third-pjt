import { useState, useEffect } from "react"
import styled from "styled-components"
import { MasonryInfiniteGrid } from "@egjs/react-infinitegrid"
import "./styles.css"
import { getMyImages } from "../lib/api/image-api"
import { useParams } from "react-router-dom"

const s = {
  Image: styled.img`
    width: 100%;
    border-radius: 8px;
    border: solid 1px rgba(229, 229, 229, 1);
    outline: ${({ isSelected }) =>
      isSelected ? "3px solid rgba(255, 184, 0, 1)" : "none"};
    &:hover {
      outline: ${({ isSelected }) =>
        isSelected
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
  }))
}

const Item = ({ imageUrl, isSelected, onClick }) => (
  <div className="item" onClick={onClick}>
    <div className="thumbnail">
      <img
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

  // useEffect(() => {
  //   setItems([])
  //   setCurrentPage(1)
  //   setTotalPage(null)
  // }, [params.id])

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
          console.log(e)
        }}
      >
        {items.map((item) => (
          <Item
            key={item.key}
            imageUrl={item.imageUrl}
            isSelected={item.key === selectedImageId}
            onClick={() => setSelectedImageId(item.key)}
            data-grid-groupkey={item.groupKey}
          />
        ))}
      </MasonryInfiniteGrid>
    </>
  )
}

export default ImgList
