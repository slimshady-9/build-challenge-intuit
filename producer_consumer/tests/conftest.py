import pytest

SUITE_DESCRIPTIONS = {
    "tests/test_buffer_condition.py": "Condition buffer ordering, blocking semantics, capacity validation (wait/notify).",
    "tests/test_buffer_queue.py": "Queue buffer ordering and blocking queue behavior.",
    "tests/test_config.py": "Logging configuration and handler cleanup.",
    "tests/test_data_source.py": "Source copy semantics and thread-safe destination adds.",
    "tests/test_main.py": "Main entry wiring, buffer factory validation, zero-item run.",
    "tests/test_producer_consumer.py": "End-to-end producer/consumer flows with both buffer types and empty source.",
    "tests/test_thread_delays.py": "Producer/consumer delay handling and sentinel flow.",
    "tests/test_concurrency_behavior.py": "Thread synchronization, wait/notify wakeups, and blocking queue handoff.",
}


@pytest.hookimpl
def pytest_runtest_logreport(report):
    if report.when == "call" and report.passed:
        print(f"PASS: {report.nodeid}")


@pytest.hookimpl(trylast=True)
def pytest_sessionfinish(session, exitstatus):
    if exitstatus == 0:
        print("\nSuite summary (passed):")
        for path, desc in SUITE_DESCRIPTIONS.items():
            print(f"  {path} -> {desc}")
