import React from "react"
import ReactDOM from "react-dom/client"
import App from "./App"
import { HashRouter, Navigate, Route, Routes } from "react-router-dom"
import { AppProvider } from "./contexts/AppContext"

// If you want use Node.js, the`nodeIntegration` needs to be enabled in the Main process.
// import './demos/node'

ReactDOM.createRoot(document.getElementById("root")).render(
  <HashRouter>
    <AppProvider>
      <App />
    </AppProvider>
  </HashRouter>
)

postMessage({ payload: "removeLoading" }, "*")
