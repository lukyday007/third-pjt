import { authAxios, publicAxios } from "../../util/axios-setting"

const local = publicAxios()
const jwt = authAxios()

// 내 폴더 이미지 조회
export const getMyImages = async (
  directoryId,
  page,
  size,
  keyword,
  sort,
  isBin,
  success,
  fail
) => {
  await jwt
    .get(
      `/images/my?directoryId=${directoryId}&page=${page}&size=${size}&keyword=${keyword}&sort=${sort}&bin=${isBin}`
    )
    .then(success)
    .catch(fail)
}

// 피드 이미지 조회
export const getFeedImages = async (
  page,
  size,
  keyword,
  sort,
  success,
  fail
) => {
  await jwt
    .get(
      `/images/feed?page=${page}&size=${size}&keyword=${keyword}&sort=${sort}`
    )
    .then(success)
    .catch(fail)
}

// 이미지 상세 조회
export const getImageDetail = async (imageId, success, fail) => {
  const response = await local
    .get(`/images/${imageId}`)
    .then(success)
    .catch(fail)
  return response
}
