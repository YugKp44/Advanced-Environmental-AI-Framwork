import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import EnergyManagement from './pages/EnergyManagement'
import Departments from './pages/Departments'
import Simulation from './pages/Simulation'
import Analytics from './pages/Analytics'
import Alerts from './pages/Alerts'
import Settings from './pages/Settings'
import { getCompanies } from './services/api'
import './App.css'

function App() {
  const [companies, setCompanies] = useState([])
  const [selectedCompany, setSelectedCompany] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadCompanies()
  }, [])

  const loadCompanies = async () => {
    try {
      const data = await getCompanies()
      setCompanies(data)
      if (data.length > 0) {
        setSelectedCompany(data[0])
      }
    } catch (error) {
      console.error('Error loading companies:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    )
  }

  return (
    <Router>
      <div className="app-container">
        {/* Sidebar */}
        <aside className="sidebar">
          <div className="logo">
            <span className="logo-icon">🌱</span>
            <span className="logo-text">EcoAI</span>
          </div>
          
          <nav>
            <ul className="nav-menu">
              <li className="nav-item">
                <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">📊</span>
                  <span className="nav-text">Dashboard</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/energy" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">⚡</span>
                  <span className="nav-text">Energy Usage</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/departments" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">🏢</span>
                  <span className="nav-text">Departments</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/simulation" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">🔮</span>
                  <span className="nav-text">What-If Simulator</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/analytics" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">📈</span>
                  <span className="nav-text">Analytics</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/alerts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">🔔</span>
                  <span className="nav-text">Alerts</span>
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/settings" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">⚙️</span>
                  <span className="nav-text">Settings</span>
                </NavLink>
              </li>
            </ul>
          </nav>

          {/* Company Selector */}
          {companies.length > 0 && (
            <div className="company-selector">
              <select 
                className="form-select"
                value={selectedCompany?.id || ''}
                onChange={(e) => {
                  const company = companies.find(c => c.id === e.target.value)
                  setSelectedCompany(company)
                }}
              >
                {companies.map(company => (
                  <option key={company.id} value={company.id}>
                    {company.name}
                  </option>
                ))}
              </select>
            </div>
          )}
        </aside>

        {/* Main Content */}
        <main className="main-content">
          {selectedCompany ? (
            <Routes>
              <Route path="/" element={<Dashboard company={selectedCompany} />} />
              <Route path="/energy" element={<EnergyManagement company={selectedCompany} />} />
              <Route path="/departments" element={<Departments company={selectedCompany} />} />
              <Route path="/simulation" element={<Simulation company={selectedCompany} />} />
              <Route path="/analytics" element={<Analytics company={selectedCompany} />} />
              <Route path="/alerts" element={<Alerts company={selectedCompany} />} />
              <Route path="/settings" element={<Settings company={selectedCompany} onUpdate={loadCompanies} />} />
            </Routes>
          ) : (
            <div className="card">
              <h2>Welcome to EcoAI Framework</h2>
              <p>No company found. Please create a company in Settings to get started.</p>
              <NavLink to="/settings" className="btn btn-primary">
                Go to Settings
              </NavLink>
            </div>
          )}
        </main>
      </div>
    </Router>
  )
}

export default App
