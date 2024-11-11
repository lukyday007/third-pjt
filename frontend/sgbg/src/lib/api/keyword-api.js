import { authAxios } from "../../util/axios-setting"

const jwt = authAxios()

// 키워드 검색
export const getKeywordList = async (keyword, success, fail) => {
  const response = await jwt
    .get(`/keywords?keyword=${keyword}`)
    .then(success)
    .catch(fail)
  return response
}

// 인기 키워드
