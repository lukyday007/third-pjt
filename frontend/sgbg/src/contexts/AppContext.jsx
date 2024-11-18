// src/contexts/AppContext.js

import React, { createContext, useContext, useEffect, useState } from "react"

// Context 생성
export const AppContext = createContext()

export const AppProvider = ({ children }) => {
  const [folderName, setFolderName] = useState("")
  const [searchKeywords, setSearchKeywords] = useState([])
  const [isLatest, setIsLatest] = useState(0) // 최신:0 랜덤:2
  const [isSideBarOpen, setIsSideBarOpen] = useState(true) // 사이드바 열림 여부
  const [directoryInfos, setDirectoryInfos] = useState([]) // 디렉토리 정보
  const [directoryId, setDirectoryId] = useState()
  const [isBin, setIsBin] = useState(false)
  const [keywords, setKeywords] = useState("")

  // 로그인 상태
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  const tokentest = localStorage.getItem("accessToken")
  useEffect(() => {
    console.log("testtoken", tokentest)
  }, [tokentest])
  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (token) {
      setIsAuthenticated(true)
    }
    console.log("context useEffect", token)
  }, [])

  useEffect(() => {
    console.log("auth", isAuthenticated)
  }, [isAuthenticated])

  // storage 이벤트로 accessToken 값 변경을 감지
  useEffect(() => {
    const handleStorageChange = async (event) => {
      const token = localStorage.getItem("accessToken")
      if (token) {
        setIsAuthenticated(true)
      }
    }

    window.addEventListener("localStorageUpdate", handleStorageChange)

    return () => {
      window.removeEventListener("localStorageUpdate", handleStorageChange)
    }
  }, [])

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
        keywords,
        setKeywords,
        isAuthenticated,
        setIsAuthenticated,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}
