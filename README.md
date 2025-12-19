# 🌱 EcoAI - AI Energy & Carbon Management Framework

A company-level framework for tracking, attributing, forecasting, and optimizing electricity usage and carbon emissions caused by AI workloads. This enables data-driven sustainability and ESG decisions.

> **Think of it as**: "AWS Cost Explorer + ESG dashboard for AI energy usage"

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

---

## 🎯 What This Framework Does

| Feature | Description |
|---------|-------------|
| ⚡ **Energy Tracking** | Track total company electricity usage (kWh) with daily/monthly data |
| 🤖 **AI Attribution Engine** | Calculate AI energy usage from total consumption using transparent formulas |
| 🌍 **Carbon Calculation** | Convert kWh → CO₂e using region-based carbon intensity factors |
| 💰 **Dual Impact Tracking** | Track both electricity cost and environmental cost together |
| 🔮 **What-If Simulator** | Simulate growth, region changes, and efficiency improvements |
| 📈 **Analytics & Forecasting** | Historical trends, department comparisons, and simple forecasting |
| 🔔 **Alerts & Insights** | Threshold-based alerts and optimization suggestions |
| 📊 **Executive Dashboard** | Professional UI with KPIs, charts, and actionable insights |

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+** & npm
- **PostgreSQL 15+** (optional - H2 is used by default for development)

### 1. Clone and Setup

```bash
git clone <repository-url>
cd "Environmental AI Framwork"
```

### 2. Start Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

H2 Console (for development): `http://localhost:8080/h2-console`

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

The dashboard will be available at `http://localhost:5173`

---

## 📁 Project Structure

```
Environmental AI Framwork/
├── backend/                          # Spring Boot Application
│   ├── pom.xml                       # Maven configuration
│   └── src/main/java/com/ecoai/
│       ├── EcoAiApplication.java     # Main application
│       ├── config/                   # Configuration (CORS, DataInit)
│       ├── entity/                   # JPA Entities (7 entities)
│       ├── repository/               # Spring Data JPA (7 repos)
│       ├── dto/                      # Data Transfer Objects
│       ├── service/                  # Business Logic (8 services)
│       ├── controller/               # REST APIs (8 controllers)
│       └── util/                     # Utilities (CarbonDefaults)
│
├── frontend/                         # React Application
│   ├── package.json
│   └── src/
│       ├── App.jsx                   # Main app with routing
│       ├── index.css                 # Design system
│       ├── services/api.js           # API client
│       └── pages/                    # 7 page components
│           ├── Dashboard.jsx         # Executive overview
│           ├── EnergyManagement.jsx  # Energy CRUD + CSV
│           ├── Departments.jsx       # Department management
│           ├── Simulation.jsx        # What-if simulator
│           ├── Analytics.jsx         # Trends & forecasts
│           ├── Alerts.jsx            # Alerts & insights
│           └── Settings.jsx          # Configuration
│
└── docs/                             # Documentation
```

---

## 🔌 API Endpoints

### Companies
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/companies` | Create company |
| GET | `/api/companies` | List all companies |
| GET | `/api/companies/{id}` | Get company |
| PUT | `/api/companies/{id}` | Update company |

### Energy Usage
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/companies/{id}/energy` | Record energy usage |
| POST | `/api/companies/{id}/energy/csv` | Import from CSV |
| GET | `/api/companies/{id}/energy` | Get energy records |
| GET | `/api/companies/{id}/energy/trends` | Get trend data |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/companies/{id}/dashboard` | Full dashboard data |
| GET | `/api/companies/{id}/dashboard/kpis` | KPI summary |

### Simulation
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/companies/{id}/simulate/growth` | Growth scenario |
| POST | `/api/companies/{id}/simulate/region` | Region change |
| POST | `/api/companies/{id}/simulate/efficiency` | Efficiency gain |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/companies/{id}/analytics/trends` | Historical trends |
| GET | `/api/companies/{id}/analytics/forecast` | Predictions |
| GET | `/api/companies/{id}/analytics/yoy` | Year-over-year |

### Alerts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/companies/{id}/alerts` | Active alerts |
| POST | `/api/companies/{id}/alerts/thresholds` | Configure threshold |
| GET | `/api/companies/{id}/insights` | Optimization tips |

---

## 🧮 Core Formulas

### AI Energy Attribution
```
AI_kWh = Total_kWh × Company_AI_Percentage × Department_Weight
```

Example:
- Total: 10,000 kWh
- Company AI%: 30%
- Dept Weight: 0.8
- **AI Attribution = 10,000 × 0.30 × 0.8 = 2,400 kWh**

### Carbon Calculation
```
CO₂e (grams) = AI_kWh × Carbon_Intensity
CO₂e (kg) = CO₂e (grams) / 1000
```

### Default Carbon Intensities (gCO₂/kWh)
| Region | Intensity | Notes |
|--------|-----------|-------|
| India | 708 | High coal usage |
| USA | 386 | Mixed sources |
| EU | 276 | Average |
| UK | 233 | Good mix |
| Canada | 120 | Hydro power |
| Norway | 26 | Very clean grid |

---

## 📊 Sample Data

The application automatically creates sample data on first startup:
- 1 Company: "TechCorp AI Solutions"
- 4 Departments: ML, Data Science, Development, Operations
- 6 months of daily energy data
- 2 alert thresholds

---

## ⚠️ Important Notes

**This framework is:**
- ✅ An estimation tool for decision-making
- ✅ ESG-reporting ready
- ✅ Configurable and transparent

**This framework is NOT:**
- ❌ Exact carbon measurement
- ❌ GPU-level monitoring
- ❌ A billing system

---

## 🛠️ Configuration

### PostgreSQL (Production)

Edit `backend/src/main/resources/application.properties`:

```properties
# Comment out H2 settings
# spring.datasource.url=jdbc:h2:mem:ecoaidb

# Uncomment PostgreSQL settings
spring.datasource.url=jdbc:postgresql://localhost:5432/ecoai_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Custom Carbon Intensities

Use the Settings page or API to configure custom carbon intensity factors for your regions.

---

## 📄 CSV Import Format

```csv
date,totalKwh,departmentName,region
2024-01-15,500.5,Machine Learning,US
2024-01-15,300.2,Data Science,US
2024-01-16,520.0,Machine Learning,EU
```

---

## 🏗️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA, PostgreSQL/H2
- **Frontend**: React 18, Vite, Recharts, Axios, React Router
- **Build**: Maven, npm, Git

---

## 📝 License

MIT License - Feel free to use and modify for your organization.

---

Built with 💚 for sustainable AI operations
