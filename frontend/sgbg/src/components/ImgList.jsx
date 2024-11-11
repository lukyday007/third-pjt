import styled from "styled-components"
import * as React from "react"
import { MasonryInfiniteGrid } from "@egjs/react-infinitegrid"
import "./styles.css"

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
  const [items, setItems] = React.useState(() => getItems(0, 10))
  return (
    <>
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
    </>
  )
}

export default ImgList
