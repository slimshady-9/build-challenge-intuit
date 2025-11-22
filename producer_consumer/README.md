# Producer-Consumer Challenge

Python demo of the classic producer-consumer pattern using threads, blocking queues, and explicit wait/notify with `threading.Condition`.

## Table of Contents

- [How to Run](#how-to-run)
  - [Prerequisites](#prerequisites)
  - [Docker (recommended)](#docker-recommended)
  - [Basic Usage](#basic-usage)
  - [Command Line Arguments](#command-line-arguments)
  - [Example Commands](#example-commands)
- [Assumptions](#assumptions)
- [Challenges](#challenges)
- [Logging](#logging)
  - [Log Format](#log-format)
  - [Key Log Messages](#key-log-messages)
  - [Log Level Control](#log-level-control)
- [Basic Workflow](#basic-workflow)
  - [Step-by-Step Execution Flow](#step-by-step-execution-flow)
- [Results and Explanation](#results-and-explanation)
  - [Example 1: Balanced Configuration](#example-1-balanced-configuration)
  - [Example 2: Multiple Producers, Single Consumer, Limited Capacity](#example-2-multiple-producers-single-consumer-limited-capacity)
  - [Example 3: Single Producer, Multiple Consumers, Limited Capacity](#example-3-single-producer-multiple-consumers-limited-capacity)
  - [Key Observations from Results](#key-observations-from-results)
- [Design Sketch](#design-sketch)
- [Testing](#testing)
  - [Running Tests](#running-tests)
  - [Test Suite Overview](#test-suite-overview)
  - [Test Results Summary](#test-results-summary)
  - [Test Organization](#test-organization)
  - [Running Specific Tests](#running-specific-tests)

## How to Run

### Prerequisites
- **Docker** (recommended to skip local Python setup), or
- **Python 3.11+** with `pip` if running locally without containers

### Docker (recommended)
- Build (from repo root):
  ```bash
  docker compose build
  ```
- Run with defaults:
  ```bash
  docker compose run --rm producer_consumer
  ```
- Run with custom args:
  ```bash
  docker compose run --rm producer_consumer --buffer condition --producers 2 --consumers 3 --items 10 --capacity 4
  ```
- Run both services together (stop with Ctrl+C):
  ```bash
  docker compose up
  ```

### Basic Usage

If you prefer running locally:

1. Navigate to the project directory:
   ```bash
   cd producer_consumer
   ```

2. Install dependencies:
   ```bash
   python -m pip install -r requirements.txt
   ```

**Default run (queue buffer):**
```bash
python -m src.main
```

**Using condition buffer:**
```bash
python -m src.main --buffer condition
```

**Full command with all options:**
```bash
python -m src.main --buffer <queue|condition> --producers <N> --consumers <M> --items <K> --capacity <C> --log-level <LEVEL>
```

### Command Line Arguments

- `--buffer`: Buffer implementation type (`queue` or `condition`). Default: `queue`
- `--producers`: Number of producer threads. Default: `1`
- `--consumers`: Number of consumer threads. Default: `1`
- `--items`: Number of items to produce (sequential integers starting from 1). Default: `5`
- `--capacity`: Maximum capacity of the shared buffer. Default: `2`
- `--log-level`: Logging verbosity (`DEBUG`, `INFO`, `WARNING`, `ERROR`, `CRITICAL`). Default: `INFO`

### Example Commands

```bash
# Single producer, single consumer, balanced capacity
python -m src.main --buffer condition --producers 1 --consumers 1 --items 6 --capacity 6

# Multiple producers, single consumer, limited capacity
python -m src.main --buffer condition --producers 5 --consumers 1 --items 6 --capacity 2

# Single producer, multiple consumers, limited capacity
python -m src.main --buffer condition --producers 1 --consumers 3 --items 6 --capacity 2
```

## Assumptions

1. **Item Integrity**: All items produced will be consumed exactly once. No items are lost or duplicated.

2. **Sentinel-Based Termination**: The system uses a sentinel value (`<SENTINEL>`) to signal completion. Each consumer thread requires exactly one sentinel to terminate gracefully.

3. **Thread Safety**: The shared buffer implementations (`ConditionBuffer` and `BlockingQueueBuffer`) handle all synchronization internally, ensuring thread-safe operations.

4. **Sequential Item Production**: Items are produced as sequential integers starting from 1 (e.g., 1, 2, 3, ..., N).

5. **No Delays by Default**: Producer and consumer delays are set to 0.0 by default, meaning operations happen as fast as possible (useful for demonstrating race conditions and synchronization).

6. **Round-Robin Producer Distribution**: When multiple producers are used, items are distributed using round-robin slicing (e.g., with 3 producers and 6 items: Producer 1 gets [1, 4], Producer 2 gets [2, 5], Producer 3 gets [3, 6]).

7. **FIFO Ordering**: Items are consumed in First-In-First-Out order within the buffer, though with multiple producers/consumers, the interleaving may vary.

## Challenges

1. **Race Conditions**: Multiple producers and consumers accessing a shared buffer simultaneously can lead to race conditions. The implementation uses proper synchronization primitives (`threading.Condition` or `queue.Queue`) to prevent data corruption.

2. **Buffer Overflow**: Producers must wait when the buffer is full. The condition buffer implementation logs when producers are blocked waiting for space.

3. **Buffer Underflow**: Consumers must wait when the buffer is empty. The condition buffer logs when consumers are waiting for items.

4. **Graceful Shutdown**: Ensuring all consumers terminate correctly when production is complete. This is handled by sending one sentinel per consumer thread.

5. **Thread Coordination**: With multiple producers and consumers, coordinating the start and stop of threads requires careful sequencing (consumers start first, then producers; producers join, then sentinels are sent, then consumers join).

6. **Order Preservation**: While FIFO is maintained within the buffer, with multiple producers producing concurrently, the exact order items enter the buffer may differ from their production order, especially with limited buffer capacity.

## Logging

The application uses Python's `logging` module with a custom format that includes:
- **Timestamp**: When the log entry was created
- **Thread Name**: Which thread generated the log (e.g., `MainThread`, `Producer`, `Consumer`)
- **Log Level**: Severity of the log message (`INFO`, `DEBUG`, etc.)
- **Message**: The actual log content

### Log Format
```
%(asctime)s [%(threadName)s] %(levelname)s: %(message)s
```

### Key Log Messages

- **Producer logs**:
  - `Producing <item>`: Producer is about to produce an item
  - `Producer added: <item> (size=X/Y)`: Item successfully added to buffer with current size
  - `⛔ Buffer FULL — producer WAITING (size=X/Y)`: Producer blocked waiting for buffer space

- **Consumer logs**:
  - `Consumer got: <item> (size=X/Y)`: Item retrieved from buffer with current size
  - `Consuming <item>`: Consumer is processing the item
  - `Consumer received sentinel; stopping`: Consumer received termination signal

- **Main thread logs**:
  - `Starting demo with X items using <buffer> buffer (capacity=Y)`: Initialization message
  - `All threads completed. Destination collected: [items]`: Final results

### Log Level Control

Use `--log-level` to control verbosity:
- `DEBUG`: Most verbose, includes buffer empty/full waiting states
- `INFO`: Standard level, shows production/consumption and buffer state changes
- `WARNING`, `ERROR`, `CRITICAL`: For errors and warnings only

## Basic Workflow

```
┌─────────────────┐
│ SourceContainer │ (Holds items to be produced: [1, 2, 3, ..., N])
└────────┬────────┘
         │
         │ Items distributed to producers
         ▼
┌─────────────────┐
│ Producer Thread │ ──┐                      ┌────────────────────┐ 
│ Producer Thread │ ──┼──► [Shared Buffer] ──┼──► Consumer Thread │
│ Producer Thread │ ──┘                      │   Consumer Thread ─┼──► DestinationContainer
└─────────────────┘                          │   Consumer Thread  │   (Collects consumed items)
                                             └────────────────────┘ 
                                              └──► [Capacity Limit]
```

### Step-by-Step Execution Flow

1. **Initialization**:
   - Create source container with items [1, 2, ..., N]
   - Create empty destination container
   - Create shared buffer with specified capacity
   - Configure logging

2. **Thread Creation**:
   - Create consumer threads (each waits for items from buffer)
   - Create producer threads (each gets a chunk of items to produce)
   - Start all consumer threads first
   - Start all producer threads

3. **Production Phase**:
   - Each producer thread produces its assigned items sequentially
   - Items are added to the shared buffer (blocking if buffer is full)
   - Multiple producers may produce concurrently

4. **Consumption Phase**:
   - Consumer threads continuously retrieve items from buffer (blocking if buffer is empty)
   - Each consumed item is added to the destination container
   - Consumers run concurrently with producers

5. **Termination**:
   - Wait for all producer threads to complete
   - Send one sentinel value per consumer thread to signal completion
   - Wait for all consumer threads to finish
   - Collect and display final results from destination container

## Results and Explanation

> **Note**: Results screenshots for the examples below are saved in the [`results_screenshots`](./results_screenshots) folder.

### Example 1: Balanced Configuration
**Command:**
```bash
python -m src.main --buffer condition --producers 1 --consumers 1 --items 6 --capacity 6
```

**Result:**
```
Consumed items: [1, 2, 3, 4, 5, 6]
```

**Explanation:**
- **Single producer, single consumer**: No concurrency conflicts
- **Capacity equals items**: Buffer never fills up, no waiting occurs
- **Perfect interleaving**: Each item is produced and immediately consumed
- **Order preserved**: Items consumed in exact production order (1→2→3→4→5→6)
- **No blocking**: Neither producer nor consumer ever waits, as buffer size matches demand

The logs show a perfect ping-pong pattern: produce → consume → produce → consume, with buffer size alternating between 1 and 0.

---

### Example 2: Multiple Producers, Single Consumer, Limited Capacity
**Command:**
```bash
python -m src.main --buffer condition --producers 5 --consumers 1 --items 6 --capacity 2
```

**Result:**
```
Consumed items: [1, 2, 6, 4, 3, 5]
```

**Explanation:**
- **5 producers, 1 consumer**: High production rate vs. single consumption rate
- **Limited capacity (2)**: Buffer fills quickly, causing producer blocking
- **Order variation**: Items consumed as `[1, 2, 6, 4, 3, 5]` instead of `[1, 2, 3, 4, 5, 6]`
- **Why this order?**:
  - Producer 1 produces 1 → buffer: [1]
  - Producer 2 produces 2 → buffer: [1, 2] (FULL)
  - Consumer consumes 1 → buffer: [2]
  - Producer 6 produces 6 → buffer: [2, 6] (FULL)
  - Consumer consumes 2 → buffer: [6]
  - Producer 4 produces 4 → buffer: [6, 4] (FULL)
  - Consumer consumes 6 → buffer: [4]
  - Producer 3 produces 3 → buffer: [4, 3] (FULL)
  - Consumer consumes 4 → buffer: [3]
  - Producer 5 produces 5 → buffer: [3, 5]
  - Consumer consumes 3, then 5

- **Producer blocking**: Logs show `⛔ Buffer FULL — producer WAITING` when producers are blocked
- **Race conditions**: With 5 producers competing, the order items enter the buffer depends on thread scheduling

---

### Example 3: Single Producer, Multiple Consumers, Limited Capacity
**Command:**
```bash
python -m src.main --buffer condition --producers 1 --consumers 3 --items 6 --capacity 2
```

**Result:**
```
Consumed items: [1, 2, 3, 4, 5, 6]
```

**Explanation:**
- **Single producer, 3 consumers**: Single production rate vs. high consumption rate
- **Limited capacity (2)**: Buffer stays small but doesn't block producers often
- **Order preserved**: Despite multiple consumers, items are consumed in order `[1, 2, 3, 4, 5, 6]`
- **Why order is maintained?**:
  - Single producer produces sequentially (1, 2, 3, 4, 5, 6)
  - Consumers compete for items, but since production is sequential and buffer capacity is small, items are consumed in FIFO order
  - Each consumer processes items as they become available, maintaining sequential order

- **Sentinel handling**: With 3 consumers, 3 sentinels are sent (one per consumer)
- **Logs show**: Multiple sentinels being added and consumed, with consumers stopping as they receive their sentinel
- **No producer blocking**: With only one producer and fast consumption, the buffer rarely fills up

---

### Key Observations from Results

1. **Buffer Capacity Impact**: Smaller buffer capacity increases the likelihood of blocking and can affect consumption order with multiple producers.

2. **Producer/Consumer Ratio**: 
   - More producers than consumers → buffer fills up, producers block
   - More consumers than producers → buffer empties quickly, consumers wait less

3. **Order Preservation**: Order is best preserved with:
   - Single producer (sequential production)
   - Sufficient buffer capacity
   - Single consumer (no race conditions)

4. **Concurrency Effects**: Multiple threads introduce non-deterministic ordering due to thread scheduling, especially visible with limited buffer capacity.

## Design Sketch

```
[SourceContainer] --> [Producer Thread] --> [Shared Buffer] --> [Consumer Thread] --> [DestinationContainer]
```

Key points:
- Producer copies items from the source into the shared buffer, then emits a sentinel to signal completion (one sentinel per consumer for multi-thread runs).
- Consumer drains the buffer until it sees the sentinel; no items are lost and shutdown is clean across multiple producers/consumers.
- Two buffer implementations are provided: one using `queue.Queue` (blocking queue) and one using `threading.Condition` (explicit wait/notify).

## Testing

### Running Tests

From the `producer_consumer` folder, run:
```bash
pytest
```

This will run all tests and print each passing test along with a suite summary.

**Expected Output:**
```
========================================================================== test session starts ===========================================================================
platform win32 -- Python 3.11.4, pytest-9.0.1, pluggy-1.6.0
rootdir: C:\Users\...\producer_consumer
configfile: pytest.ini
plugins: anyio-4.8.0, langsmith-0.3.34
collected 19 items

tests\test_buffer_condition.py .PASS: ...
tests\test_buffer_queue.py .PASS: ...
tests\test_concurrency_behavior.py .PASS: ...
tests\test_config.py .PASS: ...
tests\test_data_source.py .PASS: ...
tests\test_main.py .PASS: ...
tests\test_producer_consumer.py .PASS: ...
tests\test_thread_delays.py .PASS: ...

=========================================================================== 19 passed in 0.37s ===========================================================================
```

### Test Suite Overview

The test suite consists of **19 tests** organized across **8 test files**, covering all aspects of the producer-consumer implementation:

#### Test Files and Coverage

1. **`test_buffer_condition.py`** (3 tests)
   - **Purpose**: Tests the condition-based buffer implementation using `threading.Condition`
   - **Coverage**:
     - FIFO ordering: Verifies items are retrieved in the order they were added
     - Blocking semantics: Ensures producers block when buffer is full
     - Capacity validation: Rejects non-positive capacity values
   - **Key Tests**:
     - `test_put_and_get_order_condition`: Verifies FIFO ordering
     - `test_blocking_semantics_condition_buffer`: Tests producer blocking when buffer is full
     - `test_condition_buffer_rejects_non_positive_capacity`: Validates capacity constraints

2. **`test_buffer_queue.py`** (2 tests)
   - **Purpose**: Tests the queue-based buffer implementation using `queue.Queue`
   - **Coverage**:
     - FIFO ordering: Verifies items maintain order
     - Blocking behavior: Ensures `put()` blocks when queue is full until space is available
   - **Key Tests**:
     - `test_put_and_get_order`: Verifies FIFO ordering
     - `test_put_blocks_when_full_until_get_allows_progress`: Tests blocking queue behavior

3. **`test_concurrency_behavior.py`** (2 tests)
   - **Purpose**: Tests thread synchronization and concurrent access patterns
   - **Coverage**:
     - Wait/notify mechanisms: Verifies condition variables properly wake waiting threads
     - Thread handoff: Tests data transfer between producer and consumer threads
   - **Key Tests**:
     - `test_condition_buffer_waits_and_wakes_consumer`: Verifies consumer wakes when item is added
     - `test_blocking_queue_handoff_between_threads`: Tests thread-safe data transfer

4. **`test_config.py`** (1 test)
   - **Purpose**: Tests logging configuration
   - **Coverage**:
     - Handler cleanup: Ensures existing handlers are removed before configuration
     - Log level setting: Verifies log level is set correctly
   - **Key Test**:
     - `test_configure_logging_removes_existing_handlers_and_sets_level`

5. **`test_data_source.py`** (2 tests)
   - **Purpose**: Tests source and destination container implementations
   - **Coverage**:
     - Source immutability: Verifies `get_items()` returns a copy, not a reference
     - Thread-safe destination: Ensures concurrent adds to destination are safe
   - **Key Tests**:
     - `test_source_returns_copy`: Verifies source container returns copies
     - `test_destination_collects_items_thread_safe`: Tests thread-safe collection

6. **`test_main.py`** (3 tests)
   - **Purpose**: Tests the main entry point and buffer factory
   - **Coverage**:
     - Buffer factory: Validates buffer creation and error handling
     - Zero-item handling: Ensures system handles empty production gracefully
     - Multi-thread execution: Tests multiple producers and consumers
   - **Key Tests**:
     - `test_build_buffer_rejects_unknown_type`: Validates buffer factory error handling
     - `test_main_runs_with_zero_items`: Tests edge case with no items
     - `test_main_runs_with_multiple_producers_and_consumers`: Tests concurrent execution

7. **`test_producer_consumer.py`** (4 tests)
   - **Purpose**: End-to-end integration tests for the producer-consumer pipeline
   - **Coverage**:
     - Queue buffer pipeline: Full pipeline test with `BlockingQueueBuffer`
     - Condition buffer pipeline: Full pipeline test with `ConditionBuffer`
     - Empty source handling: Edge case with no items to produce
     - Multiple threads: Tests multiple producers and consumers with limited capacity
   - **Key Tests**:
     - `test_pipeline_with_queue_buffer`: End-to-end test with queue buffer
     - `test_pipeline_with_condition_buffer`: End-to-end test with condition buffer
     - `test_empty_source_finishes_cleanly`: Tests graceful handling of empty source
     - `test_multiple_consumers_and_producers_with_small_capacity`: Tests complex concurrent scenario

8. **`test_thread_delays.py`** (2 tests)
   - **Purpose**: Tests producer and consumer delay functionality
   - **Coverage**:
     - Producer delays: Verifies producers respect delay settings
     - Consumer delays: Verifies consumers respect delay settings
     - Sentinel flow: Ensures sentinel handling works correctly with delays
   - **Key Tests**:
     - `test_producer_respects_delay`: Tests producer delay timing
     - `test_consumer_respects_delay`: Tests consumer delay timing

### Test Results Summary

All **19 tests pass** successfully, covering:

- ✅ **Buffer Implementations**: Both `ConditionBuffer` and `BlockingQueueBuffer` are fully tested
- ✅ **Thread Safety**: Concurrent access patterns and synchronization mechanisms
- ✅ **Blocking Behavior**: Proper waiting and wake-up semantics
- ✅ **Order Preservation**: FIFO ordering maintained in both buffer types
- ✅ **Edge Cases**: Empty sources, zero items, invalid inputs
- ✅ **Integration**: End-to-end producer-consumer pipelines
- ✅ **Configuration**: Logging setup and configuration validation
- ✅ **Concurrency**: Multiple producers and consumers working together

### Test Organization

Tests are organized by component:
- **Buffer tests**: Separate files for each buffer implementation
- **Component tests**: Individual tests for producers, consumers, data sources
- **Integration tests**: End-to-end pipeline tests
- **Concurrency tests**: Thread synchronization and blocking behavior
- **Configuration tests**: Setup and configuration validation

### Running Specific Tests

You can run specific test files or individual tests:

```bash
# Run a specific test file
pytest tests/test_buffer_condition.py

# Run a specific test
pytest tests/test_buffer_condition.py::test_put_and_get_order_condition

# Run tests matching a pattern
pytest -k "buffer"

# Run with verbose output
pytest -v

# Run with output capture disabled (see print statements)
pytest -s
```
