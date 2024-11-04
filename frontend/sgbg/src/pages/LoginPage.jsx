import React, { useState } from "react"
import styled, { keyframes } from "styled-components"
import LoginButton from "../asset/images/LoginPage/LoginButton.svg?react"
import LoginButtonSmall from "../asset/images/LoginPage/LoginButtonSmall.svg?react"
import SingBung from "../asset/images/MainPage/SingBung.svg?react"
import BigSingBung from "../asset/images/LoginPage/BigSingBung.svg?react"

const s = {
  Container: styled.div`
    display: flex;
    width: 100vw;
  `,
  Title: styled.div`
    font-size: 44px;
    font-weight: 700;
  `,
  Caption: styled.div`
    font-size: 20px;
    font-weight: 500;
    color: #666666;
  `,
  LoginArea: styled.div`
    display: flex;
    flex-direction: column;
    width: 50vw;
    height: 100vh;
    justify-content: center;
    padding-left: 100px;
  `,
  ImageArea: styled.div`
    width: 50vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-right: 100px;
  `,
  SingBungImage: styled.div``,
}

const LoginPage = () => {
  const [size, setSize] = useState(400)
  const handleClickSingBung = () => {
    setSize((prev) => prev + 100)
  }
  return (
    <s.Container>
      <s.LoginArea>
        <s.Title>싱글벙글 신나는 이미지 저장을 위해</s.Title>
        <s.Title style={{ marginBottom: "12px" }}>로그인하세요.</s.Title>
        <s.Caption>
          싱글벙글은 SSAFY B205 팀이 개발한 이미지 관리 플랫폼입니다.
        </s.Caption>
        <s.Caption>
          손쉽게 웹상의 재밌는 이미지, GIF, 밈 등을 다운받아 저장하고, 나만의
          컬렉션을 만들어 보세요.
        </s.Caption>
        <s.Caption style={{ marginBottom: "36px" }}>
          지금 바로 싱글벙글과 함께 웹서핑의 재미를 두 배로 늘려보세요!
        </s.Caption>
        <LoginButton style={{ cursor: "pointer" }} />
      </s.LoginArea>
      <s.ImageArea>
        <s.SingBungImage>
          <BigSingBung
            onClick={handleClickSingBung}
            style={{
              width: `${size}px`,
              height: `${size}px`,
              cursor: "pointer",
            }}
          />
        </s.SingBungImage>
      </s.ImageArea>
    </s.Container>
  )
}
export default LoginPage
