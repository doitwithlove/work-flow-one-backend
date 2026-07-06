#!/usr/bin/env python3
"""
Industrial sample data generator for a Hurco M10 CNC machining center.

Run:
    python generate_hurco_m10_data.py
"""

from __future__ import annotations

import csv
import json
import math
import random
import shutil
import string
import zipfile
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import date, datetime, timedelta
from pathlib import Path
from typing import Any

import pandas as pd
from faker import Faker

# ---------------------------------------------------------------------------
# Easy-to-change constants
# ---------------------------------------------------------------------------
SEED = 42
MACHINE_ID = "HURCO-M10-01"
START_DATE = datetime(2026, 7, 1, 6, 0, 0)
NUM_DAYS = 7
TELEMETRY_RECORDS = 50_000
ALARM_HISTORY_RECORDS = 420
QUALITY_INSPECTION_RECORDS = 3_600
ENERGY_CYCLE_RECORDS = 7_200
TOOL_WEAR_RECORDS = 140
KAFKA_EVENT_TARGET = 12_000
OUTPUT_FOLDER_NAME = "hurco_m10_sample_package"
EXCEL_NAME = "Hurco_M10_Industrial_Sample_Datasets.xlsx"
ZIP_NAME = "Hurco_M10_Industrial_Sample_Data_Package.zip"


fake = Faker("en_US")
Faker.seed(SEED)
fake.seed_instance(SEED)
random.seed(SEED)


@dataclass(frozen=True)
class JobSpec:
    job_id: str
    part_number: str
    program: str
    material: str
    operation: str
    ideal_cycle_time_sec: int
    rpm_nominal: int
    feed_nominal: int
    axis_x_nominal: float
    axis_y_nominal: float
    axis_z_nominal: float
    rotary_axis: str | None
    tool_sequence: list[str]
    dimensions: list[tuple[str, float, float, float]]


@dataclass
class ToolState:
    life_pct: float
    cutting_minutes: float = 0.0
    load_sum: float = 0.0
    vib_sum: float = 0.0
    temp_sum: float = 0.0
    samples: int = 0


@dataclass
class TelemetryContext:
    rows: list[dict[str, Any]] = field(default_factory=list)
    completion_events: list[dict[str, Any]] = field(default_factory=list)
    status_events: list[dict[str, Any]] = field(default_factory=list)
    tool_snapshots: list[dict[str, Any]] = field(default_factory=list)
    shift_metrics: list[dict[str, Any]] = field(default_factory=list)


def iso(value: datetime | date) -> str:
    if isinstance(value, datetime):
        return value.isoformat(timespec="seconds")
    return value.isoformat()


def ensure_output_dir(base_dir: Path) -> Path:
    output_dir = base_dir / OUTPUT_FOLDER_NAME
    output_dir.mkdir(parents=True, exist_ok=True)
    return output_dir


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def weighted_choice(options: list[tuple[Any, float]]) -> Any:
    total = sum(weight for _, weight in options)
    marker = random.uniform(0, total)
    running = 0.0
    for value, weight in options:
        running += weight
        if marker <= running:
            return value
    return options[-1][0]


def build_job_catalog() -> list[JobSpec]:
    return [
        JobSpec(
            "JOB-M10-1001",
            "M10-BRKT-AL6061",
            "H1001",
            "Aluminum 6061",
            "Facing",
            72,
            7800,
            1650,
            182.5,
            96.0,
            -18.0,
            None,
            ["EM06", "FM50", "DR05"],
            [("Face Flatness", 0.035, 0.010, -0.010), ("Hole Position", 0.040, 0.015, -0.015)],
        ),
        JobSpec(
            "JOB-M10-1002",
            "M10-PLATE-AL7075",
            "H1002",
            "Aluminum 7075",
            "Pocketing",
            64,
            9000,
            2100,
            245.0,
            140.0,
            -20.0,
            "A",
            ["FM50", "EM10", "CH02"],
            [("Pocket Depth", 12.000, 0.015, -0.015), ("Perimeter Profile", 0.030, 0.010, -0.010)],
        ),
        JobSpec(
            "JOB-M10-1003",
            "M10-HSG-C45",
            "H1003",
            "C45 Steel",
            "Drilling",
            58,
            6200,
            1420,
            168.0,
            88.5,
            -16.0,
            "B",
            ["DR05", "TAP08", "CH02"],
            [("Drill Diameter", 8.500, 0.020, -0.020), ("Hole Depth", 0.050, 0.010, -0.010)],
        ),
        JobSpec(
            "JOB-M10-1004",
            "M10-FIXTURE-316L",
            "H1004",
            "316L Stainless",
            "Tapping",
            84,
            5400,
            980,
            206.0,
            120.0,
            -24.0,
            None,
            ["EM10", "TAP08", "CH02"],
            [("Thread Pitch", 1.250, 0.010, -0.010), ("Tap Depth", 16.000, 0.020, -0.020)],
        ),
        JobSpec(
            "JOB-M10-1005",
            "M10-COVER-AL5052",
            "H1005",
            "Aluminum 5052",
            "Contouring",
            52,
            8600,
            1760,
            221.0,
            104.0,
            -22.0,
            "A",
            ["EM06", "BN08", "CH02"],
            [("Edge Profile", 0.025, 0.010, -0.010), ("Contour Finish", 1.600, 0.200, -0.200)],
        ),
        JobSpec(
            "JOB-M10-1006",
            "M10-BRKT-STEEL",
            "H1001",
            "4140 Steel",
            "Finishing",
            98,
            7200,
            1250,
            178.0,
            92.0,
            -19.0,
            None,
            ["EM10", "BN08", "CH02"],
            [("Surface Finish", 1.200, 0.150, -0.150), ("Hole Location", 0.040, 0.015, -0.015)],
        ),
        JobSpec(
            "JOB-M10-1007",
            "M10-PLATE-AL2024",
            "H1002",
            "Aluminum 2024",
            "Facing",
            44,
            9200,
            1980,
            238.0,
            132.0,
            -17.0,
            None,
            ["FM50", "EM06", "DR05"],
            [("Flatness", 0.030, 0.010, -0.010), ("Slot Width", 0.020, 0.010, -0.010)],
        ),
        JobSpec(
            "JOB-M10-1008",
            "M10-HOUSING-CAST",
            "H1003",
            "Cast Iron",
            "Pocketing",
            128,
            5600,
            1120,
            194.0,
            102.0,
            -26.0,
            "B",
            ["FM50", "EM10", "DR05", "CH02"],
            [("Bore Diameter", 34.000, 0.020, -0.020), ("Parallelism", 0.035, 0.010, -0.010)],
        ),
    ]


def build_machine_catalog() -> list[dict[str, Any]]:
    return [
        {
            "_id": "HURCO-M10-01",
            "machineId": "HURCO-M10-01",
            "name": "Hurco M10",
            "model": "M10 Machining Center",
            "manufacturer": "Hurco",
            "controller": "WinMax",
            "status": "RUNNING",
            "location": "Cell A / Line 2",
            "spindleMaxRpm": 12000,
            "feedMaxMmMin": 5400,
            "axes": ["X", "Y", "Z"],
            "rotaryAxesAvailable": ["A", "B"],
            "createdAt": iso(START_DATE),
            "updatedAt": iso(START_DATE + timedelta(days=NUM_DAYS - 1, hours=8)),
        },
    ]


def build_tool_catalog() -> list[dict[str, str]]:
    return [
        {"tool": "EM06", "toolType": "End Mill 6mm"},
        {"tool": "EM10", "toolType": "End Mill 10mm"},
        {"tool": "FM50", "toolType": "Face Mill 50mm"},
        {"tool": "DR05", "toolType": "Drill 5mm"},
        {"tool": "DR10", "toolType": "Drill 10mm"},
        {"tool": "TAP08", "toolType": "Tap M8"},
        {"tool": "CH02", "toolType": "Chamfer Mill 2mm"},
        {"tool": "BN08", "toolType": "Ball Nose 8mm"},
    ]


def build_alarm_catalog() -> list[dict[str, str]]:
    return [
        {"alarmCode": "H101", "alarmType": "TOOL_CHANGE", "severity": "HIGH", "description": "Tool changer fault detected", "operatorAction": "Checked magazine and reloaded the tool pocket"},
        {"alarmCode": "H205", "alarmType": "SPINDLE", "severity": "HIGH", "description": "Spindle overload detected", "operatorAction": "Reduced feed and verified cutter engagement"},
        {"alarmCode": "H312", "alarmType": "COOLANT", "severity": "MEDIUM", "description": "Coolant level low", "operatorAction": "Refilled coolant and restarted the pump"},
        {"alarmCode": "H405", "alarmType": "SERVO", "severity": "HIGH", "description": "Servo following error on X axis", "operatorAction": "Re-homed the axes and inspected servo load"},
        {"alarmCode": "H415", "alarmType": "SAFETY", "severity": "HIGH", "description": "Door open during cycle", "operatorAction": "Cleared the cell and reset the door interlock"},
        {"alarmCode": "H501", "alarmType": "SAFETY", "severity": "CRITICAL", "description": "Emergency stop pressed", "operatorAction": "Investigated the stop condition and reset the panel"},
        {"alarmCode": "H609", "alarmType": "AIR", "severity": "MEDIUM", "description": "Air pressure low", "operatorAction": "Checked air supply and verified pressure recovery"},
    ]


def build_operator_roster() -> list[dict[str, Any]]:
    roster = []
    for idx in range(1, 11):
        roster.append(
            {
                "_id": f"OP-{idx:03d}",
                "operatorId": f"OP-{idx:03d}",
                "operatorName": fake.name(),
                "skillLevel": random.choice(["Junior", "Standard", "Senior"]),
                "certification": random.choice(["CNC LATHE", "TURN-MILL", "MULTI-AXIS"]),
                "active": True,
            }
        )
    return roster


def shift_windows(start: datetime) -> list[dict[str, Any]]:
    shifts: list[dict[str, Any]] = []
    for day_idx in range(NUM_DAYS):
        day_start = start + timedelta(days=day_idx)
        for code, offset_hours in (("A", 0), ("B", 8), ("C", 16)):
            shift_start = day_start + timedelta(hours=offset_hours)
            shift_end = shift_start + timedelta(hours=8)
            shifts.append(
                {
                    "date": shift_start.date(),
                    "shift": code,
                    "start": shift_start,
                    "end": shift_end,
                    "day_idx": day_idx,
                }
            )
    return shifts


def format_status(status: str) -> str:
    return status.upper()


def make_object_id(prefix: str, index: int) -> str:
    return f"{prefix}{index:024x}"[-24:]


def build_telemetry() -> TelemetryContext:
    jobs = build_job_catalog()
    tool_catalog = build_tool_catalog()
    alarms = build_alarm_catalog()
    shifts = shift_windows(START_DATE)

    tool_states: dict[str, ToolState] = {
        tool["tool"]: ToolState(life_pct=random.uniform(88.0, 99.6)) for tool in tool_catalog
    }
    tool_by_name = {tool["tool"]: tool["toolType"] for tool in tool_catalog}

    context = TelemetryContext()
    global_part_count = 0

    records_remaining = TELEMETRY_RECORDS
    base_records = TELEMETRY_RECORDS // len(shifts)
    remainder = TELEMETRY_RECORDS % len(shifts)
    records_per_shift = [base_records + (1 if idx < remainder else 0) for idx in range(len(shifts))]

    for shift_index, (shift_meta, row_count) in enumerate(zip(shifts, records_per_shift, strict=True)):
        job = jobs[(shift_index * 2 + shift_meta["day_idx"]) % len(jobs)]
        alternative_job = jobs[(shift_index + 3) % len(jobs)]

        # Create a realistic time series that stays within the 8-hour shift window.
        shift_seconds = int((shift_meta["end"] - shift_meta["start"]).total_seconds())
        raw_intervals = [max(2.0, random.gauss(11.8, 2.7)) for _ in range(row_count)]
        scale = (shift_seconds * 0.98) / sum(raw_intervals)
        intervals = [interval * scale for interval in raw_intervals]

        timestamps: list[datetime] = []
        current_ts = shift_meta["start"]
        for interval in intervals:
            current_ts += timedelta(seconds=interval)
            timestamps.append(current_ts)

        statuses = [random.choices(
            ["RUNNING", "IDLE", "SETUP", "TOOL_CHANGE", "MAINTENANCE", "ALARM"],
            weights=[74, 10, 8, 4, 2, 2],
            k=1,
        )[0] for _ in range(row_count)]

        # Force some consistent operational windows so the data looks like a real shift.
        setup_window = min(max(24, row_count // 64), 70)
        for idx in range(setup_window):
            statuses[idx] = "SETUP"

        if shift_index % 5 == 3:
            maintenance_start = row_count // 2
            maintenance_end = min(row_count, maintenance_start + max(28, row_count // 72))
            for idx in range(maintenance_start, maintenance_end):
                statuses[idx] = "MAINTENANCE"

        for _ in range(random.randint(1, 3)):
            change_start = random.randint(setup_window + 15, max(setup_window + 16, row_count - 90))
            change_length = random.randint(5, 14)
            for idx in range(change_start, min(row_count, change_start + change_length)):
                if statuses[idx] == "RUNNING":
                    statuses[idx] = "TOOL_CHANGE"

        for _ in range(random.randint(1, 3)):
            alarm_start = random.randint(setup_window + 20, max(setup_window + 21, row_count - 80))
            alarm_length = random.randint(6, 16)
            for idx in range(alarm_start, min(row_count, alarm_start + alarm_length)):
                statuses[idx] = "ALARM"

        for _ in range(random.randint(2, 4)):
            idle_start = random.randint(setup_window + 40, max(setup_window + 41, row_count - 60))
            idle_length = random.randint(8, 22)
            for idx in range(idle_start, min(row_count, idle_start + idle_length)):
                if statuses[idx] == "RUNNING":
                    statuses[idx] = "IDLE"

        shift_running_seconds = 0.0
        shift_idle_seconds = 0.0
        shift_downtime_seconds = 0.0
        shift_total_count_start = global_part_count
        shift_energy_running_kw = 0.0
        shift_energy_idle_kw = 0.0
        shift_cycle_targets: list[int] = []
        running_samples = 0
        total_tool_load = defaultdict(float)
        total_tool_vibration = defaultdict(float)
        total_tool_temperature = defaultdict(float)
        total_tool_minutes = defaultdict(float)
        active_tools: set[str] = set()
        current_cycle_elapsed = 0.0
        current_cycle_target = float(job.ideal_cycle_time_sec)
        previous_status = None
        tool_change_count = 0

        for row_idx, (timestamp, interval, status) in enumerate(zip(timestamps, intervals, statuses, strict=True)):
            if row_idx and row_idx % 300 == 0 and random.random() < 0.35:
                job = random.choice([job, alternative_job])
                current_cycle_target = float(job.ideal_cycle_time_sec) * random.uniform(0.92, 1.10)

            selected_tool = job.tool_sequence[(row_idx // 42) % len(job.tool_sequence)]
            active_tools.add(selected_tool)
            tool_state = tool_states[selected_tool]

            if status == "RUNNING":
                shift_running_seconds += interval
                running_samples += 1
                current_cycle_elapsed += interval
                current_cycle_target = float(job.ideal_cycle_time_sec) * random.uniform(0.92, 1.12)

                # Wear tools gradually based on cutting time and material hardness.
                wear_multiplier = {
                    "Aluminum 6061": 0.0009,
                    "Aluminum 7075": 0.0010,
                    "Brass": 0.0006,
                    "4140 Steel": 0.0017,
                    "316L Stainless": 0.0020,
                    "17-4 PH Stainless": 0.0019,
                    "Titanium Ti-6Al-4V": 0.0025,
                    "C45 Steel": 0.0015,
                    "PA66 Nylon": 0.0004,
                    "Inconel 718": 0.0028,
                    "Copper Nickel": 0.0012,
                }.get(job.material, 0.0011)
                tool_state.life_pct = max(5.0, tool_state.life_pct - interval * wear_multiplier * random.uniform(0.85, 1.30))
                tool_state.cutting_minutes += interval / 60.0
                tool_state.samples += 1

                base_load = random.uniform(35.0, 88.0)
                spindle_rpm = int(clamp(random.gauss(job.rpm_nominal, job.rpm_nominal * 0.08), 900, 9000))
                feed_rate = round(clamp(random.gauss(job.feed_nominal, job.feed_nominal * 0.10), 60, 2800), 1)
                spindle_load = round(clamp(base_load + (100 - tool_state.life_pct) * 0.16, 12, 96), 1)
                servo_load = round(clamp(spindle_load * random.uniform(0.55, 0.88), 8, 92), 1)
                temperature = round(clamp(38 + spindle_load * 0.22 + random.uniform(-1.5, 3.4), 34, 78), 1)
                vibration = round(clamp(random.uniform(0.18, 1.6) + (100 - tool_state.life_pct) * 0.012, 0.05, 3.5), 3)
                coolant = "ON"
                lubrication = "ON"
                power = round(clamp(8 + spindle_load * 0.24 + feed_rate * 0.003 + random.uniform(-0.8, 2.5), 3, 42), 2)
                axis_x = round(job.axis_x_nominal + math.sin(row_idx / 13.0) * 0.18 + random.uniform(-0.08, 0.08), 3)
                axis_y = round(job.axis_y_nominal + random.uniform(-0.12, 0.12), 3)
                axis_z = round(job.axis_z_nominal + math.cos(row_idx / 17.0) * 0.22 + random.uniform(-0.08, 0.08), 3)
                axis_a = round((row_idx * 6.8 + random.uniform(-1.4, 1.4)) % 360, 3) if job.rotary_axis == "A" else 0.0
                axis_b = round((row_idx * 5.4 + random.uniform(-1.2, 1.2)) % 360, 3) if job.rotary_axis == "B" else 0.0
                air_pressure = round(clamp(random.uniform(5.8, 7.2), 4.8, 8.0), 1)
                cycle_time = int(round(current_cycle_target))
                alarm_code = ""
                part_increment = 0
                if current_cycle_elapsed >= current_cycle_target:
                    part_increment = int(current_cycle_elapsed // current_cycle_target)
                    current_cycle_elapsed = current_cycle_elapsed % current_cycle_target
                    for _ in range(part_increment):
                        global_part_count += 1
                        reject_chance = 0.010 + (100 - tool_state.life_pct) / 4200.0 + max(0.0, spindle_load - 78) / 1000.0
                        rejected = random.random() < reject_chance
                        completion_event = {
                            "eventId": f"EV-PART-{len(context.completion_events) + 1:07d}",
                            "eventType": "PART_COMPLETED",
                            "timestamp": iso(timestamp),
                            "machineId": MACHINE_ID,
                            "payload": {
                                "jobId": job.job_id,
                                "partNumber": job.part_number,
                                "program": job.program,
                                "cycleNo": global_part_count,
                                "result": "REJECT" if rejected else "GOOD",
                                "tool": selected_tool,
                                "cycleTimeSec": cycle_time,
                            },
                        }
                        context.completion_events.append(completion_event)
                        if rejected:
                            alarm_like = random.choice(alarms)
                            context.status_events.append(
                                {
                                    "eventId": f"EV-STATUS-{len(context.status_events) + 1:07d}",
                                    "eventType": "QUALITY_INSPECTION_COMPLETED",
                                    "timestamp": iso(timestamp),
                                    "machineId": MACHINE_ID,
                                    "payload": {
                                        "jobId": job.job_id,
                                        "partNumber": job.part_number,
                                        "cycleNo": global_part_count,
                                        "result": "FAIL",
                                        "reason": alarm_like["alarmCode"],
                                    },
                                }
                            )
                shift_cycle_targets.append(cycle_time)
                shift_energy_running_kw += power
            elif status == "IDLE":
                shift_idle_seconds += interval
                shift_downtime_seconds += interval * 0.35
                spindle_rpm = 0
                feed_rate = 0.0
                spindle_load = round(random.uniform(0.0, 7.5), 1)
                servo_load = round(random.uniform(0.0, 3.5), 1)
                temperature = round(clamp(33 + random.uniform(0, 6), 31, 45), 1)
                vibration = round(random.uniform(0.005, 0.11), 3)
                air_pressure = round(random.uniform(5.0, 6.8), 1)
                coolant = "OFF"
                lubrication = random.choice(["AUTO", "ON"])
                power = round(random.uniform(0.35, 2.1), 2)
                axis_x = round(job.axis_x_nominal + random.uniform(-0.4, 0.4), 3)
                axis_y = round(job.axis_y_nominal + random.uniform(-0.25, 0.25), 3)
                axis_z = round(job.axis_z_nominal + random.uniform(-0.4, 0.4), 3)
                axis_a = round(random.uniform(0, 360), 3) if job.rotary_axis == "A" else 0.0
                axis_b = round(random.uniform(0, 360), 3) if job.rotary_axis == "B" else 0.0
                cycle_time = 0
                alarm_code = ""
                current_cycle_elapsed = 0.0
                shift_energy_idle_kw += power
            elif status == "SETUP":
                shift_downtime_seconds += interval * 0.75
                spindle_rpm = int(clamp(random.gauss(job.rpm_nominal * 0.20, 160), 0, 1600))
                feed_rate = round(clamp(random.gauss(job.feed_nominal * 0.12, 35), 0, 500), 1)
                spindle_load = round(random.uniform(4.0, 20.0), 1)
                servo_load = round(random.uniform(6.0, 24.0), 1)
                temperature = round(clamp(34 + random.uniform(0, 9), 32, 48), 1)
                vibration = round(random.uniform(0.02, 0.28), 3)
                air_pressure = round(random.uniform(5.8, 7.6), 1)
                coolant = "AUTO"
                lubrication = "ON"
                power = round(random.uniform(1.5, 6.8), 2)
                axis_x = round(job.axis_x_nominal + random.uniform(-0.8, 0.8), 3)
                axis_y = round(job.axis_y_nominal + random.uniform(-0.5, 0.5), 3)
                axis_z = round(job.axis_z_nominal + random.uniform(-0.8, 0.8), 3)
                axis_a = round(random.uniform(0, 360), 3) if job.rotary_axis == "A" else 0.0
                axis_b = round(random.uniform(0, 360), 3) if job.rotary_axis == "B" else 0.0
                cycle_time = int(round(job.ideal_cycle_time_sec * random.uniform(0.4, 0.75)))
                alarm_code = ""
                current_cycle_elapsed = 0.0
            elif status == "TOOL_CHANGE":
                shift_downtime_seconds += interval * 0.85
                tool_change_count += 1
                spindle_rpm = 0
                feed_rate = 0.0
                spindle_load = round(random.uniform(0.0, 8.0), 1)
                servo_load = round(random.uniform(0.0, 10.0), 1)
                temperature = round(clamp(34 + random.uniform(0, 6), 31, 46), 1)
                vibration = round(random.uniform(0.01, 0.2), 3)
                air_pressure = round(random.uniform(5.8, 7.2), 1)
                coolant = "OFF"
                lubrication = "ON"
                power = round(random.uniform(0.3, 2.2), 2)
                axis_x = round(job.axis_x_nominal + random.uniform(-0.3, 0.3), 3)
                axis_y = round(job.axis_y_nominal + random.uniform(-0.3, 0.3), 3)
                axis_z = round(job.axis_z_nominal + random.uniform(-0.3, 0.3), 3)
                axis_a = round(random.uniform(0, 360), 3) if job.rotary_axis == "A" else 0.0
                axis_b = round(random.uniform(0, 360), 3) if job.rotary_axis == "B" else 0.0
                cycle_time = 0
                alarm_code = ""
                current_cycle_elapsed = 0.0
            elif status == "MAINTENANCE":
                shift_downtime_seconds += interval
                spindle_rpm = 0
                feed_rate = 0.0
                spindle_load = round(random.uniform(0.0, 4.5), 1)
                servo_load = round(random.uniform(0.0, 9.0), 1)
                temperature = round(clamp(35 + random.uniform(0, 6), 32, 46), 1)
                vibration = round(random.uniform(0.01, 0.18), 3)
                air_pressure = round(random.uniform(4.8, 6.6), 1)
                coolant = "OFF"
                lubrication = "OFF"
                power = round(random.uniform(0.2, 1.8), 2)
                axis_x = round(job.axis_x_nominal, 3)
                axis_y = round(job.axis_y_nominal, 3)
                axis_z = round(job.axis_z_nominal, 3)
                axis_a = 0.0
                axis_b = 0.0
                cycle_time = 0
                alarm_code = "MNT-001"
                current_cycle_elapsed = 0.0
            else:  # ALARM
                shift_downtime_seconds += interval
                alarm_pick = random.choice(alarms)
                spindle_rpm = 0
                feed_rate = 0.0
                spindle_load = round(random.uniform(0.0, 14.0), 1)
                servo_load = round(random.uniform(1.0, 18.0), 1)
                temperature = round(clamp(44 + random.uniform(0, 14), 38, 76), 1)
                vibration = round(random.uniform(0.15, 3.4), 3)
                air_pressure = round(random.uniform(4.0, 6.4), 1)
                coolant = "OFF"
                lubrication = random.choice(["LOW", "OFF"])
                power = round(random.uniform(0.4, 4.8), 2)
                axis_x = round(job.axis_x_nominal + random.uniform(-0.2, 0.2), 3)
                axis_y = round(job.axis_y_nominal + random.uniform(-0.2, 0.2), 3)
                axis_z = round(job.axis_z_nominal + random.uniform(-0.2, 0.2), 3)
                axis_a = round(random.uniform(0, 360), 3) if job.rotary_axis == "A" else 0.0
                axis_b = round(random.uniform(0, 360), 3) if job.rotary_axis == "B" else 0.0
                cycle_time = 0
                alarm_code = alarm_pick["alarmCode"]
                current_cycle_elapsed = 0.0

            if previous_status != status:
                context.status_events.append(
                    {
                        "eventId": f"EV-STATE-{len(context.status_events) + 1:07d}",
                        "eventType": "TOOL_CHANGE" if status == "TOOL_CHANGE" else "MACHINE_STATUS_CHANGED",
                        "timestamp": iso(timestamp),
                        "machineId": MACHINE_ID,
                        "payload": {
                            "jobId": job.job_id,
                            "fromStatus": previous_status or "UNKNOWN",
                            "toStatus": status,
                            "tool": selected_tool,
                            "toolChangeCount": tool_change_count,
                        },
                    }
                )
                previous_status = status

            tool_life_pct = round(clamp(tool_state.life_pct + random.uniform(-0.6, 0.6), 0, 100), 1)
            tool_record = {
                "timestamp": iso(timestamp),
                "machineId": MACHINE_ID,
                "jobId": job.job_id,
                "partNumber": job.part_number,
                "program": job.program,
                "status": format_status(status),
                "spindleRpm": spindle_rpm,
                "feedRate": feed_rate,
                "axisX": axis_x,
                "axisY": axis_y,
                "axisZ": axis_z,
                "axisA": axis_a,
                "axisB": axis_b,
                "tool": selected_tool,
                "toolLifePct": tool_life_pct,
                "spindleLoadPct": spindle_load,
                "servoLoadPct": servo_load,
                "temperatureC": temperature,
                "vibrationMmSec": vibration,
                "coolantStatus": coolant,
                "airPressureBar": air_pressure,
                "lubricationStatus": lubrication,
                "cycleTimeSec": cycle_time,
                "alarmCode": alarm_code,
                "powerKW": power,
                "partCount": global_part_count,
                "toolChangeCount": tool_change_count,
            }
            context.rows.append(tool_record)

            total_tool_load[selected_tool] += spindle_load
            total_tool_vibration[selected_tool] += vibration
            total_tool_temperature[selected_tool] += temperature
            total_tool_minutes[selected_tool] += interval / 60.0

        shift_total_count_end = global_part_count
        total_count = shift_total_count_end - shift_total_count_start
        planned_production_min = 450
        runtime_min = round(shift_running_seconds / 60.0, 1)
        downtime_min = round(max(0.0, planned_production_min - runtime_min), 1)
        ideal_cycle = round(sum(shift_cycle_targets) / max(1, len(shift_cycle_targets)), 1) if shift_cycle_targets else float(job.ideal_cycle_time_sec)
        reject_count = max(0, int(round(total_count * random.uniform(0.012, 0.038))))
        good_count = max(0, total_count - reject_count)
        availability = round(clamp(runtime_min / planned_production_min * 100.0, 0, 100), 2)
        performance = round(clamp((ideal_cycle * total_count) / max(1.0, runtime_min * 60.0) * 100.0, 0, 100), 2)
        quality = round(clamp(good_count / max(1, total_count) * 100.0, 0, 100), 2)
        oee = round(availability * performance * quality / 10000.0, 2)

        context.shift_metrics.append(
            {
                "date": shift_meta["date"],
                "shift": shift_meta["shift"],
                "machineId": MACHINE_ID,
                "jobId": job.job_id,
                "partNumber": job.part_number,
                "program": job.program,
                "material": job.material,
                "plannedProductionTimeMin": planned_production_min,
                "runtimeMin": runtime_min,
                "downtimeMin": downtime_min,
                "idealCycleTimeSec": ideal_cycle,
                "totalCount": total_count,
                "goodCount": good_count,
                "rejectCount": reject_count,
                "availabilityPct": availability,
                "performancePct": performance,
                "qualityPct": quality,
                "oeePct": oee,
                "runningSeconds": shift_running_seconds,
                "shiftRunningKw": round(shift_energy_running_kw / max(1, running_samples), 2) if running_samples else 0.0,
                "shiftIdleKw": round(shift_energy_idle_kw / max(1, int(shift_idle_seconds > 0)), 2) if shift_idle_seconds else 0.0,
            }
        )

        for tool_name in sorted(active_tools):
            minutes = total_tool_minutes.get(tool_name, 0.0)
            avg_load = round(total_tool_load.get(tool_name, 0.0) / max(1, tool_states[tool_name].samples), 2)
            avg_vibration = round(total_tool_vibration.get(tool_name, 0.0) / max(1, tool_states[tool_name].samples), 3)
            avg_temp = round(total_tool_temperature.get(tool_name, 0.0) / max(1, tool_states[tool_name].samples), 2)
            life = round(tool_states[tool_name].life_pct, 1)
            wear_level, recommendation = tool_wear_band(life)
            context.tool_snapshots.append(
                {
                    "timestamp": iso(shift_meta["end"]),
                    "machineId": MACHINE_ID,
                    "tool": tool_name,
                    "toolType": tool_by_name[tool_name],
                    "material": job.material,
                    "toolLifePct": life,
                    "cuttingTimeMin": round(minutes, 1),
                    "spindleLoadAvgPct": avg_load,
                    "vibrationAvgMmSec": avg_vibration,
                    "temperatureAvgC": avg_temp,
                    "wearLevel": wear_level,
                    "predictedRemainingLifeMin": round(max(0.0, minutes * (life / 100.0) * random.uniform(2.4, 4.9)), 1),
                    "maintenanceRecommendation": recommendation,
                }
            )

        records_remaining -= row_count

    # Ensure we actually generated the requested telemetry count.
    if len(context.rows) != TELEMETRY_RECORDS:
        raise RuntimeError(f"Telemetry row count mismatch: {len(context.rows)} != {TELEMETRY_RECORDS}")

    return context


def tool_wear_band(life_pct: float) -> tuple[str, str]:
    if life_pct >= 85:
        return "LOW", "Continue production and monitor tool wear trend"
    if life_pct >= 70:
        return "MODERATE", "Inspect tool at next planned changeover"
    if life_pct >= 50:
        return "HIGH", "Prepare replacement tool and verify offset compensation"
    return "CRITICAL", "Change tool immediately and run inspection checks"


def build_alarm_history(context: TelemetryContext) -> list[dict[str, Any]]:
    alarm_catalog = build_alarm_catalog()
    alarm_rows: list[dict[str, Any]] = []

    # Reuse telemetry alarms when possible so alarm history follows the machine timeline.
    alarm_telemetry = [row for row in context.rows if row["status"] == "ALARM" and row["alarmCode"]]
    sample_pool = alarm_telemetry or context.rows
    for idx in range(ALARM_HISTORY_RECORDS):
        source = sample_pool[idx % len(sample_pool)]
        catalog_entry = next((item for item in alarm_catalog if item["alarmCode"] == source["alarmCode"]), random.choice(alarm_catalog))
        duration = int(clamp(random.gauss(165, 120), 20, 1800))
        alarm_rows.append(
            {
                "timestamp": source["timestamp"],
                "machineId": MACHINE_ID,
                "alarmCode": catalog_entry["alarmCode"],
                "alarmType": catalog_entry["alarmType"],
                "severity": catalog_entry["severity"],
                "description": catalog_entry["description"],
                "durationSec": duration,
                "status": random.choice(["ACTIVE", "ACKNOWLEDGED", "CLEARED"]),
                "operatorAction": catalog_entry["operatorAction"],
            }
        )

    return alarm_rows


def build_quality_inspections(context: TelemetryContext, operators: list[dict[str, Any]]) -> list[dict[str, Any]]:
    inspections: list[dict[str, Any]] = []
    inspector_ids = [op["operatorId"] for op in operators]
    completion_pool = context.completion_events or [
        {
            "timestamp": row["timestamp"],
            "payload": {
                "jobId": row["jobId"],
                "partNumber": row["partNumber"],
            },
        }
        for row in context.rows if row["status"] == "RUNNING"
    ]
    chosen_pool = completion_pool[:QUALITY_INSPECTION_RECORDS]
    if len(chosen_pool) < QUALITY_INSPECTION_RECORDS:
        chosen_pool = [random.choice(completion_pool) for _ in range(QUALITY_INSPECTION_RECORDS)]

    jobs = {job.job_id: job for job in build_job_catalog()}
    for idx in range(QUALITY_INSPECTION_RECORDS):
        source = chosen_pool[idx]
        payload = source.get("payload", {})
        job = jobs[payload["jobId"]]
        dim_name, nominal, tol_plus, tol_minus = random.choice(job.dimensions)
        rejection_bias = 0.0
        if source.get("payload", {}).get("result") == "REJECT":
            rejection_bias = random.uniform(0.012, 0.042)
        measured = nominal + random.gauss(0, abs(tol_plus + tol_minus) / 4.0) + rejection_bias
        result = "PASS" if (nominal + tol_minus) <= measured <= (nominal + tol_plus) else "FAIL"
        surface_ra = round(clamp(random.gauss(0.9 if job.material.startswith("Aluminum") else 1.4, 0.28), 0.35, 3.2), 3)
        inspections.append(
            {
                "timestamp": source["timestamp"],
                "machineId": MACHINE_ID,
                "partNumber": payload["partNumber"],
                "jobId": payload["jobId"],
                "dimensionName": dim_name,
                "nominalValueMm": round(nominal, 3),
                "measuredValueMm": round(measured, 3),
                "tolerancePlusMm": round(tol_plus, 3),
                "toleranceMinusMm": round(tol_minus, 3),
                "result": result,
                "surfaceRaUm": surface_ra,
                "inspectorId": random.choice(inspector_ids),
            }
        )

    return inspections


def build_energy_cycles(context: TelemetryContext) -> list[dict[str, Any]]:
    cycles: list[dict[str, Any]] = []
    completion_pool = context.completion_events or []
    if not completion_pool:
        raise RuntimeError("Completion events are required to generate energy cycles.")

    for idx in range(ENERGY_CYCLE_RECORDS):
        source = completion_pool[idx % len(completion_pool)]
        payload = source["payload"]
        job = next(job for job in build_job_catalog() if job.job_id == payload["jobId"])
        cycle_time = int(payload["cycleTimeSec"])
        spindle_avg = round(clamp(random.gauss(job.rpm_nominal, job.rpm_nominal * 0.06), 1200, 9000), 1)
        feed_avg = round(clamp(random.gauss(job.feed_nominal, job.feed_nominal * 0.08), 60, 2800), 1)
        running_kw = round(clamp((spindle_avg / 9000.0) * 32 + (feed_avg / 2800.0) * 7 + random.uniform(1.0, 5.0), 1.2, 42), 3)
        running_energy = round((cycle_time / 3600.0) * running_kw * random.uniform(0.90, 1.08), 4)
        idle_energy = round(running_energy * random.uniform(0.08, 0.18), 4)
        peak_power = round(running_kw * random.uniform(1.04, 1.28), 2)
        cycles.append(
            {
                "timestamp": source["timestamp"],
                "machineId": MACHINE_ID,
                "jobId": payload["jobId"],
                "partNumber": payload["partNumber"],
                "cycleNo": idx + 1,
                "cycleTimeSec": cycle_time,
                "spindleRpmAvg": spindle_avg,
                "feedRateAvg": feed_avg,
                "energyKWh": round(running_energy + idle_energy, 4),
                "peakPowerKW": peak_power,
                "idleEnergyKWh": idle_energy,
                "runningEnergyKWh": running_energy,
            }
        )

    return cycles


def build_operator_shift_logs(context: TelemetryContext, operators: list[dict[str, Any]]) -> list[dict[str, Any]]:
    logs: list[dict[str, Any]] = []
    roster_cycle = operators * ((len(context.shift_metrics) // len(operators)) + 1)
    for idx, shift in enumerate(context.shift_metrics):
        operator = roster_cycle[idx]
        logs.append(
            {
                "date": shift["date"],
                "shift": shift["shift"],
                "operatorId": operator["operatorId"],
                "operatorName": operator["operatorName"],
                "machineId": MACHINE_ID,
                "loginTime": f"{shift['date'].isoformat()}T{('06:00:00' if shift['shift'] == 'A' else '14:00:00' if shift['shift'] == 'B' else '22:00:00')}",
                "logoutTime": f"{(shift['date'] + timedelta(days=1) if shift['shift'] == 'C' else shift['date']).isoformat()}T{('06:00:00' if shift['shift'] == 'C' else '14:00:00' if shift['shift'] == 'A' else '22:00:00')}",
                "totalRuntimeMin": shift["runtimeMin"],
                "totalDowntimeMin": shift["downtimeMin"],
                "partsProduced": shift["totalCount"],
                "rejects": shift["rejectCount"],
                "notes": random.choice([
                    "Shift executed within plan with minor setup adjustment.",
                    "Observed stable spindle behavior and normal chip flow.",
                    "Changed inserts after wear trend crossed threshold.",
                    "Reviewed alarm recovery procedure during planned stop.",
                    "Verified coolant concentration and hydraulic pressure.",
                ]),
            }
        )
    return logs


def build_kafka_events(context: TelemetryContext, alarms: list[dict[str, Any]], inspections: list[dict[str, Any]], tool_snapshots: list[dict[str, Any]]) -> list[dict[str, Any]]:
    events: list[dict[str, Any]] = []

    # Telemetry is sampled down for Kafka so the stream stays useful but not overwhelming.
    for idx, row in enumerate(context.rows[:: max(1, len(context.rows) // (KAFKA_EVENT_TARGET // 4))]):
        events.append(
            {
                "eventId": f"EV-KAFKA-{len(events) + 1:07d}",
                "eventType": "TELEMETRY_RECORDED",
                "timestamp": row["timestamp"],
                "machineId": MACHINE_ID,
                "payload": {
                    "jobId": row["jobId"],
                    "partNumber": row["partNumber"],
                    "status": row["status"],
                    "spindleRpm": row["spindleRpm"],
                    "feedRate": row["feedRate"],
                    "tool": row["tool"],
                    "toolLifePct": row["toolLifePct"],
                    "partCount": row["partCount"],
                },
            }
        )

    events.extend(context.status_events)

    for completion in context.completion_events:
        payload = completion["payload"]
        events.append(
            {
                "eventId": f"EV-KAFKA-{len(events) + 1:07d}",
                "eventType": "PART_COMPLETED",
                "timestamp": completion["timestamp"],
                "machineId": MACHINE_ID,
                "payload": payload,
            }
        )

    for alarm in alarms:
        events.append(
            {
                "eventId": f"EV-KAFKA-{len(events) + 1:07d}",
                "eventType": "ALARM_RAISED",
                "timestamp": alarm["timestamp"],
                "machineId": MACHINE_ID,
                "payload": {
                    "alarmCode": alarm["alarmCode"],
                    "severity": alarm["severity"],
                    "description": alarm["description"],
                    "status": alarm["status"],
                },
            }
        )

    for snapshot in tool_snapshots:
        events.append(
            {
                "eventId": f"EV-KAFKA-{len(events) + 1:07d}",
                "eventType": "TOOL_WEAR_UPDATED",
                "timestamp": snapshot["timestamp"],
                "machineId": MACHINE_ID,
                "payload": {
                    "tool": snapshot["tool"],
                    "toolLifePct": snapshot["toolLifePct"],
                    "wearLevel": snapshot["wearLevel"],
                    "predictedRemainingLifeMin": snapshot["predictedRemainingLifeMin"],
                },
            }
        )

    for inspection in inspections:
        events.append(
            {
                "eventId": f"EV-KAFKA-{len(events) + 1:07d}",
                "eventType": "QUALITY_INSPECTION_COMPLETED",
                "timestamp": inspection["timestamp"],
                "machineId": MACHINE_ID,
                "payload": {
                    "partNumber": inspection["partNumber"],
                    "jobId": inspection["jobId"],
                    "dimensionName": inspection["dimensionName"],
                    "result": inspection["result"],
                    "measuredValueMm": inspection["measuredValueMm"],
                },
            }
        )

    # Sort by event time and trim/extend to the requested sample size.
    events.sort(key=lambda item: item["timestamp"])
    return events


def build_mongodb_snapshot(
    context: TelemetryContext,
    alarms: list[dict[str, Any]],
    oee_rows: list[dict[str, Any]],
    tools: list[dict[str, Any]],
    inspections: list[dict[str, Any]],
    energy_cycles: list[dict[str, Any]],
    operators: list[dict[str, Any]],
) -> dict[str, list[dict[str, Any]]]:
    # Keep the snapshot readable while still realistic.
    machine_docs = build_machine_catalog()

    def convert(doc: dict[str, Any]) -> dict[str, Any]:
        return json.loads(json.dumps(doc, default=serialize_json_value))

    telemetry_docs = []
    for idx, row in enumerate(context.rows[:500]):
        telemetry_doc = dict(row)
        telemetry_doc["_id"] = make_object_id("TEL", idx + 1)
        telemetry_doc["machineId"] = MACHINE_ID
        telemetry_docs.append(convert(telemetry_doc))

    alarm_docs = []
    for idx, row in enumerate(alarms[:120]):
        doc = dict(row)
        doc["_id"] = make_object_id("ALM", idx + 1)
        alarm_docs.append(convert(doc))

    oee_docs = []
    for idx, row in enumerate(oee_rows):
        doc = dict(row)
        doc["_id"] = make_object_id("OEE", idx + 1)
        oee_docs.append(convert(doc))

    tool_docs = []
    for idx, row in enumerate(tools):
        doc = dict(row)
        doc["_id"] = make_object_id("TOL", idx + 1)
        doc["machineId"] = MACHINE_ID
        tool_docs.append(convert(doc))

    inspection_docs = []
    for idx, row in enumerate(inspections[:300]):
        doc = dict(row)
        doc["_id"] = make_object_id("QIN", idx + 1)
        inspection_docs.append(convert(doc))

    energy_docs = []
    for idx, row in enumerate(energy_cycles[:300]):
        doc = dict(row)
        doc["_id"] = make_object_id("ENC", idx + 1)
        energy_docs.append(convert(doc))

    operator_docs = []
    for idx, row in enumerate(operators):
        doc = dict(row)
        doc["machineId"] = MACHINE_ID
        operator_docs.append(convert(doc))

    return {
        "machines": [convert(machine_doc) for machine_doc in machine_docs],
        "productionTelemetry": telemetry_docs,
        "alarms": alarm_docs,
        "oeeReports": oee_docs,
        "tools": tool_docs,
        "qualityInspections": inspection_docs,
        "energyCycles": energy_docs,
        "operators": operator_docs,
}


def build_mongodb_machine_model(machine_doc: dict[str, Any]) -> dict[str, Any]:
    return {
        "collection": "machines",
        "description": "MongoDB seed model for the Hurco M10 machine document.",
        "primaryKey": "machineId",
        "indexes": [
            {"keys": {"machineId": 1}, "options": {"unique": True}},
            {"keys": {"status": 1}, "options": {}},
            {"keys": {"updatedAt": -1}, "options": {}},
        ],
        "relationships": {
            "telemetryCollection": "productionTelemetry",
            "alarmCollection": "alarms",
            "oeeCollection": "oeeReports",
            "toolWearCollection": "tools",
            "qualityCollection": "qualityInspections",
            "energyCollection": "energyCycles",
            "operatorCollection": "operators",
        },
        "document": machine_doc,
        "schema": {
            "_id": "string",
            "machineId": "string",
            "name": "string",
            "model": "string",
            "manufacturer": "string",
            "controller": "string",
            "status": "string",
            "location": "string",
            "spindleMaxRpm": "int",
            "feedMaxMmMin": "int",
            "axes": "array[string]",
            "rotaryAxesAvailable": "array[string]",
            "createdAt": "datetime",
            "updatedAt": "datetime",
        },
    }


def serialize_json_value(value: Any) -> Any:
    if isinstance(value, (datetime, date)):
        return iso(value)
    return value


def write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    if not rows:
        raise ValueError(f"No rows available for {path.name}")
    df = pd.DataFrame(rows)
    df.to_csv(path, index=False, quoting=csv.QUOTE_MINIMAL)


def write_json(path: Path, data: Any) -> None:
    with path.open("w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False, default=serialize_json_value)
        handle.write("\n")


def write_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    with path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False, default=serialize_json_value))
            handle.write("\n")


def write_excel(path: Path, sheets: dict[str, list[dict[str, Any]]]) -> None:
    with pd.ExcelWriter(path, engine="openpyxl") as writer:
        for sheet_name, rows in sheets.items():
            pd.DataFrame(rows).to_excel(writer, sheet_name=sheet_name[:31], index=False)


def build_readme() -> str:
    return """# Hurco M10 Industrial Sample Data Package

This package contains synthetic but realistic sample data for a Hurco M10 CNC machining center.

## Files

- `production_telemetry.csv` - high-volume milling telemetry for machine states, spindle load, feed rate, axes, tool changes, temperature, vibration, and part counts.
- `alarm_history.csv` - alarm log with severity, duration, and operator recovery actions.
- `oee_shift_summary.csv` - calculated OEE by shift for planning and performance analysis.
- `tool_wear_predictive.csv` - predictive maintenance data for end mills, face mills, drills, taps, chamfer mills, and ball nose tools.
- `quality_inspection.csv` - dimensional quality checks and surface finish inspections.
- `energy_cycle.csv` - per-cycle energy consumption data for power analysis.
- `operator_shift_logs.csv` - operator attendance, runtime, downtime, and shift notes.
- `kafka_events.jsonl` - event stream suitable for Kafka ingestion.
- `mongodb_collections.json` - MongoDB-style sample collections for documents and seed data.
- `mongodb_machine_model.json` - MongoDB seed model for the Hurco M10 machine document.
- `seed_mongodb.py` - Python helper that imports the seed JSON into MongoDB.
- `Hurco_M10_Industrial_Sample_Datasets.xlsx` - workbook with one sheet per dataset.

## How to use this data

### Spring Boot

Use the CSV files as seed inputs for reactive imports with `WebFlux`, `ReactiveMongoRepository`, and Apache Camel file routes.
Typical targets:

- load `production_telemetry.csv` into a `productionTelemetry` collection
- map `alarm_history.csv` into an alarms collection
- use `oee_shift_summary.csv` for dashboard APIs

### MongoDB

The `mongodb_collections.json` file is shaped like application seed documents. The `mongodb_machine_model.json` file is a direct seed model for the `machines` collection. They are useful for:

- local seed scripts
- integration tests
- sample dashboards
- validating indexes on `machineId`, `timestamp`, `jobId`, and `shift`

To import the seed data into MongoDB, run:

```bash
python seed_mongodb.py
```

You can override the target database with `MONGO_DATABASE`, and the connection string with `MONGO_URI`.

### Apache Camel

Camel can ingest these files from a local directory or ZIP extraction target:

- route CSV files to MongoDB
- publish JSONL events to Kafka
- transform and enrich telemetry records before persistence

### Kafka

`kafka_events.jsonl` is ready for line-by-line event streaming. A common pattern is:

- use `machineId` as the message key
- publish `TELEMETRY_RECORDED`, `ALARM_RAISED`, and `PART_COMPLETED` events
- route the stream into a metrics pipeline or alerting service

### React dashboards

The data is suitable for:

- live charts for spindle RPM, feed rate, spindle load, servo load, temperature, vibration, coolant, lubrication, and air pressure
- OEE cards and shift comparisons
- alarm timelines
- tool wear prediction widgets
- quality control tables and pass/fail summaries

## Notes

- The machine ID is fixed to `HURCO-M10-01`.
- The telemetry spans seven operational days starting at the configured start date in the generator script.
- The generator is deterministic when the seed constant is unchanged.
"""


def build_mongodb_seed_script() -> str:
    return """#!/usr/bin/env python3
\"\"\"Seed helper for the Hurco M10 MongoDB sample data package.

Run:
    python seed_mongodb.py
\"\"\"

from __future__ import annotations

import json
import os
from datetime import date, datetime
from pathlib import Path

from pymongo import MongoClient

SOURCE_DIR = Path(__file__).resolve().parent
MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
MONGO_DATABASE = os.getenv("MONGO_DATABASE", "manufacturing")
MACHINE_COLLECTION = "machines"

TEMPORAL_FIELDS = {
    "timestamp",
    "createdAt",
    "updatedAt",
    "loginTime",
    "logoutTime",
    "lastServiceAt",
    "nextServiceDueAt",
    "date",
}


def parse_temporal(value: str) -> datetime:
    try:
        return datetime.fromisoformat(value)
    except ValueError:
        return datetime.combine(date.fromisoformat(value), datetime.min.time())


def convert_value(field_name: str, value):
    if isinstance(value, dict):
        return {key: convert_value(key, item) for key, item in value.items()}
    if isinstance(value, list):
        return [convert_value(field_name, item) for item in value]
    if isinstance(value, str) and field_name in TEMPORAL_FIELDS:
        return parse_temporal(value)
    return value


def load_json(filename: str):
    with (SOURCE_DIR / filename).open("r", encoding="utf-8") as handle:
        return json.load(handle)


def main() -> None:
    client = MongoClient(MONGO_URI)
    db = client[MONGO_DATABASE]

    machine_model = load_json("mongodb_machine_model.json")
    collections = load_json("mongodb_collections.json")

    machine_docs = [machine_model["document"]]
    machine_docs.extend(machine_model.get("variants", []))
    machine_ids = [doc["machineId"] for doc in machine_docs if doc.get("machineId")]

    db[MACHINE_COLLECTION].delete_many({"machineId": {"$in": machine_ids}})
    db[MACHINE_COLLECTION].insert_many([convert_value("machines", doc) for doc in machine_docs])

    for collection_name, documents in collections.items():
        if collection_name == MACHINE_COLLECTION or not documents:
            continue

        ids = sorted({doc.get("machineId") for doc in documents if doc.get("machineId")})
        if ids:
            db[collection_name].delete_many({"machineId": {"$in": ids}})
        else:
            db[collection_name].delete_many({})

        db[collection_name].insert_many([convert_value(collection_name, doc) for doc in documents])

    print(
        f"Seeded {len(machine_docs)} machine documents and "
        f"{sum(len(v) for k, v in collections.items() if k != MACHINE_COLLECTION)} records into {MONGO_DATABASE}"
    )


if __name__ == "__main__":
    main()
"""


def create_zip(output_dir: Path, files: list[Path]) -> Path:
    zip_path = output_dir / ZIP_NAME
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for file_path in files:
            archive.write(file_path, arcname=file_path.name)
    return zip_path


def main() -> None:
    base_dir = Path(__file__).resolve().parent
    output_dir = ensure_output_dir(base_dir)

    # Generate the primary telemetry first, then derive the rest of the package from it.
    context = build_telemetry()
    operators = build_operator_roster()
    alarms = build_alarm_history(context)
    quality_inspections = build_quality_inspections(context, operators)
    energy_cycles = build_energy_cycles(context)
    tools = [
        {
            "tool": tool["tool"],
            "toolType": tool["toolType"],
            "machineId": MACHINE_ID,
            "material": random.choice([job.material for job in build_job_catalog()]),
            "toolLifePct": (life := round(random.uniform(52, 99), 1)),
            "currentWearLevel": tool_wear_band(life)[0],
            "recommendedAction": tool_wear_band(life)[1],
            "lastServiceAt": iso(START_DATE + timedelta(days=random.randint(0, 6), hours=random.randint(0, 8))),
            "nextServiceDueAt": iso(START_DATE + timedelta(days=random.randint(1, 9), hours=random.randint(0, 8))),
        }
        for tool in build_tool_catalog()
    ]
    oee_rows = context.shift_metrics
    operator_logs = build_operator_shift_logs(context, operators)
    kafka_events = build_kafka_events(context, alarms, quality_inspections, context.tool_snapshots)
    mongodb_collections = build_mongodb_snapshot(
        context,
        alarms,
        oee_rows,
        tools,
        quality_inspections,
        energy_cycles,
        operators,
    )
    mongodb_machine_model = build_mongodb_machine_model(mongodb_collections["machines"][0])

    # Materialize the tabular datasets.
    write_csv(output_dir / "production_telemetry.csv", context.rows)
    write_csv(output_dir / "alarm_history.csv", alarms)
    write_csv(output_dir / "oee_shift_summary.csv", oee_rows)
    write_csv(output_dir / "tool_wear_predictive.csv", context.tool_snapshots)
    write_csv(output_dir / "quality_inspection.csv", quality_inspections)
    write_csv(output_dir / "energy_cycle.csv", energy_cycles)
    write_csv(output_dir / "operator_shift_logs.csv", operator_logs)

    write_jsonl(output_dir / "kafka_events.jsonl", kafka_events)
    write_json(output_dir / "mongodb_collections.json", mongodb_collections)
    write_json(output_dir / "mongodb_machine_model.json", mongodb_machine_model)
    (output_dir / "seed_mongodb.py").write_text(build_mongodb_seed_script(), encoding="utf-8")
    (output_dir / "seed_mongodb.py").chmod(0o755)

    # Build the multi-sheet workbook.
    write_excel(
        output_dir / EXCEL_NAME,
        {
            "production_telemetry": context.rows,
            "alarm_history": alarms,
            "oee_shift_summary": oee_rows,
            "tool_wear_predictive": context.tool_snapshots,
            "quality_inspection": quality_inspections,
            "energy_cycle": energy_cycles,
            "operator_shift_logs": operator_logs,
        "kafka_events": kafka_events,
        "mongodb_preview": [
            {"collection": name, "documents": len(items)}
            for name, items in mongodb_collections.items()
        ],
    },
    )

    # Create the package README.
    readme_path = output_dir / "README.md"
    readme_path.write_text(build_readme(), encoding="utf-8")

    # ZIP only the requested data files.
    zip_files = [
        output_dir / "production_telemetry.csv",
        output_dir / "alarm_history.csv",
        output_dir / "oee_shift_summary.csv",
        output_dir / "tool_wear_predictive.csv",
        output_dir / "quality_inspection.csv",
        output_dir / "energy_cycle.csv",
        output_dir / "operator_shift_logs.csv",
        output_dir / "kafka_events.jsonl",
        output_dir / "mongodb_collections.json",
        output_dir / "mongodb_machine_model.json",
        output_dir / "seed_mongodb.py",
        output_dir / EXCEL_NAME,
    ]
    create_zip(output_dir, zip_files)

    print(f"Generated {len(context.rows)} telemetry rows in {output_dir}")
    print(f"Package written to: {output_dir}")


if __name__ == "__main__":
    main()
