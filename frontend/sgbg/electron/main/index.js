import { app, BrowserWindow, shell, ipcMain, Tray, Menu } from "electron"
import { createRequire } from "node:module"
import { fileURLToPath } from "node:url"
import path from "node:path"
import os from "node:os"
import { update } from "./update"
import Store from "electron-store"

const require = createRequire(import.meta.url)
const __dirname = path.dirname(fileURLToPath(import.meta.url))

const store = new Store()
ipcMain.handle("get-settings", () => {
  return {
    isAutoStartEnabled: store.get("isAutoStartEnabled", false),
    isTrayMinimizeEnabled: store.get("isTrayMinimizeEnabled", false),
  }
})

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
let tray = null // 시스템 트레이 변수 추가
let isTrayMinimizeEnabled = false // 트레이 최소화 설정 상태

const preload = path.join(__dirname, "../preload/index.mjs")
const indexHtml = path.join(RENDERER_DIST, "index.html")

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
    // #298
    win.loadURL(VITE_DEV_SERVER_URL)
    // Open devTool if the app is not packaged
    // win.webContents.openDevTools()
  } else {
    win.loadFile(indexHtml)
    // win.webContents.openDevTools()
  }

  // Test actively push message to the Electron-Renderer
  win.webContents.on("did-finish-load", () => {
    win?.webContents.send("main-process-message", new Date().toLocaleString())
  })

  // Make all links open with the browser, not with the application
  win.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith("https:")) shell.openExternal(url)
    return { action: "deny" }
  })

  // Auto update
  update(win)

  // 시스템 트레이 생성
  tray = new Tray(path.join(process.env.VITE_PUBLIC, "singlebungle128.svg"))
  const trayMenu = Menu.buildFromTemplate([
    {
      label: "열기",
      click: () => {
        if (win.isMinimized()) win.restore()
        if (!win.isVisible()) win.show()
        win.focus()
      },
    },
    {
      label: "종료",
      click: () => {
        app.isQuiting = true
        app.quit()
      },
    },
  ])
  tray.setContextMenu(trayMenu)
  tray.setToolTip("싱글벙글 앱")
}

// 창을 닫을 때 트레이에 최소화 여부 확인
app.on("before-quit", () => (app.isQuiting = true))
app.on("window-all-closed", () => {
  win = null
  if (process.platform !== "darwin") app.quit()
})

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

// 창을 닫을 때 트레이에 남기기 (isTrayMinimizeEnabled에 따라)
app.whenReady().then(() => {
  createWindow()

  win.on("close", (event) => {
    if (!app.isQuiting && isTrayMinimizeEnabled) {
      event.preventDefault()
      win.hide()
    }
  })
})

// 트레이 최소화 설정 받기
ipcMain.handle("set-tray-minimize", (_, isEnabled) => {
  store.set("isTrayMinimizeEnabled", isEnabled)
})

app.on("window-all-closed", () => {
  win = null
  if (process.platform !== "darwin") app.quit()
})

app.on("second-instance", () => {
  if (win) {
    // Focus on the main window if the user tried to open another
    if (win.isMinimized()) win.restore()
    win.focus()
  }
})

app.on("activate", () => {
  const allWindows = BrowserWindow.getAllWindows()
  if (allWindows.length) {
    allWindows[0].focus()
  } else {
    createWindow()
  }
})

// ipcMain: 시작 앱 설정 처리
ipcMain.handle("set-auto-start", (_, isEnabled) => {
  store.set("isAutoStartEnabled", isEnabled)
  app.setLoginItemSettings({
    openAtLogin: isEnabled,
    path: app.getPath("exe"),
  })
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
