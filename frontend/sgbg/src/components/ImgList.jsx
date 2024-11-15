import { useState, useEffect, useRef, useContext } from "react"
import styled from "styled-components"
import { MasonryInfiniteGrid } from "@egjs/react-infinitegrid"
import "./styles.css"
import {
  deleteImages,
  getFeedImages,
  getMyImages,
  patchImageToTrash,
  postAppImage,
} from "../lib/api/image-api"
import { useParams } from "react-router-dom"
import ImgDetailModal from "./ImgDetailModal"
import { AppContext } from "../contexts/AppContext"
import ImgListRightClickModal from "./ImgListRightClickModal"

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
    imageManagementId: image?.imageManagementId,
  }))
}

const Item = ({ imageUrl, isSelected, onClick, onContextMenu }) => {
  const selectedRef = useRef(null)

  // 선택 이미지가 변경되면 해당 위치로 화면 스크롤 이동
  useEffect(() => {
    if (isSelected && selectedRef.current) {
      selectedRef.current.scrollIntoView({
        behavior: "smooth",
      })
    }
  }, [isSelected])

  return (
    <div className="item" onClick={onClick} onContextMenu={onContextMenu}>
      <div className="thumbnail">
        <s.Image
          src={`https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/${imageUrl}`}
          alt="User image"
          isSelected={isSelected}
          ref={isSelected ? selectedRef : null}
        />
      </div>
    </div>
  )
}

const ImgList = () => {
  const [selectedImageKey, setSelectedImageKey] = useState(null)
  const [selectedImageId, setSelectedImageId] = useState(null)
  const [selectedImageManagementId, setSelectedImageManagementId] =
    useState(null)
  const [items, setItems] = useState([])
  const [currentPage, setCurrentPage] = useState(1)
  const [prevPage, setPrevPage] = useState(0)
  const [totalPage, setTotalPage] = useState(null)
  const [isFetching, setIsFetching] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [keywords, setKeywords] = useState("")
  const selectedImageKeyRef = useRef(selectedImageKey)
  const itemsRef = useRef(items)

  // 이미지 우클릭 관리
  const [isRightClickModalOpen, setIsRightClickModalOpen] = useState(false)
  const [rightClickModalPosition, setRightClickModalPosition] = useState({
    X: 0,
    Y: 0,
  })

  const params = useParams()
  useEffect(() => {
    console.log(params, "params")
  }, [params])

  const { searchKeywords, isLatest } = useContext(AppContext)

  useEffect(() => {
    const result = searchKeywords.map((item) => item.keyword).join(",")
    setKeywords(result)
    console.log(result)
  }, [searchKeywords])

  const fetchMyImages = async () => {
    if (isFetching || (totalPage !== null && currentPage > totalPage)) return
    setIsFetching(true)

    getMyImages(
      params.id === "all" ? -1 : params.id === "bin" ? 0 : params.id,
      currentPage,
      10,
      keywords,
      isLatest,
      params.id === "bin" ? true : false,
      // true,
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

  const fetchFeedImages = async () => {
    if (isFetching || (totalPage !== null && currentPage > totalPage)) return
    setIsFetching(true)

    getFeedImages(
      currentPage,
      10,
      keywords,
      isLatest,
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
  }, [params.id, keywords, isLatest])

  // 선택 이미지 변경시 Ref 참조 변경
  useEffect(() => {
    selectedImageKeyRef.current = selectedImageKey
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
  }

  // 이미지 우클릭 모달 관리
  const toggleRightClickModal = () => {
    setIsRightClickModalOpen((prev) => !prev)
  }

  // 우클릭 관리
  const handleRightClick = (event, itemInfo) => {
    // 우클릭 이벤트 제한
    event.preventDefault()

    const currentX = event.clientX
    const currentY = event.clientY

    if (currentX && currentY) {
      setRightClickModalPosition({ X: currentX, Y: currentY })
    }

    if (itemInfo) {
      console.log(itemInfo)
      setSelectedImageKey(itemInfo.key)
      setSelectedImageId(itemInfo.imageId)

      if (itemInfo?.imageManagementId) {
        setSelectedImageManagementId(itemInfo.imageManagementId)
      }
      toggleRightClickModal()
    }
  }

  // 이미지 삭제
  const deleteImage = async () => {
    const targetManagementId = selectedImageManagementId
    const targetImages = [selectedImageManagementId]
    const data = { imageManagementIds: targetImages }
    console.log(data)
    try {
      if (params.id === "bin") {
        await deleteImages(
          data,
          (resp) => {
            console.log(resp)
          },
          (error) => {
            throw new Error(error)
          }
        )
      } else {
        await patchImageToTrash(
          true,
          data,
          (resp) => {},
          (error) => {
            throw new Error(error)
          }
        )
      }

      const targetIndex = items.findIndex((item) => {
        return item.imageManagementId === targetManagementId
      })
      const newItems = [...items]

      newItems.splice(targetIndex, 1)
      setItems(newItems)
    } catch (e) {
      alert("이미지 삭제 실패")
    }
  }

  // 웹 이미지 저장
  const handleImageSaveClick = async () => {
    const targetImageId = selectedImageId
    const data = { imageId: targetImageId, directoryId: 0 }
    try {
      postAppImage(data, (resp) => {
        console.log(resp),
          (error) => {
            console.log(error)
          }
      })
    } catch (e) {
      console.log(e)
    }
  }

  return (
    <>
      {params.id ? (
        <MasonryInfiniteGrid
          className="container"
          gap={5}
          align={"stretch"}
          maxStretchColumnSize={360}
          useFirstRender={true}
          onRequestAppend={(e) => {
            fetchMyImages()
          }}
          onRenderComplete={(e) => {}}
        >
          {items.map((item) => (
            <Item
              key={item.key}
              imageUrl={item.imageUrl}
              isSelected={item.key === selectedImageKey}
              onClick={() => handleImageClick(item)}
              onContextMenu={(event) => handleRightClick(event, item)}
              data-grid-groupkey={item.groupKey}
            />
          ))}
        </MasonryInfiniteGrid>
      ) : (
        <MasonryInfiniteGrid
          className="container"
          gap={5}
          align={"stretch"}
          maxStretchColumnSize={360}
          useFirstRender={true}
          onRequestAppend={(e) => {
            fetchFeedImages()
          }}
          onRenderComplete={(e) => {}}
        >
          {items.map((item) => (
            <Item
              key={item.key}
              imageUrl={item.imageUrl}
              isSelected={item.key === selectedImageKey}
              onClick={() => handleImageClick(item)}
              onContextMenu={(event) => handleRightClick(event, item)}
              data-grid-groupkey={item.groupKey}
            />
          ))}
        </MasonryInfiniteGrid>
      )}
      {isModalOpen && (
        <ImgDetailModal
          imageId={selectedImageId}
          saveFunction={handleImageSaveClick}
          onClose={closeModal}
        />
      )}
      {isRightClickModalOpen && (
        <ImgListRightClickModal
          toggleFunction={toggleRightClickModal}
          position={rightClickModalPosition}
          deleteFunction={deleteImage}
          saveFunction={handleImageSaveClick}
        />
      )}
    </>
  )
}

export default ImgList
