import { app, BrowserWindow, shell, ipcMain, dialog } from "electron"
import { autoUpdater } from "electron-updater"
import ProgressBar from "electron-progressbar"
import path from "path"
import os from "os"

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
let autoUpdater = null
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

// `import()`로 electron-updater 동적 로딩
async function initAutoUpdater() {
  const updaterModule = await import("electron-updater")
  autoUpdater = updaterModule.autoUpdater

  // autoUpdater 설정
  autoUpdater.autoDownload = false
  autoUpdater.autoInstallOnAppQuit = false

  autoUpdater.checkForUpdates()

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
        if (result.response === 0) {
          setTimeout(() => {
            autoUpdater.quitAndInstall()
          }, 1000) // 1초 대기 후 quitAndInstall 호출
        }
      })
  })
}

app.whenReady().then(async () => {
  createWindow()
  await initAutoUpdater() // autoUpdater 초기화 함수 호출
})

app.on("before-quit", () => {
  // 모든 창을 닫아 프로세스를 종료
  BrowserWindow.getAllWindows().forEach((win) => win.close())
})

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit()
})
