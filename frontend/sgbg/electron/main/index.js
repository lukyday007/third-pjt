import { app, BrowserWindow, shell, ipcMain, dialog } from "electron"
import ProgressBar from "electron-progressbar"
import { createRequire } from "node:module"
import { fileURLToPath } from "node:url"
import path from "node:path"
import os from "node:os"

const require = createRequire(import.meta.url)
const __dirname = path.dirname(fileURLToPath(import.meta.url))
const { autoUpdater } = require("electron-updater")

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
process.env.APP_ROOT = path.join(__dirname, "../..")

export const MAIN_DIST = path.join(process.env.APP_ROOT, "dist-electron")
export const RENDERER_DIST = path.join(process.env.APP_ROOT, "dist")
export const VITE_DEV_SERVER_URL = process.env.VITE_DEV_SERVER_URL

process.env.VITE_PUBLIC = VITE_DEV_SERVER_URL
  ? path.join(process.env.APP_ROOT, "public")
  : RENDERER_DIST

// Disable GPU Acceleration for Windows 7
if (os.release().startsWith("6.1")) app.disableHardwareAcceleration()

// Set application name for Windows 10+ notifications
if (process.platform === "win32") app.setAppUserModelId(app.getName())

if (!app.requestSingleInstanceLock()) {
  app.quit()
  process.exit(0)
}

let win = BrowserWindow
let progressBar = null
const preload = path.join(__dirname, "../preload/index.mjs")
const indexHtml = path.join(RENDERER_DIST, "index.html")

// 자동 업데이트가 자동 설치되지 않도록 설정
autoUpdater.autoInstallOnAppQuit = false
autoUpdater.autoDownload = false

async function createWindow() {
  win = new BrowserWindow({
    title: "Main window",
    icon: path.join(process.env.VITE_PUBLIC, "singlebungle128.svg"),
    webPreferences: {
      preload,
      // Warning: Enable nodeIntegration and disable contextIsolation is not secure in production
      // nodeIntegration: true,

      // Consider using contextBridge.exposeInMainWorld
      // Read more on https://www.electronjs.org/docs/latest/tutorial/context-isolation
      // contextIsolation: false,
    },
  })

  if (VITE_DEV_SERVER_URL) {
    win.loadURL(VITE_DEV_SERVER_URL)
  } else {
    win.loadFile(indexHtml)
  }
}

autoUpdater.on("checking-for-update", () => {
  console.log("업데이트 확인 중")
})

autoUpdater.on("update-available", () => {
  console.log("업데이트 버전 확인")

  dialog
    .showMessageBox({
      type: "info",
      title: "Update",
      message:
        "새로운 버전이 확인되었습니다. 설치 파일을 다운로드 하시겠습니까?",
      buttons: ["지금 설치", "나중에 설치"],
    })
    .then((result) => {
      if (result.response === 0) autoUpdater.downloadUpdate()
    })
})

autoUpdater.on("update-not-available", () => {
  console.log("업데이트 불가")
  if (progressBar) {
    progressBar.setCompleted()
    progressBar = null
  }
})

autoUpdater.on("download-progress", (progressObj) => {
  if (!progressBar) {
    progressBar = new ProgressBar({
      text: "Download 중...",
      detail: "다운로드 중입니다.",
    })
  }
  progressBar.value = progressObj.percent
})

autoUpdater.on("update-downloaded", () => {
  console.log("업데이트 완료")

  if (progressBar) {
    progressBar.setCompleted()
    progressBar = null
  }

  dialog
    .showMessageBox({
      type: "info",
      title: "Update",
      message: "새로운 버전이 다운로드 되었습니다. 다시 시작하시겠습니까?",
      buttons: ["예", "아니오"],
    })
    .then((result) => {
      if (result.response === 0) autoUpdater.quitAndInstall(false, true)
    })
})

autoUpdater.on("error", (err) => {
  console.log("업데이트 중 에러 발생: ", err)
  if (progressBar) {
    progressBar.close()
    progressBar = null
  }
})

app.whenReady().then(() => {
  createWindow()
  autoUpdater.checkForUpdates()
})

app.on("window-all-closed", () => {
  win = null
  if (process.platform !== "darwin") app.quit()
})

app.on("activate", () => {
  const allWindows = BrowserWindow.getAllWindows()
  if (allWindows.length) {
    allWindows[0].focus()
  } else {
    createWindow()
  }
})

// New window example arg: new windows url
ipcMain.handle("open-win", (_, arg) => {
  const childWindow = new BrowserWindow({
    webPreferences: {
      preload,
      nodeIntegration: true,
      contextIsolation: false,
    },
  })

  if (VITE_DEV_SERVER_URL) {
    childWindow.loadURL(`${VITE_DEV_SERVER_URL}#${arg}`)
  } else {
    childWindow.loadFile(indexHtml, { hash: arg })
  }
})
