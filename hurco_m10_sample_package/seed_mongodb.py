#!/usr/bin/env python3
"""Seed helper for the Hurco M10 MongoDB sample data package.

Run:
    python seed_mongodb.py
"""

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
