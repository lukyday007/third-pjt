// src/contexts/AppContext.js

import React, { createContext, useState } from "react"

// Context 생성
export const AppContext = createContext()

export const AppProvider = ({ children }) => {
  const [folderName, setFolderName] = useState("")
  const [searchKeywords, setSearchKeywords] = useState([])
  const [isLatest, setIsLatest] = useState(0) // 최신:0 랜덤:2

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
      }}
    >
      {children}
    </AppContext.Provider>
  )
}
