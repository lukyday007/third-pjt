import { useState } from "react"
import styled from "styled-components"
import Masonry, { ResponsiveMasonry } from "react-responsive-masonry"

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

const ImgList = () => {
  const [selectedImageId, setSelectedImageId] = useState(null)
  return (
    <ResponsiveMasonry
      columnsCountBreakPoints={{ 350: 1, 750: 2, 900: 3, 1200: 4 }}
    >
      <Masonry gutter="20px">
        {Dummydata.images.map((image) => (
          <s.Image
            key={image.imageId}
            src={image.imageUrl}
            alt={`이미지 ${image.imageId}`}
            isSelected={selectedImageId === image.imageId}
            onClick={() => setSelectedImageId(image.imageId)}
          />
        ))}
      </Masonry>
    </ResponsiveMasonry>
  )
}

export default ImgList
