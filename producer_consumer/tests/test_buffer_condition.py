import threading
import time

import pytest

from src.buffer_condition import ConditionBuffer


def test_put_and_get_order_condition():
    buf = ConditionBuffer(capacity=2)
    buf.put("a")
    buf.put("b")
    assert buf.get() == "a"
    assert buf.get() == "b"


def test_blocking_semantics_condition_buffer():
    buf = ConditionBuffer(capacity=1)
    buf.put("first")

    started = threading.Event()

    def producer():
        started.set()
        buf.put("second")

    thread = threading.Thread(target=producer)
    thread.start()
    started.wait(timeout=1)
    time.sleep(0.05)
    assert thread.is_alive()

    assert buf.get() == "first"
    thread.join(timeout=1)
    assert not thread.is_alive()
    assert buf.get() == "second"


def test_condition_buffer_rejects_non_positive_capacity():
    with pytest.raises(ValueError):
        ConditionBuffer(capacity=0)
