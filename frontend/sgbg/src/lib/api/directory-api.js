import { authAxios, publicAxios } from "../../util/axios-setting"

const jwt = authAxios()

export const getDirectoryList = async (success, fail) => {
  const response = await jwt.get("/directories").then(success).catch(fail)

  return response
}
