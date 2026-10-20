import { useState, useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import {
  BatteryCharging,
  Activity,
  Cpu,
  ShieldAlert,
  Zap,
  Flame,
  Clock,
  PlayCircle,
  Sliders,
  CheckCircle2
} from 'lucide-react';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface BatteryFormData {
  batteryName: string;
  ratedCapacityAh: number;
  currentCapacityAh: number;
  cycleCount: number;
  avgOperatingVoltage: number;
  minDischargeVoltage: number;
  maxOperatingTempC: number;
  ambientTempC: number;
  dischargeDurationSec: number;
  dischargeCurrentA: number;
}

interface BatteryAnalysisResponse {
  batteryName: string;
  currentCycle: number;
  calculatedSohPct: number;
  predictedSohPct: number;
  predictedRulCycles: number;
  estimatedTotalCycleLife: number;
  internalResistanceOhms: number;
  thermalRiseDeltaC: number;
  jouleHeatingWatts: number;
  energyDeliveredWh: number;
  currentFailureRiskPct: number;
  b10WarrantyBenchmark: number;
  healthStatus: string;
  projectedDegradationCurve: { cycle: number; sohPct: number; isHistorical?: boolean }[];
}

const API_BASE = 'http://localhost:8080/api';

const PRESETS: Record<string, BatteryFormData> = {
  'New 18650 Cell': {
    batteryName: 'Panasonic NCR18650B (Fresh)',
    ratedCapacityAh: 2.0,
    currentCapacityAh: 1.95,
    cycleCount: 5,
    avgOperatingVoltage: 3.68,
    minDischargeVoltage: 2.9,
    maxOperatingTempC: 30.5,
    ambientTempC: 24.0,
    dischargeDurationSec: 3510,
    dischargeCurrentA: 2.0
  },
  'Mid-Life EV Cell': {
    batteryName: 'EV Pack Module Cell (Cycle 70)',
    ratedCapacityAh: 2.0,
    currentCapacityAh: 1.68,
    cycleCount: 70,
    avgOperatingVoltage: 3.52,
    minDischargeVoltage: 2.65,
    maxOperatingTempC: 37.8,
    ambientTempC: 24.0,
    dischargeDurationSec: 3020,
    dischargeCurrentA: 2.0
  },
  'Degraded Cell (<75% SOH)': {
    batteryName: 'Aged ESS Cell (Degraded)',
    ratedCapacityAh: 2.0,
    currentCapacityAh: 1.48,
    cycleCount: 110,
    avgOperatingVoltage: 3.42,
    minDischargeVoltage: 2.45,
    maxOperatingTempC: 41.2,
    ambientTempC: 24.0,
    dischargeDurationSec: 2660,
    dischargeCurrentA: 2.0
  }
};

export default function App() {
  const [formData, setFormData] = useState<BatteryFormData>(PRESETS['Mid-Life EV Cell']);
  const [analyzing, setAnalyzing] = useState<boolean>(false);

  // Initial calculated state
  const [result, setResult] = useState<BatteryAnalysisResponse>(() => {
    // Generate initial baseline calculation
    const currCap = 1.68;
    const ratedCap = 2.0;
    const cycle = 70;
    const soh = (currCap / ratedCap) * 100.0;
    const rInt = (3.8 - 3.52) / 2.0;
    const deltaT = 37.8 - 24.0;
    const heat = 4.0 * rInt;
    const energy = currCap * 3.52;
    const rul = Math.max(0, Math.round((soh - 70.0) / 0.22));

    const curve = [];
    for (let i = 0; i <= 60; i += 3) {
      curve.push({
        cycle: cycle + i,
        sohPct: Math.round(Math.max(50.0, soh - (i * 0.22)) * 10.0) / 10.0,
        isHistorical: i === 0
      });
    }

    return {
      batteryName: 'EV Pack Module Cell (Cycle 70)',
      currentCycle: 70,
      calculatedSohPct: Math.round(soh * 10.0) / 10.0,
      predictedSohPct: 83.8,
      predictedRulCycles: rul,
      estimatedTotalCycleLife: cycle + rul,
      internalResistanceOhms: Math.round(rInt * 1000.0) / 1000.0,
      thermalRiseDeltaC: Math.round(deltaT * 10.0) / 10.0,
      jouleHeatingWatts: Math.round(heat * 1000.0) / 1000.0,
      energyDeliveredWh: Math.round(energy * 100.0) / 100.0,
      currentFailureRiskPct: 6.8,
      b10WarrantyBenchmark: 85.4,
      healthStatus: 'Moderate Degradation (Within Safe Limits)',
      projectedDegradationCurve: curve
    };
  });

  const handleInputChange = (field: keyof BatteryFormData, val: string) => {
    const num = parseFloat(val);
    setFormData(prev => ({
      ...prev,
      [field]: isNaN(num) ? val : num
    }));
  };

  const handleAnalyze = async () => {
    setAnalyzing(true);
    try {
      const res = await fetch(`${API_BASE}/battery/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      if (res.ok) {
        const data = await res.json();
        setResult(data);
      } else {
        runClientFallback();
      }
    } catch {
      runClientFallback();
    } finally {
      setAnalyzing(false);
    }
  };

  const runClientFallback = () => {
    const curr = formData.currentCapacityAh || 1.85;
    const rated = formData.ratedCapacityAh || 2.0;
    const cyc = formData.cycleCount || 10;
    const soh = Math.round(((curr / rated) * 100.0) * 10.0) / 10.0;
    const rInt = Math.max(0.01, (3.8 - (formData.avgOperatingVoltage || 3.65)) / (formData.dischargeCurrentA || 2.0));
    const deltaT = Math.max(0, (formData.maxOperatingTempC || 35.0) - (formData.ambientTempC || 24.0));
    const heat = (formData.dischargeCurrentA || 2.0) ** 2 * rInt;
    const energy = curr * (formData.avgOperatingVoltage || 3.65);

    // Physics + empirical regression fallback
    const estimatedRul = Math.max(0, Math.round((soh - 70.0) / 0.19));
    const curve = [];
    for (let i = 0; i <= Math.max(20, estimatedRul + 15); i += 4) {
      curve.push({
        cycle: cyc + i,
        sohPct: Math.round(Math.max(50.0, soh - (i * 0.19)) * 10.0) / 10.0
      });
    }

    const failureRisk = Math.min(100.0, Math.round((1.0 - Math.exp(-Math.pow(cyc / 135.91, 4.8488))) * 1000.0) / 10.0);

    let status = 'Optimal Health (Minimal Degradation)';
    if (soh < 70.0) status = 'End-of-Life Exceeded (<70% SOH Replacement Due)';
    else if (soh < 75.0) status = 'Nearing End-of-Life (Inspection Recommended)';
    else if (soh < 85.0) status = 'Moderate Degradation (Within Safe Limits)';

    setResult({
      batteryName: formData.batteryName || 'Custom Battery Cell',
      currentCycle: cyc,
      calculatedSohPct: soh,
      predictedSohPct: soh,
      predictedRulCycles: estimatedRul,
      estimatedTotalCycleLife: cyc + estimatedRul,
      internalResistanceOhms: Math.round(rInt * 1000.0) / 1000.0,
      thermalRiseDeltaC: Math.round(deltaT * 10.0) / 10.0,
      jouleHeatingWatts: Math.round(heat * 1000.0) / 1000.0,
      energyDeliveredWh: Math.round(energy * 100.0) / 100.0,
      currentFailureRiskPct: failureRisk,
      b10WarrantyBenchmark: 85.4,
      healthStatus: status,
      projectedDegradationCurve: curve
    });
  };

  // Memoized Chart 1: Projected SOH Degradation Trajectory
  const projectedChartData = useMemo(() => {
    const pts = result.projectedDegradationCurve || [];
    return {
      labels: pts.map(p => `C${p.cycle}`),
      datasets: [
        {
          label: 'Projected SOH (%)',
          data: pts.map(p => p.sohPct),
          borderColor: '#38bdf8',
          backgroundColor: 'rgba(56, 189, 248, 0.08)',
          fill: true,
          tension: 0.2,
          borderWidth: 2,
          pointRadius: 1
        },
        {
          label: '70% EOL Threshold',
          data: pts.map(() => 70),
          borderColor: '#f87171',
          borderDash: [4, 4],
          borderWidth: 1.5,
          pointRadius: 0
        }
      ]
    };
  }, [result]);

  // Memoized Chart 2: Weibull Fleet Hazard Curve
  const fleetCurveData = useMemo(() => {
    const pts = [];
    for (let c = 1; c <= 160; c += 4) {
      const r = Math.exp(-Math.pow(c / 135.91, 4.8488));
      pts.push({ cycle: c, r: Math.round(r * 1000.0) / 10.0, f: Math.round((1 - r) * 1000.0) / 10.0 });
    }
    return {
      labels: pts.map(p => `C${p.cycle}`),
      datasets: [
        {
          label: 'Fleet Survival R(t)',
          data: pts.map(p => p.r),
          borderColor: '#34d399',
          borderWidth: 1.75,
          tension: 0.15,
          pointRadius: 0
        },
        {
          label: 'Failure Risk F(t)',
          data: pts.map(p => p.f),
          borderColor: '#fbbf24',
          borderWidth: 1.75,
          tension: 0.15,
          pointRadius: 0
        }
      ]
    };
  }, []);

  const chartOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    animation: false as const,
    scales: {
      y: {
        grid: { color: '#2b394f' },
        ticks: { color: '#8a99ad', font: { size: 10 } }
      },
      x: {
        grid: { display: false },
        ticks: { color: '#8a99ad', font: { size: 10 }, maxTicksLimit: 9 }
      }
    },
    plugins: {
      legend: {
        labels: { color: '#cbd5e1', font: { size: 11 }, boxWidth: 12, padding: 8 }
      }
    }
  }), []);

  return (
    <div className="dashboard-container">
      {/* Top Header */}
      <header className="header">
        <div className="logo-area">
          <Zap className="logo-icon" size={22} />
          <div>
            <div className="title-main">VoltTwin: Custom Battery Life & Reliability Calculator</div>
            <div className="tagline">Physics-Informed SOH, Internal Resistance & Remaining Useful Life (RUL) Prognostics</div>
          </div>
        </div>

        <div className="preset-selector">
          <span className="preset-label">Load Preset:</span>
          {Object.keys(PRESETS).map(name => (
            <button
              key={name}
              className="preset-btn"
              onClick={() => {
                setFormData(PRESETS[name]);
              }}
            >
              {name}
            </button>
          ))}
        </div>
      </header>

      {/* Main Split Layout: Left Form vs Right Visual Analytics */}
      <div className="main-split">
        {/* Left Form: Submit Details of Your Own Battery */}
        <aside className="form-panel">
          <div className="panel-header">
            <div className="panel-title"><Sliders size={16} /> Cell Telemetry Inputs</div>
            <span style={{ fontSize: '0.65rem', color: '#64748b' }}>Custom Specs</span>
          </div>

          <div className="form-scrollable">
            <div className="input-group">
              <label className="input-label">Battery Name / Model</label>
              <input
                type="text"
                className="input-field"
                value={formData.batteryName}
                onChange={e => handleInputChange('batteryName', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Rated (Nominal) Capacity <span className="input-unit">Ah</span>
              </label>
              <input
                type="number"
                step="0.1"
                className="input-field"
                value={formData.ratedCapacityAh}
                onChange={e => handleInputChange('ratedCapacityAh', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Measured Discharge Capacity <span className="input-unit">Ah</span>
              </label>
              <input
                type="number"
                step="0.01"
                className="input-field"
                value={formData.currentCapacityAh}
                onChange={e => handleInputChange('currentCapacityAh', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Completed Cycles Count <span className="input-unit">cycles</span>
              </label>
              <input
                type="number"
                className="input-field"
                value={formData.cycleCount}
                onChange={e => handleInputChange('cycleCount', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Average Operating Voltage <span className="input-unit">V</span>
              </label>
              <input
                type="number"
                step="0.01"
                className="input-field"
                value={formData.avgOperatingVoltage}
                onChange={e => handleInputChange('avgOperatingVoltage', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Discharge Cutoff Sag Voltage <span className="input-unit">V</span>
              </label>
              <input
                type="number"
                step="0.05"
                className="input-field"
                value={formData.minDischargeVoltage}
                onChange={e => handleInputChange('minDischargeVoltage', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Peak Operating Temperature <span className="input-unit">°C</span>
              </label>
              <input
                type="number"
                step="0.5"
                className="input-field"
                value={formData.maxOperatingTempC}
                onChange={e => handleInputChange('maxOperatingTempC', e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Discharge Current Load <span className="input-unit">A</span>
              </label>
              <input
                type="number"
                step="0.5"
                className="input-field"
                value={formData.dischargeCurrentA}
                onChange={e => handleInputChange('dischargeCurrentA', e.target.value)}
              />
            </div>
          </div>

          <button className="btn-analyze" onClick={handleAnalyze} disabled={analyzing}>
            <PlayCircle size={16} />
            {analyzing ? 'Computing Prognostics...' : 'Calculate Predicted Life'}
          </button>
        </aside>

        {/* Right Dashboard: Instant Calculations & Future Projections */}
        <section className="results-panel">
          {/* 4 Essential KPI Cards */}
          <div className="kpi-row">
            <div className="card kpi-card">
              <div className="kpi-title"><BatteryCharging size={14} /> State of Health (SOH)</div>
              <div className="kpi-value" style={{ color: '#38bdf8' }}>
                {result.calculatedSohPct.toFixed(1)}%
              </div>
              <div className="kpi-subtext">Rated: {formData.ratedCapacityAh} Ah | Measured: {formData.currentCapacityAh} Ah</div>
            </div>

            <div className="card kpi-card">
              <div className="kpi-title"><Clock size={14} /> Predicted Remaining Life</div>
              <div className="kpi-value" style={{ color: result.predictedRulCycles <= 15 ? '#f87171' : '#34d399' }}>
                {result.predictedRulCycles} <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>cycles</span>
              </div>
              <div className="kpi-subtext">Total Estimated Life: {result.estimatedTotalCycleLife} cycles</div>
            </div>

            <div className="card kpi-card">
              <div className="kpi-title"><Flame size={14} /> Thermal Dissipation (I²R)</div>
              <div className="kpi-value" style={{ color: '#fbbf24' }}>
                {result.jouleHeatingWatts.toFixed(2)} <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>W</span>
              </div>
              <div className="kpi-subtext">ΔT Thermal Rise: +{result.thermalRiseDeltaC}°C above ambient</div>
            </div>

            <div className="card kpi-card">
              <div className="kpi-title"><ShieldAlert size={14} /> Fleet Failure Risk</div>
              <div className="kpi-value" style={{ color: result.currentFailureRiskPct > 15 ? '#f87171' : '#818cf8' }}>
                {result.currentFailureRiskPct.toFixed(1)}%
              </div>
              <div className="kpi-subtext">Weibull B10 Warranty Benchmark: 85.4 cycles</div>
            </div>
          </div>

          {/* Center 2 Analytical Graphs */}
          <div className="charts-row">
            {/* Chart 1: Projected SOH Trajectory */}
            <div className="card chart-card">
              <div className="card-header">
                <div className="card-title">
                  <Activity size={14} /> Projected Degradation Trajectory (Cycles {result.currentCycle} → {result.estimatedTotalCycleLife})
                </div>
                <span className="badge">RUL Forward Extrapolation</span>
              </div>
              <div className="chart-wrapper">
                <Line data={projectedChartData} options={chartOptions} />
              </div>
            </div>

            {/* Chart 2: Weibull Fleet Hazard Curve */}
            <div className="card chart-card">
              <div className="card-header">
                <div className="card-title">
                  <Cpu size={14} /> Fleet Weibull Life & Risk Model (β = 4.85, η = 135.9c)
                </div>
                <span className="badge">RAMS Benchmark</span>
              </div>
              <div className="chart-wrapper">
                <Line data={fleetCurveData} options={chartOptions} />
              </div>
            </div>
          </div>

          {/* Diagnostic Strip: Physics & Health Evaluation */}
          <div className="card diagnostic-card">
            <div className="diagnostic-grid">
              <div className="diagnostic-item">
                <div className="diag-label">Internal Resistance (R_int)</div>
                <div className="diag-val">{result.internalResistanceOhms.toFixed(4)} Ω</div>
              </div>
              <div className="diagnostic-item">
                <div className="diag-label">Energy Delivered per Cycle</div>
                <div className="diag-val">{result.energyDeliveredWh.toFixed(2)} Wh</div>
              </div>
              <div className="diagnostic-item">
                <div className="diag-label">B10 Warranty Milestone</div>
                <div className="diag-val">Cycle {result.b10WarrantyBenchmark} (10% fleet fail)</div>
              </div>
              <div className="diagnostic-item">
                <div className="diag-label">Operating Health Status</div>
                <div className="diag-val" style={{ color: result.calculatedSohPct >= 75 ? '#34d399' : '#f87171', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <CheckCircle2 size={13} /> {result.healthStatus}
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
