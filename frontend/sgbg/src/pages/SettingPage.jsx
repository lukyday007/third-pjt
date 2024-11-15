import styled from "styled-components"
import { useState, useEffect } from "react"

const ipcRenderer = window.electron?.ipcRenderer

const s = {
  Container: styled.div`
    display: flex;
    flex-direction: column;
    padding: 30px 50px;
    border-radius: 8px;
  `,
  Section: styled.div`
    padding-top: 20px;
    border-top: 1px solid #ccc; /* 상단 회색 보더 */
  `,
  Title: styled.h1`
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 16px;
  `,
  Subtitle: styled.h2`
    font-size: 14px;
    font-weight: bold;
    color: #505050;
    margin-bottom: 12px;
  `,
  FeatureName: styled.p`
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 8px;
    display: flex;
    justify-content: space-between; /* Toggle을 오른쪽에 배치 */
    align-items: center;
  `,
  Description: styled.p`
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 20px;
  `,
  ToggleContainer: styled.label`
    position: relative;
    display: inline-block;
    width: 40px;
    height: 20px;
  `,
  ToggleInput: styled.input`
    opacity: 0;
    width: 0;
    height: 0;
  `,
  Slider: styled.span`
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: ${(props) => (props.isChecked ? "#4caf50" : "#ccc")};
    border-radius: 20px;
    transition: 0.4s;

    &:before {
      position: absolute;
      content: "";
      height: 14px;
      width: 14px;
      left: ${(props) => (props.isChecked ? "23px" : "3px")};
      bottom: 3px;
      background-color: white;
      border-radius: 50%;
      transition: 0.4s;
    }
  `,
}

const SettingPage = () => {
  const [isAutoStartEnabled, setIsAutoStartEnabled] = useState(false)
  const [isTrayMinimizeEnabled, setIsTrayMinimizeEnabled] = useState(false)

  // 초기값 로드
  useEffect(() => {
    if (!ipcRenderer) {
      console.error("ipcRenderer가 정의되지 않았습니다. Electron 환경에서 실행되지 않았을 수 있습니다.")
      return
    }

    ipcRenderer
      .invoke("get-settings")
      .then((settings) => {
        console.log("현재 electron-store 데이터:", settings)
        setIsAutoStartEnabled(settings.isAutoStartEnabled)
        setIsTrayMinimizeEnabled(settings.isTrayMinimizeEnabled)
      })
      .catch((error) => {
        console.error("설정 데이터를 가져오는 중 오류 발생:", error)
      })
  }, [])

  // 시작 앱 설정 업데이트
  useEffect(() => {
    if (ipcRenderer) {
      ipcRenderer.invoke("set-auto-start", isAutoStartEnabled)
    }
  }, [isAutoStartEnabled])

  // 트레이 최소화 설정 상태 업데이트
  useEffect(() => {
    if (ipcRenderer) {
      ipcRenderer.invoke("set-tray-minimize", isTrayMinimizeEnabled)
    }
  }, [isTrayMinimizeEnabled])

  return (
    <s.Container>
      <s.Title>Windows 설정</s.Title>

      <s.Section>
        <s.Subtitle>시작 앱 설정</s.Subtitle>
        <s.FeatureName>
          싱글벙글 열기
          <s.ToggleContainer>
            <s.ToggleInput
              type="checkbox"
              checked={isAutoStartEnabled}
              onChange={() => setIsAutoStartEnabled(!isAutoStartEnabled)}
            />
            <s.Slider isChecked={isAutoStartEnabled} />
          </s.ToggleContainer>
        </s.FeatureName>
        <s.Description>
          클릭하지 않아도 싱글벙글이 시작 앱이 되어 여러분을 반갑게 맞이할 수
          있도록 해보세요.
        </s.Description>
      </s.Section>

      <s.Section>
        <s.Subtitle>닫기 버튼</s.Subtitle>
        <s.FeatureName>
          트레이로 최소화하기
          <s.ToggleContainer>
            <s.ToggleInput
              type="checkbox"
              checked={isTrayMinimizeEnabled}
              onChange={() => setIsTrayMinimizeEnabled(!isTrayMinimizeEnabled)}
            />
            <s.Slider isChecked={isTrayMinimizeEnabled} />
          </s.ToggleContainer>
        </s.FeatureName>
        <s.Description>
          X버튼을 눌러 앱을 닫아도 싱글벙글이 시스템 트레이에 남아있어요.
        </s.Description>
      </s.Section>
    </s.Container>
  )
}

export default SettingPage
