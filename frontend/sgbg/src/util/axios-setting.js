import axios, { Axios } from "axios"

export const authAxios = () => {
  const instance = axios.create({
    baseURL: "https://k11b205.p.ssafy.io/api",
    withCredentials: true,
  })

  instance.interceptors.request.use(
    (config) => {
      const accessToken = localStorage.getItem("accessToken")

      if (accessToken) {
        // Access 토큰 앞에 `Bearer ` 설정
        config.headers["Authorization"] = `Bearer ${accessToken}`
      }

      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  instance.interceptors.response.use(
    (response) => {
      return response
    },

    async (error) => {
      const originalRequest = error.config

      if (error.response && error.response.status === 401) {
        try {
          const tokenRefreshResult = await instance.post(`/users/refresh-token`)
          const newAccessToken = tokenRefreshResult.data

          localStorage.setItem("accessToken", newAccessToken)

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return axios(originalRequest)
        } catch (refreshError) {
          // 토큰 없애야될거같은자리
          window.location.href = "http://localhost:5173/#/login"
          return Promise.reject(error)
        }
      }
      return Promise.reject(error)
    }
  )

  instance.defaults.headers.common["Authorization"] = ""
  instance.defaults.headers.post["Content-Type"] = "application/json"
  instance.defaults.headers.patch["Content-Type"] = "application/json"
  instance.defaults.headers.put["Content-Type"] = "application/json"

  return instance
}

export const publicAxios = () => {
  const publicAxios = axios.create({
    baseURL: "https://k11b205.p.ssafy.io/api",
    withCredentials: true,
  })

  publicAxios.defaults.headers.common["Authorization"] = ""
  publicAxios.defaults.headers.post["Content-Type"] = "application/json"
  publicAxios.defaults.headers.patch["Content-Type"] = "application/json"

  return publicAxios
}
