const { app, BrowserWindow, shell, ipcMain, dialog } = require("electron")
const updater = require("electron-updater")
const { autoUpdater } = updater
const ProgressBar = require("electron-progressbar")
const path = require("path")
const os = require("os")

const __dirname = path.resolve()

// The built directory structure
//
// ├─┬ dist-electron
// │ ├─┬ main
// │ │ └── index.js    > Electron-Main
// │ └─┬ preload
// │   └── index.mjs   > Preload-Scripts
// ├─┬ dist
// │ └── index.html    > Electron-Renderer
//
// The built directory structure 설정
process.env.APP_ROOT = path.join(__dirname, "../..")
const MAIN_DIST = path.join(process.env.APP_ROOT, "dist-electron")
const RENDERER_DIST = path.join(process.env.APP_ROOT, "dist")
const VITE_DEV_SERVER_URL = process.env.VITE_DEV_SERVER_URL

let win = BrowserWindow
let progressBar = null
const preload = path.join(__dirname, "../preload/index.mjs")
const indexHtml = path.join(RENDERER_DIST, "index.html")

async function createWindow() {
  win = new BrowserWindow({
    title: "Main window",
    icon: path.join(RENDERER_DIST, "singlebungle128.svg"),
    webPreferences: {
      preload,
    },
  })

  win.loadFile(indexHtml)
}

// autoUpdater 및 error event 설정
autoUpdater.autoDownload = false
autoUpdater.autoInstallOnAppQuit = false

app.whenReady().then(() => {
  createWindow()
  autoUpdater.checkForUpdates()
})

autoUpdater.on("update-available", () => {
  dialog
    .showMessageBox({
      type: "info",
      title: "Update",
      message: "새로운 버전이 있습니다. 다운로드하시겠습니까?",
      buttons: ["예", "아니오"],
    })
    .then((result) => {
      if (result.response === 0) autoUpdater.downloadUpdate()
    })
})

autoUpdater.on("download-progress", (progressObj) => {
  if (!progressBar) {
    progressBar = new ProgressBar({
      text: "다운로드 중...",
      detail: "파일을 다운로드 중입니다.",
    })
  }
  progressBar.value = progressObj.percent
})

autoUpdater.on("update-downloaded", () => {
  if (progressBar) {
    progressBar.setCompleted()
    progressBar = null
  }
  dialog
    .showMessageBox({
      type: "info",
      title: "업데이트 완료",
      message: "새 버전이 다운로드되었습니다. 지금 설치하시겠습니까?",
      buttons: ["예", "아니오"],
    })
    .then((result) => {
      if (result.response === 0) autoUpdater.quitAndInstall()
    })
})

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit()
})
