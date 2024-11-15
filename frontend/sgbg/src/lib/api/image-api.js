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

// 이미지 휴지통 보내기 / 복원
export const patchImageToTrash = async (isTrash, data, success, fail) => {
  jwt
    .patch(`/directories/bin?toTrash=${isTrash}`, data)
    .then(success)
    .catch(fail)
}

// 앱 이미지 저장
export const postAppImage = async (data, success, fail) => {
  jwt.post(`/images/app`, data).then(success).catch(fail)
}

// 이미지 영구 삭제
export const deleteImages = async (data, success, fail) => {
  jwt
    .request({ method: "delete", url: `/images`, data: data })
    .then(success)
    .catch(fail)
}
