# Mechanical & Statistical Reliability Analysis Report

> **Project:** VoltTwin (EV Battery Digital Twin & Reliability Intelligence Platform)  
> **Domain:** Mechanical Engineering, Electrochemical Degradation & Statistical Reliability (RAMS)  
> **Benchmark Dataset:** NASA Ames Prognostics Center of Excellence (PCoE) 18650 Li-ion Cells (`B0005`, `B0006`, `B0007`, `B0018`)  
> **Script Reference:** [`scripts/mechanical_reliability_analysis.py`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/scripts/mechanical_reliability_analysis.py)

---

## 1. Executive Summary & Physics of Degradation

Lithium-ion cells in Electric Vehicles (EVs) experience irreversible mechanical and electrochemical capacity fading driven by cyclic mechanical stress, lithium inventory loss, and interfacial impedance growth. 

Under constant-current discharge conditions ($I = 2.0\text{ A}$), terminal voltage drops governed by:
$$V(t) = V_{\text{OCV}}(t) - I \cdot R_{\text{int}} - \eta_{\text{polarization}}$$

As cycles accumulate, the growth of the Solid Electrolyte Interphase (SEI) layer on the graphite anode increases the effective cell internal resistance ($R_{\text{int}}$). This directly accelerates Joule heating dissipation according to Joule's First Law:
$$\dot{Q}_{\text{joule}} = I^2 \cdot R_{\text{int}}$$

This thermal energy drives higher localized cell surface temperatures ($T_{\text{max}}$), leading to accelerated electrolyte degradation and thermal stress.

---

## 2. Experimental Degradation Dynamics (Cell `B0005` Benchmark)

Comparison of early life operational parameters against End-of-Life (EOL) failure conditions:

| Operational Metric | Cycle 1 (Fresh Cell) | Cycle 125 (End-of-Life) | Absolute Change ($\Delta$) | Percentage Shift | Physical Degradation Mechanism |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Discharge Capacity** | $1.8565\text{ Ah}$ | $1.3967\text{ Ah}$ | $-0.4598\text{ Ah}$ | **$-24.77\%$** | Loss of cyclable lithium ions (LLI) & loss of active material (LAM) |
| **State of Health ($SOH$)** | $92.82\%$ | $69.84\%$ | $-22.98\%$ | **$-24.77\%$** | Breach of automotive $70\%$ SOH EOL safety threshold |
| **Internal Resistance ($R_{\text{int}}$)** | $0.1351\ \Omega$ | $0.1565\ \Omega$ | $+0.0214\ \Omega$ | **$+15.84\%$** | Thickening of the passivating SEI film & cathode micro-cracking |
| **Thermal Rise ($\Delta T = T_{\text{max}} - T_{\text{amb}}$)** | $14.98^\circ\text{C}$ | $16.86^\circ\text{C}$ | $+1.88^\circ\text{C}$ | **$+12.55\%$** | Higher internal impedance converting more electrical energy to heat |
| **Joule Dissipation ($\dot{Q} = I^2 R$)** | $0.5403\text{ W}$ | $0.6262\text{ W}$ | $+0.0859\text{ W}$ | **$+15.84\%$** | Inefficiency growth causing elevated cooling demand for the pack |

---

## 3. Statistical Reliability & Weibull Life Modeling

To model the fleet failure distribution and determine warranty limits, the observed End-of-Life cycles across the test population were fitted to a **2-parameter Weibull Distribution** using Maximum Likelihood Estimation (MLE).

### Observed Failure Points ($1.4\text{ Ah}$ Cutoff)
- **Cell `B0018`**: $97\text{ cycles}$ (Accelerated stress)
- **Cell `B0006`**: $109\text{ cycles}$
- **Cell `B0005`**: $125\text{ cycles}$
- **Cell `B0007`**: $168\text{ cycles}$ (Surviving / Upper life bound)

### Mathematical Formulations

1. **Reliability Function (Survival Probability):**
   $$R(t) = \exp\left(-\left(\frac{t}{\eta}\right)^\beta\right)$$

2. **Cumulative Failure Distribution (Unreliability):**
   $$F(t) = 1 - R(t) = 1 - \exp\left(-\left(\frac{t}{\eta}\right)^\beta\right)$$

3. **Hazard Rate Function (Instantaneous Failure Rate):**
   $$h(t) = \frac{f(t)}{R(t)} = \left(\frac{\beta}{\eta}\right) \left(\frac{t}{\eta}\right)^{\beta - 1}$$

4. **Mean Time To Failure (MTTF):**
   $$\text{MTTF} = \eta \cdot \Gamma\left(1 + \frac{1}{\beta}\right)$$

---

## 4. Weibull Results & Fleet Reliability Parameters

```json
{
    "beta_shape": 4.8488,
    "eta_scale_cycles": 135.91,
    "mttf_cycles": 124.56,
    "b10_life_cycles": 85.44,
    "b50_life_cycles": 126.01,
    "failure_mode": "Wear-out Degradation (Increasing Hazard Rate)"
}
```

### Engineering Interpretation of Results

1. **Shape Parameter ($\beta = 4.8488$)**:
   - In Bathtub Curve reliability theory:
     - $\beta < 1.0$: Decreasing failure rate (Infant mortality / manufacturing defects).
     - $\beta = 1.0$: Constant failure rate (Random exponential shocks).
     - $\beta > 1.0$: Increasing failure rate (**Wear-out wear/fatigue regime**).
   - A value of $\beta \approx 4.85$ mathematically proves that the cell failures are dominated by **wear-out fatigue and aging**, validating the need for proactive predictive maintenance (PdM).

2. **Characteristic Life ($\eta = 135.91\text{ cycles}$)**:
   - The statistical life milestone at which exactly $63.2\%$ of the cell population has reached End-of-Life.

3. **Mean Time To Failure ($\text{MTTF} = 124.56\text{ cycles}$)**:
   - The expected mean operational life of cells operating under these thermal and discharge stress conditions.

4. **B10 Life Metric ($\text{B10} = 85.44\text{ cycles}$)**:
   - The point in time where $10\%$ of the battery fleet is projected to fail:
     $$t_{B10} = \eta \cdot \left[-\ln(0.90)\right]^{1 / \beta} = 85.44\text{ cycles}$$
   - **Industry Significance**: This metric directly dictates automotive and consumer electronics **warranty periods**. Fleet operators must schedule battery pack overhauls or warranty reserves prior to cycle 85.

5. **B50 Life Metric ($\text{B50} = 126.01\text{ cycles}$)**:
   - The median operational life of the battery fleet ($50\%$ survival rate).

---

## 5. Artifacts & Pipeline Integration

The results of this analysis are exported for live integration into the Java backend and web dashboard:

| File Path | Description | Downstream Consumer |
| :--- | :--- | :--- |
| [`data/processed/weibull_parameters.json`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/data/processed/weibull_parameters.json) | Parsed Weibull parameter payload ($\beta, \eta, \text{MTTF}, B10$) | Ingested by Java Spring Boot `ReliabilityService` |
| [`data/processed/weibull_reliability_curves.csv`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/data/processed/weibull_reliability_curves.csv) | Pre-computed discrete curve samples for $F(t)$, $R(t)$, and $h(t)$ | Rendered by React UI Chart.js / Canvas dashboard |
| [`data/processed/battery_internal_resistance_analysis.csv`](file:///C:/Users/Shashaank/.gemini/antigravity/scratch/ev-battery-reliability-platform/data/processed/battery_internal_resistance_analysis.csv) | Full 636-cycle internal resistance and thermal rise dataset | Used for AI feature engineering and degradation heatmaps |

---

## 6. Interview Talking Points (Mechanical & Reliability Roles)

> *"In this project, I evaluated physical cell degradation by coupling electrochemical impedance growth with thermal fatigue. Across 125 cycles of NASA 18650 testing data, I found that an internal resistance rise of $+15.8\%$ led to an escalated thermal rise ($\Delta T$) of $16.86^\circ\text{C}$ and increased Joule heating by $15.8\%$.*  
> 
> *To translate physical test degradation into fleet reliability metrics, I fitted a 2-parameter Weibull distribution via Maximum Likelihood Estimation. The resulting shape parameter $\beta = 4.85$ confirmed an increasing hazard rate consistent with mechanical wear-out and active material dissolution. From this, I established a fleet B10 warranty benchmark of 85.4 cycles and an MTTF of 124.6 cycles, feeding these parameters directly into the digital twin platform."*
