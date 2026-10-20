import os
import json
import numpy as np
import pandas as pd
from scipy.optimize import minimize
from scipy.stats import weibull_min

DATA_PATH = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\processed\battery_cycle_summary.csv"
OUTPUT_DIR = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\processed"

def compute_internal_resistance_trend(df):
    """
    Estimate relative internal resistance growth per cycle.
    During discharge at constant current I = 2.0A:
    Terminal Voltage V(t) = OCV - I * R_int - Polarization_losses
    As R_int increases with degradation, the average voltage drops and 
    the rate of temperature increase (dT/dt) climbs via Joule heating (Q_dot = I^2 * R_int).
    """
    I_DISCHARGE = 2.0  # Amperes
    results = []
    for (battery_id, cycle), group in df.groupby(['battery_id', 'cycle']):
        # Approximate dynamic resistance from nominal OCV (~3.8V) and observed avg voltage
        nominal_ocv = 3.8
        avg_v = group['avg_voltage'].values[0]
        max_t = group['max_temp_c'].values[0]
        amb_t = group['ambient_temperature'].values[0]
        capacity = group['capacity_ah'].values[0]
        soh = group['soh_pct'].values[0]
        
        # Effective R_int estimate (Ohms)
        r_eff = max(0.01, (nominal_ocv - avg_v) / I_DISCHARGE)
        delta_t = max_t - amb_t
        joule_heat_watts = (I_DISCHARGE ** 2) * r_eff

        results.append({
            "battery_id": battery_id,
            "cycle": cycle,
            "soh_pct": round(soh, 2),
            "capacity_ah": round(capacity, 4),
            "estimated_r_int_ohms": round(r_eff, 4),
            "delta_temp_c": round(delta_t, 2),
            "joule_heating_watts": round(joule_heat_watts, 4)
        })
    return pd.DataFrame(results)

def fit_weibull_mle(cycles_to_failure):
    """
    Fit 2-parameter Weibull Distribution via Maximum Likelihood Estimation (MLE):
    Reliability Function R(t) = exp(-(t / eta)^beta)
    Cumulative Failure Probability F(t) = 1 - R(t)
    Hazard Rate h(t) = (beta / eta) * (t / eta)^(beta - 1)
    
    beta (Shape parameter):
      beta < 1 : Infant mortality (decreasing hazard rate)
      beta = 1 : Random / exponential failure (constant hazard rate)
      beta > 1 : Wear-out degradation (increasing hazard rate) -> typical for battery fatigue
    eta (Scale parameter / Characteristic Life):
      Cycle at which 63.2% of the battery population has failed.
    """
    t = np.array(cycles_to_failure, dtype=float)
    
    # Scipy fit weibull_min: shape = beta, scale = eta, floc=0 (2-parameter)
    shape, loc, scale = weibull_min.fit(t, floc=0)
    beta = shape
    eta = scale
    
    # Mean Time To Failure (MTTF) / B-lives
    # B10 life: Time when 10% of batteries fail: F(t) = 0.10 => t = eta * (-ln(0.9))^(1/beta)
    # B50 life: Median life (50% failure)
    b10_life = eta * ((-np.log(0.90)) ** (1.0 / beta))
    b50_life = eta * ((-np.log(0.50)) ** (1.0 / beta))
    import math
    mttf = eta * math.gamma(1.0 + 1.0 / beta)
    
    return {
        "beta_shape": round(float(beta), 4),
        "eta_scale_cycles": round(float(eta), 2),
        "mttf_cycles": round(float(mttf), 2),
        "b10_life_cycles": round(float(b10_life), 2),
        "b50_life_cycles": round(float(b50_life), 2),
        "failure_mode": "Wear-out Degradation (Increasing Hazard Rate)" if beta > 1 else "Random Failure"
    }

def main():
    print("Loading cycle summary dataset...")
    df = pd.read_csv(DATA_PATH)
    
    # 1. Physics-based Internal Resistance & Thermal Stress Analysis
    print("Computing electrochemical internal resistance & thermal dissipation metrics...")
    ir_df = compute_internal_resistance_trend(df)
    ir_out_path = os.path.join(OUTPUT_DIR, "battery_internal_resistance_analysis.csv")
    ir_df.to_csv(ir_out_path, index=False)
    print(f"[OK] Saved internal resistance & thermal analysis to: {ir_out_path}")
    
    # Compare early cycle vs late cycle for Cell B0005
    b5_early = ir_df[(ir_df['battery_id'] == 'B0005') & (ir_df['cycle'] == 1)].iloc[0]
    b5_late = ir_df[(ir_df['battery_id'] == 'B0005') & (ir_df['cycle'] == 125)].iloc[0]
    print("\n--- CELL B0005 DEGRADATION DYNAMICS ---")
    print(f"Cycle 1   -> Capacity: {b5_early['capacity_ah']} Ah | SOH: {b5_early['soh_pct']}% | R_int: {b5_early['estimated_r_int_ohms']} Ohm | Delta_T: {b5_early['delta_temp_c']} C | Heat: {b5_early['joule_heating_watts']} W")
    print(f"Cycle 125 -> Capacity: {b5_late['capacity_ah']} Ah | SOH: {b5_late['soh_pct']}% | R_int: {b5_late['estimated_r_int_ohms']} Ohm | Delta_T: {b5_late['delta_temp_c']} C | Heat: {b5_late['joule_heating_watts']} W")
    
    # 2. Weibull Reliability Modeling
    print("\nExtracting End-of-Life (EOL) cycles for Weibull statistical reliability...")
    # Get the unique EOL cycle for each cell
    cell_failures = df.groupby('battery_id')['eol_cycle'].first().to_dict()
    print("Observed EOL cycles per cell:", cell_failures)
    
    failure_cycles = list(cell_failures.values())
    weibull_stats = fit_weibull_mle(failure_cycles)
    
    # Generate CDF / Hazard curve grid (from cycle 0 to 200)
    cycle_grid = np.linspace(1, 200, 200)
    beta = weibull_stats['beta_shape']
    eta = weibull_stats['eta_scale_cycles']
    
    # F(t) = 1 - exp(-(t/eta)^beta)
    cdf_unreliability = 1.0 - np.exp(-((cycle_grid / eta) ** beta))
    # R(t) = exp(-(t/eta)^beta)
    reliability = np.exp(-((cycle_grid / eta) ** beta))
    # Hazard rate h(t) = (beta / eta) * (t / eta)^(beta - 1)
    hazard_rate = (beta / eta) * ((cycle_grid / eta) ** (beta - 1))
    
    curve_df = pd.DataFrame({
        "cycle": cycle_grid,
        "unreliability_F_t": np.round(cdf_unreliability, 5),
        "reliability_R_t": np.round(reliability, 5),
        "hazard_rate_h_t": np.round(hazard_rate, 6)
    })
    curve_out_path = os.path.join(OUTPUT_DIR, "weibull_reliability_curves.csv")
    curve_df.to_csv(curve_out_path, index=False)
    print(f"[OK] Saved Weibull reliability & hazard rate curves to: {curve_out_path}")
    
    # Save the parameters into a JSON file so the Java backend can read and evaluate them directly
    weibull_json_path = os.path.join(OUTPUT_DIR, "weibull_parameters.json")
    with open(weibull_json_path, "w") as f:
        json.dump(weibull_stats, f, indent=4)
    print(f"[OK] Saved Weibull parameters to: {weibull_json_path}")
    
    print("\n================ WEIBULL RELIABILITY REPORT ================")
    print(f"Shape Parameter (Beta):            {weibull_stats['beta_shape']} (Beta > 1 proves physical wear-out fatigue)")
    print(f"Scale Parameter / Char. Life (Eta):{weibull_stats['eta_scale_cycles']} cycles")
    print(f"Mean Time To Failure (MTTF):       {weibull_stats['mttf_cycles']} cycles")
    print(f"B10 Life (10% Fleet Failure):      {weibull_stats['b10_life_cycles']} cycles")
    print(f"B50 Life (Median 50% Failure):     {weibull_stats['b50_life_cycles']} cycles")
    print(f"Failure Regime Classification:     {weibull_stats['failure_mode']}")
    print("============================================================")

if __name__ == "__main__":
    main()
