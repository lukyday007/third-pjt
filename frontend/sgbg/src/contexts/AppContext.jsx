// src/contexts/AppContext.js

import React, { createContext, useState } from "react"

// Context 생성
export const AppContext = createContext()

export const AppProvider = ({ children }) => {
  const [folderName, setFolderName] = useState("d")
  const [searchKeywords, setSearchKeywords] = useState([])
  const [isLatest, setIsLatest] = useState(0) // 최신:0 랜덤:2
  const [isSideBarOpen, setIsSideBarOpen] = useState(true) // 사이드바 열림 여부
  const [directoryInfos, setDirectoryInfos] = useState([]) // 디렉토리 정보
  const [directoryId, setDirectoryId] = useState()
  const [isBin, setIsBin] = useState(false)
  // console.log(folderName, searchKeywords, isLatest, "잘 되고 잇니")
  const toggleLatest = () => setIsLatest((prev) => (prev === 0 ? 2 : 0))

  return (
    <AppContext.Provider
      value={{
        folderName,
        setFolderName,
        searchKeywords,
        setSearchKeywords,
        isLatest,
        toggleLatest,
        isSideBarOpen,
        setIsSideBarOpen,
        setIsLatest,
        directoryInfos,
        setDirectoryInfos,
        directoryId,
        setDirectoryId,
        isBin,
        setIsBin,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}
