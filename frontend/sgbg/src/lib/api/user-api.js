import { authAxios, publicAxios } from "../../util/axios-setting"

const local = publicAxios()
const jwt = authAxios()

// 로그인 URL 받기
export const getLoginUrl = async (success, fail) => {
  await local.get("/oauth2/google/authorize").then(success).catch(fail)
}

// 구글 로그인 요청
export const googleSignIn = async (code, success, fail) => {
  await local.get(`/oauth2/code/google?code=${code}`).then(success).catch(fail)
}

// 유저정보 조회
export const getUserInfo = async (success, fail) => {
  await jwt.get("/users").then(success).catch(fail)
}
