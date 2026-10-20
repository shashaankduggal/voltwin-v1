import os
import scipy.io
import pandas as pd
import numpy as np

RAW_DIR = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\raw"
PROCESSED_DIR = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\processed"

def extract_sample_telemetry(battery_id="B0005", cycle_indices=[1, 50, 100]):
    mat_path = os.path.join(RAW_DIR, f"{battery_id}.mat")
    mat = scipy.io.loadmat(mat_path)
    data = mat[battery_id][0, 0]['cycle'][0]
    
    discharge_cycle = 0
    records = []
    
    for entry in data:
        operation = entry['type'][0]
        if operation == 'discharge':
            discharge_cycle += 1
            if discharge_cycle in cycle_indices:
                d = entry['data']
                v = d['Voltage_measured'][0, 0].flatten()
                curr = d['Current_measured'][0, 0].flatten()
                temp = d['Temperature_measured'][0, 0].flatten()
                t = d['Time'][0, 0].flatten()
                
                # Sample every 10th reading to produce realistic high-resolution time series
                step = 5
                for i in range(0, len(t), step):
                    records.append({
                        "battery_id": battery_id,
                        "cycle": discharge_cycle,
                        "time_sec": round(float(t[i]), 2),
                        "voltage_v": round(float(v[i]), 4),
                        "current_a": round(float(curr[i]), 4),
                        "temp_c": round(float(temp[i]), 2)
                    })
                    
    df = pd.DataFrame(records)
    out_path = os.path.join(PROCESSED_DIR, f"{battery_id}_high_freq_telemetry.csv")
    df.to_csv(out_path, index=False)
    print(f"[SUCCESS] Extracted high-resolution telemetry sample to {out_path} ({len(df)} samples)")

if __name__ == "__main__":
    extract_sample_telemetry("B0005", [1, 30, 70, 110])
