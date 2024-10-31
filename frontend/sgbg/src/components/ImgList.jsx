import { useState } from "react"
import styled from "styled-components"
import Masonry, { ResponsiveMasonry } from "react-responsive-masonry"
import * as React from "react"
import { MasonryInfiniteGrid } from "@egjs/react-infinitegrid"
import "./styles.css"

import test from "../asset/images/ImgList/1.jpg"

const Dummydata = {
  images: [
    { imageId: "1", imageUrl: "src/asset/images/ImgList/1.jpg" },
    { imageId: "2", imageUrl: "src/asset/images/ImgList/2.jpg" },
    { imageId: "3", imageUrl: "src/asset/images/ImgList/3.jpg" },
    { imageId: "4", imageUrl: "src/asset/images/ImgList/4.jpg" },
    { imageId: "5", imageUrl: "src/asset/images/ImgList/5.png" },
    { imageId: "6", imageUrl: "src/asset/images/ImgList/6.png" },
    { imageId: "7", imageUrl: "src/asset/images/ImgList/7.jpg" },
    { imageId: "8", imageUrl: "src/asset/images/ImgList/8.jpg" },
    { imageId: "9", imageUrl: "src/asset/images/ImgList/9.jpg" },
    { imageId: "10", imageUrl: "src/asset/images/ImgList/10.jpg" },
    { imageId: "11", imageUrl: "src/asset/images/ImgList/11.jpg" },
    { imageId: "12", imageUrl: "src/asset/images/ImgList/12.jpg" },
  ],
}

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

function getItems(nextGroupKey, count) {
  const nextItems = []
  const nextKey = nextGroupKey * count

  for (let i = 0; i < count; ++i) {
    nextItems.push({ groupKey: nextGroupKey, key: nextKey + i })
  }
  return nextItems
}

const Item = ({ num }) => (
  <div className="item">
    <div className="thumbnail">
      <img
        src={`https://naver.github.io/egjs-infinitegrid/assets/image/${
          (num % 33) + 1
        }.jpg`}
        alt="egjs"
      />
    </div>
  </div>
)

const ImgList = () => {
  const [selectedImageId, setSelectedImageId] = useState(null)
  const [items, setItems] = React.useState(() => getItems(0, 10))
  return (
    <MasonryInfiniteGrid
      className="container"
      gap={5}
      align={"stretch"}
      maxStretchColumnSize={360}
      useFirstRender={true}
      onRequestAppend={(e) => {
        const nextGroupKey = (+e.groupKey || 0) + 1

        setItems([...items, ...getItems(nextGroupKey, 10)])
      }}
      onRenderComplete={(e) => {
        console.log(e)
      }}
    >
      {items.map((item) => (
        <Item
          data-grid-groupkey={item.groupKey}
          key={item.key}
          num={item.key}
        />
      ))}
    </MasonryInfiniteGrid>
  )
}

export default ImgList
