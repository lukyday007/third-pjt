import { authAxios, publicAxios } from "../../util/axios-setting"

const local = publicAxios()
const jwt = authAxios()

export const getLoginUrl = async (success, fail) => {
  await local.get("/oauth2/google/authorize").then(success).catch(fail)
}

export const googleSignIn = async (code, success, fail) => {
  await local.get(`/oauth2/code/google?code=${code}`).then(success).catch(fail)
}
