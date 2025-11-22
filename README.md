# Intuit Build Challenge 2025  
## Producer-Consumer (Python) + Sales Analysis (Java)

**Project Readmes**
- Python Producer-Consumer: [producer_consumer/README.md](producer_consumer/README.md)
- Java Sales Analysis: [data-analysis/README.md](data-analysis/README.md)

---

## Summary Table
| Section | Description |
|--------|-------------|
| [Overview](#overview) | High-level description of both modules |
| [Key Features](#key-features) | Engineering highlights |
| [How to Run](#how-to-run) | Docker and CLI options |
| [CI & Tests](#ci--test-verification) | Unified pipeline |
| [Sample Outputs](#sample-outputs) | Where results appear |
| [Producer-Consumer Scenarios](#producer-consumer-behavior-scenarios) | Example runs |
| [Repository Structure](#repository-structure) | Directory layout |
| [Conclusion](#conclusion) | Final notes |

---

## Overview
This repository implements two independent modules:
- **Producer-Consumer (Python):** Threaded producer/consumer pipeline using blocking queues, explicit wait/notify, and sentinel-based shutdown.
- **Sales Analysis (Java):** Stream-based financial analytics (totals, averages, trends, MoM growth) with Prosperity Insights reporting.

Why these languages:
- **Python** for Producer-Consumer: clean threading primitives, straightforward wait/notify, fast debugging cycles, and expressive logging—ideal for demonstrating synchronization and blocking behavior.
- **Java** for Data Analysis: Stream API enables functional, type-safe filtering, grouping, and statistical calculations with strong testability.

---

## Key Features
**Producer-Consumer (Python)**
- Interchangeable buffers: `BlockingQueue` and `Condition` (wait/notify).
- Accurate blocking semantics (producer waits when full, consumer waits when empty).
- Configurable concurrency (producers, consumers, items, capacity, delays).
- Thread-safe FIFO with clear logging; sentinel-based shutdown.

**Sales Analysis (Java)**
- Functional analytics with Java Streams (`map`, `filter`, `reduce`, `groupingBy`).
- Computes totals, regional averages, MoM trends, top performers, refunds, reconciliation.
- Robust CSV ingestion with error handling; immutable domain model.
- Human-readable Prosperity Insights reporting.

**Testing & Quality**
- Python: `pytest` suite covering concurrency, blocking, ordering, shutdown.
- Java: test harness covering analytics logic and CSV parsing.
- All tests run in CI.

**CI/CD & DevOps**
- GitHub Actions workflow for Python and Java (see `.github/workflows/ci.yml`).
- Docker Compose for reproducible multi-service execution.

---

## How to Run

### Using Docker Compose (recommended)
- Build all services:
  ```bash
  docker compose build
  ```
- Run everything (stop with Ctrl+C):
  ```bash
  docker compose up
  ```

### Producer-Consumer (Python)
- Default:
  ```bash
  docker compose run --rm producer_consumer
  ```
- Custom example:
  ```bash
  docker compose run --rm producer_consumer --buffer condition --producers 2 --consumers 3 --items 10 --capacity 4
  ```
- More options: see [producer_consumer/README.md](producer_consumer/README.md).

### Sales Analysis (Java)
- Default (auto-generates sample data if missing):
  ```bash
  docker compose run --rm data_analysis
  ```
- More options: see [data-analysis/README.md](data-analysis/README.md).

---

## CI & Test Verification
- GitHub Actions (Python + Java):  
  ![CI Pipeline](https://github.com/slimshady-9/build-challenge-intuit/blob/main/github%20actions%20tests.png)
- Python tests (pytest):  
  ![Python Tests](https://github.com/slimshady-9/build-challenge-intuit/blob/main/producer_consumer/results_screenshots/Testing%20Results.png)
- Java tests (JUnit):  
  ![Java Tests](https://github.com/slimshady-9/build-challenge-intuit/blob/main/data-analysis/results_screenshots/Test%20Results.png)

---

## Sample Outputs
- Results for both apps are printed to the console.
- Prosperity Insights (Java):  
  ![Prosperity Insights](https://github.com/slimshady-9/build-challenge-intuit/blob/main/data-analysis/results_screenshots/Intuit%20Prosperity%20Insights.png)
- Sales Analysis report (Java):  
  ![Sales Analysis](https://github.com/slimshady-9/build-challenge-intuit/blob/main/data-analysis/results_screenshots/SalesAnalyzer.png)
- Producer-Consumer clean run (Python):  
  ![Producer Consumer Clean](https://github.com/slimshady-9/build-challenge-intuit/blob/main/producer_consumer/results_screenshots/Clean%20(no%20blocking).png)

---

## Producer-Consumer Behavior Scenarios
- Clean run (no blocking):
  ```bash
  python -m src.main --buffer condition --producers 1 --consumers 1 --items 6 --capacity 6
  ```
- Producer waiting (buffer full):
  ```bash
  python -m src.main --buffer condition --producers 5 --consumers 1 --items 6 --capacity 2
  ```
- Consumer waiting (buffer empty):
  ```bash
  python -m src.main --buffer condition --producers 1 --consumers 3 --items 6 --capacity 2
  ```

---

## Repository Structure
```
build-challenge-intuit/
├── producer_consumer/
│   ├── src/
│   ├── tests/
│   └── README.md
├── data-analysis/
│   ├── src/
│   ├── tests/
│   └── README.md
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
```

---

## Conclusion
A multi-language engineering solution with robust concurrency modeling, functional analytics, reproducible builds, and automated testing—ready for review.
