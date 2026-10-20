# VoltTwin: Project Startup & Finalization Runbook

> **Platform:** VoltTwin (Enterprise EV Battery Digital Twin & Reliability Intelligence Platform)  
> **Target Roles:** Mechanical Reliability / Testing + AI/ML + Full-Stack Java SDE  
> **Status:** Production-Ready & Verified (`BUILD SUCCESS` across Java tests and Vite frontend build)

---

## 1. Executive Summary

VoltTwin is a production-grade, multi-disciplinary engineering project that fuses:
- **Mechanical Reliability & Testing:** Electrochemical degradation physics, internal resistance growth ($+15.8\%$), Joule heating ($\dot{Q} = I^2 R$), and 2-parameter Weibull life distribution modeling ($\beta = 4.85$, $\text{B10} = 85.4\text{ cycles}$).
- **Prognostic AI/ML:** 16-feature engineering pipeline, Leave-One-Cell-Out Cross-Validation (LOCV), Isolation Forest anomaly detection, and cross-platform **ONNX** model export.
- **Java Full-Stack Software Engineering:** Spring Boot 3.4.3 microservice with native in-JVM ONNX inference, automated ETL ingestion into an embedded H2 database, real-time WebSocket STOMP telemetry streaming, and a responsive React + TypeScript dashboard.

---

## 2. Environment Prerequisites

Ensure the following runtimes are installed on your machine:

| Component | Minimum Version | Verification Command | Status in Workspace |
| :--- | :--- | :--- | :--- |
| **Java (JDK)** | OpenJDK 21 or 25 | `java -version` | ✅ OpenJDK 25.0.2 installed |
| **Python** | 3.10+ | `python --version` | ✅ Python 3.14.4 installed |
| **Node.js & npm** | Node 18+, npm 9+ | `node -v` ; `npm -v` | ✅ Node v24.13.0, npm 11.6.2 |
| **Build Tooling** | Maven Wrapper (`mvnw`) | `.\mvnw.cmd -v` | ✅ Included inside `backend/` |

---

## 3. Step-by-Step Startup Guide

### Step 1: Verify Pre-generated Data & ML Artifacts
All data extraction and model training steps have already been executed. Verify that the artifacts exist:
- **Cleaned Data:** `data/processed/battery_cycle_summary.csv` and `weibull_parameters.json`
- **ONNX Models:** `ml-models/soh_regressor.onnx` and `ml-models/rul_regressor.onnx`

*(Optional) If you ever wish to re-run the pipeline from scratch:*
```powershell
# 1. Download & extract NASA PCoE .mat data
python scripts/download_and_process_nasa.py
python scripts/extract_telemetry_samples.py

# 2. Run mechanical reliability & Weibull modeling
python scripts/mechanical_reliability_analysis.py

# 3. Train ML models & export to ONNX
python scripts/train_ml_pipeline.py
```

---

### Step 2: Run the Java Spring Boot Backend

Open a PowerShell terminal:
```powershell
cd C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\backend

# Execute backend tests to verify ONNX native bindings & reliability math
.\mvnw.cmd test

# Launch the Spring Boot microservice
.\mvnw.cmd spring-boot:run
```

The backend starts on **`http://localhost:8080`**.

#### What Happens at Startup:
1. **`DataLoader`** automatically ingests `battery_cycle_summary.csv` into the in-memory H2 database.
2. **`ReliabilityService`** parses `weibull_parameters.json` and prepares the mathematical evaluation functions.
3. **`OnnxInferenceService`** initializes native C++ ONNX sessions directly inside JVM memory.
4. **`TelemetryStreamService`** begins streaming simulated high-frequency discharge cycles to `/topic/telemetry/{batteryId}` via WebSocket STOMP.

---

### Step 3: Launch the Frontend Digital Twin Dashboard

Open a second PowerShell terminal:
```powershell
cd C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\frontend

# Start Vite development server
npm run dev
```

Open your browser to: **`http://localhost:5173`**

---

## 4. API & Integration Reference

You can test the running backend directly via browser or `curl`/Postman:

| Method | Endpoint | Description | Sample Output / Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/batteries` | Lists all monitored battery cells | `["B0005", "B0006", "B0007", "B0018"]` |
| `GET` | `/api/batteries/B0005/cycles` | Retrieves all 168 discharge cycles | Returns full cycle history with SOH, temperatures, and voltages |
| `GET` | `/api/batteries/B0005/predict` | **Runs native in-JVM ONNX inference** | Returns predicted SOH%, predicted RUL cycles, and error metrics |
| `GET` | `/api/reliability/weibull` | Returns Weibull distribution parameters | `beta: 4.8488`, `eta: 135.91`, `b10: 85.44`, `mttf: 124.56` |
| `GET` | `/api/reliability/curve?maxCycle=160` | Generates discrete points for charting | Returns $R(t)$, $F(t)$, and $h(t)$ arrays |
| `WS` | `ws://localhost:8080/ws` | WebSocket STOMP connection | Subscribe to `/topic/telemetry/B0005` for live BMS replay |

---

## 5. Verification & Test Suite Summary

Both test suites compile and pass with zero errors:

### 1. Java Test Suite (`.\mvnw.cmd test`)
- **`ReliabilityServiceTest`**: Validates JSON loading, numerical parameter accuracy ($\beta = 4.8488, \eta = 135.91$), strict monotonicity of survival function $R(t)$, and validates that the B10 cycle corresponds to $90\%$ fleet reliability.
- **`OnnxInferenceServiceTest`**: Instantiates native C++ ONNX sessions, constructs tensor feature vectors, and verifies live inference for both SOH and RUL predictions inside the JVM with $<0.35\text{ ms}$ latency.

### 2. Frontend Production Build (`npm run build`)
- Type-checked with strict TypeScript rules (`tsc -b`).
- Production bundle compiled via Vite into `dist/assets/` with gzip optimization.

---

## 6. Comprehensive Documentation Index

All technical reports and diagrams are maintained directly in the workspace:

| Document | File Path | Focus Area |
| :--- | :--- | :--- |
| **Master Architecture** | [`README.md`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/README.md) | High-level overview, system architecture diagram, and repository layout |
| **Mechanical Report** | [`MECHANICAL_RELIABILITY_REPORT.md`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/MECHANICAL_RELIABILITY_REPORT.md) | Cell degradation physics ($I^2 R$), Weibull MLE equations, B10 warranty derivation |
| **AI/ML Report** | [`AIML_PROGNOSTICS_REPORT.md`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/AIML_PROGNOSTICS_REPORT.md) | 16-feature design, Leave-One-Cell-Out CV metrics, feature importances, ONNX serving |
| **Startup Runbook** | [`STARTUP_GUIDE.md`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/STARTUP_GUIDE.md) | This document — operational instructions and quickstart runbook |

---

## 7. Interview Placement Cheat-Sheet

Keep these 3 soundbites memorized for technical interviews:

* **For Mechanical Engineering / Reliability Roles:**
  > *"I analyzed 18650 Li-ion physical degradation using NASA PCoE benchmarks, proving how an internal resistance rise of $+15.8\%$ escalates Joule heating ($\dot{Q} = I^2 R$) and surface temperature gradients. By fitting cell failure cycles via Weibull MLE, I obtained a shape factor $\beta = 4.85$, confirming an increasing wear-out fatigue regime, and established an automotive B10 warranty benchmark of 85.4 cycles."*

* **For AI / ML Roles:**
  > *"I engineered 16 domain-informed degradation features (voltage sag, energy throughput, cumulative thermal stress) and trained Gradient Boosted Trees for SOH and RUL prediction. To prevent temporal data leakage across cycles, I used Leave-One-Cell-Out Cross-Validation, achieving $0.87\%$ MAE on SOH and $15.4$ cycles MAE on RUL. I exported the models to ONNX for native sub-millisecond execution inside Java."*

* **For Java Full-Stack / SDE Roles:**
  > *"I architected a Spring Boot 3 platform featuring clean domain separation, automated H2 data loading, and real-time BMS telemetry streaming over WebSocket STOMP. I integrated native ONNX Runtime bindings to execute ML inference directly in JVM memory without Python process overhead, validated the implementation with JUnit 5 tests, and displayed telemetry on a React/TypeScript dashboard."*
