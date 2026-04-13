import React from 'react'
import BpmnPage from './pages/BpmnPage'
import './App.css'

const App: React.FC = () => {
  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-title">AI-Powered Process Generation</span>
      </header>
      <main className="app-main">
        <BpmnPage />
      </main>
    </div>
  )
}

export default App
