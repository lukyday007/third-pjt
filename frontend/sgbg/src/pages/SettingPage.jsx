import styled from "styled-components"

const s = {
  Container: styled.div`
    display: flex;
    flex-direction: column;
    padding: 20px;
    border-radius: 8px;
  `,
  Section: styled.div`
    padding-top: 20px;
    border-top: 1px solid #ccc; /* 상단 회색 보더 */
  `,
  Title: styled.h1`
    font-size: 24px;
    font-weight: bold;
    /* color: #555; */
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
    /* color: #444; */
    margin-bottom: 8px;
  `,
  Description: styled.p`
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 20px;
  `,
}

const SettingPage = () => {
  return (
    <s.Container>
      <s.Title>Windows 설정</s.Title>

      <s.Section>
        <s.Subtitle>시작 앱 설정</s.Subtitle>
        <s.FeatureName>싱글벙글 열기</s.FeatureName>
        <s.Description>
          클릭하지 않아도 싱글벙글이 시작 앱이 되어 여러분을 반갑게 맞이할 수
          있도록 해보세요.
        </s.Description>
      </s.Section>

      <s.Section>
        <s.Subtitle>닫기 버튼</s.Subtitle>
        <s.FeatureName>트레이로 최소화하기</s.FeatureName>
        <s.Description>
          X버튼을 눌러 앱을 닫아도 싱글벙글이 시스템 트레이에 남아있어요.
        </s.Description>
      </s.Section>
    </s.Container>
  )
}

export default SettingPage
