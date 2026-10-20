import os
import requests
import scipy.io
import pandas as pd
import numpy as np

RAW_DIR = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\raw"
PROCESSED_DIR = r"C:\Users\Shashaank\.gemini\antigravity\scratch\ev-battery-reliability-platform\data\processed"

BATTERIES = ["B0005", "B0006", "B0007", "B0018"]
BASE_URL = "https://raw.githubusercontent.com/anirudhkhatry/SOH-prediction-using-NASA-Dataset/master/{}.mat"

def download_datasets():
    os.makedirs(RAW_DIR, exist_ok=True)
    for b in BATTERIES:
        mat_path = os.path.join(RAW_DIR, f"{b}.mat")
        if not os.path.exists(mat_path):
            url = BASE_URL.format(b)
            print(f"Downloading {b}.mat from {url}...")
            resp = requests.get(url, stream=True, timeout=60)
            resp.raise_for_status()
            with open(mat_path, "wb") as f:
                for chunk in resp.iter_content(chunk_size=8192):
                    f.write(chunk)
            print(f"Downloaded {b}.mat ({os.path.getsize(mat_path) / (1024*1024):.2f} MB)")
        else:
            print(f"{b}.mat already exists.")

def parse_mat_to_csv():
    os.makedirs(PROCESSED_DIR, exist_ok=True)
    all_summary_rows = []

    for b in BATTERIES:
        mat_path = os.path.join(RAW_DIR, f"{b}.mat")
        print(f"\nProcessing {b}...")
        mat = scipy.io.loadmat(mat_path)
        data = mat[b][0, 0]['cycle'][0]
        
        cycle_idx = 0
        for entry in data:
            operation = entry['type'][0]
            ambient_temp = entry['ambient_temperature'][0][0]
            time_val = entry['time'][0]
            d = entry['data']
            
            # Focus on discharge cycles where capacity and degradation are measured
            if operation == 'discharge':
                cycle_idx += 1
                try:
                    capacity = d['Capacity'][0, 0][0][0]
                except Exception:
                    capacity = np.nan
                
                try:
                    voltage_measured = d['Voltage_measured'][0, 0].flatten()
                    current_measured = d['Current_measured'][0, 0].flatten()
                    temp_measured = d['Temperature_measured'][0, 0].flatten()
                    time_measured = d['Time'][0, 0].flatten()
                    
                    min_voltage = float(np.min(voltage_measured))
                    avg_voltage = float(np.mean(voltage_measured))
                    max_temp = float(np.max(temp_measured))
                    avg_temp = float(np.mean(temp_measured))
                    duration_sec = float(time_measured[-1] - time_measured[0])
                except Exception as e:
                    min_voltage, avg_voltage, max_temp, avg_temp, duration_sec = np.nan, np.nan, np.nan, np.nan, np.nan

                all_summary_rows.append({
                    "battery_id": b,
                    "cycle": cycle_idx,
                    "ambient_temperature": float(ambient_temp),
                    "capacity_ah": float(capacity),
                    "min_voltage": min_voltage,
                    "avg_voltage": avg_voltage,
                    "max_temp_c": max_temp,
                    "avg_temp_c": avg_temp,
                    "duration_sec": duration_sec
                })

    df = pd.DataFrame(all_summary_rows)
    
    # Calculate SOH (% of initial rated capacity, nominal 2.0 Ah for 18650 cells)
    RATED_CAPACITY = 2.0
    df['soh_pct'] = (df['capacity_ah'] / RATED_CAPACITY) * 100.0
    
    # Calculate Remaining Useful Life (RUL) in cycles until EOL threshold (70% or 80% SOH, say 1.4 Ah)
    EOL_THRESHOLD = 1.4
    
    rul_list = []
    for b in BATTERIES:
        b_df = df[df['battery_id'] == b].copy()
        eol_cycles = b_df[b_df['capacity_ah'] <= EOL_THRESHOLD]['cycle']
        if not eol_cycles.empty:
            eol_cycle = eol_cycles.iloc[0]
        else:
            eol_cycle = b_df['cycle'].max()
            
        b_df['eol_cycle'] = eol_cycle
        b_df['rul_cycles'] = np.maximum(0, eol_cycle - b_df['cycle'])
        rul_list.append(b_df)
        
    final_df = pd.concat(rul_list, ignore_index=True)
    
    csv_path = os.path.join(PROCESSED_DIR, "battery_cycle_summary.csv")
    final_df.to_csv(csv_path, index=False)
    print(f"\n[SUCCESS] Saved cycle degradation summary to: {csv_path}")
    print(final_df.head(10))
    print(f"\nTotal rows extracted: {len(final_df)}")

if __name__ == "__main__":
    download_datasets()
    parse_mat_to_csv()
