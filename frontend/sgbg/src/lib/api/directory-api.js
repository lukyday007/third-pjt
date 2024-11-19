import { authAxios, publicAxios } from "../../util/axios-setting"

const jwt = authAxios()

// 내 폴더 목록 조회
export const getDirectoryList = async (success, fail) => {
  const response = await jwt.get("/directories").then(success).catch(fail)
  return response
}

// 새 폴더 생성
export const postCreateDirectory = async (data, success, fail) => {
  const response = await jwt
    .post("/directories", data)
    .then(success)
    .catch(fail)

  return response
}

// 폴더 삭제
export const deleteDirectory = async (id, success, fail) => {
  await jwt.delete(`/directories/${id}`).then(success).catch(fail)
}

// 폴더 이름 변경
export const patchDirectoryName = async (data, success, fail) => {
  await jwt.patch(`/directories`, data).then(success).catch(fail)
}

// 폴더 순서 변경
export const patchDirectorySequence = async (data, success, fail) => {
  const response = await jwt
    .patch(`directories/sequence`, data)
    .then(success)
    .catch(fail)
  return response
}
