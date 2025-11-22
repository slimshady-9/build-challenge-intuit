import threading
import time

from src.buffer_condition import ConditionBuffer
from src.buffer_queue import BlockingQueueBuffer


def test_condition_buffer_waits_and_wakes_consumer():
    buf = ConditionBuffer(capacity=1)
    received = []
    started = threading.Event()
    finished = threading.Event()

    def consumer():
        started.set()
        item = buf.get()
        received.append(item)
        finished.set()

    t = threading.Thread(target=consumer)
    t.start()
    started.wait(timeout=1)
    time.sleep(0.05)
    assert t.is_alive()  # consumer blocked waiting on empty buffer

    buf.put("payload")
    finished.wait(timeout=1)
    t.join(timeout=1)
    assert received == ["payload"]


def test_blocking_queue_handoff_between_threads():
    buf = BlockingQueueBuffer(maxsize=1)
    results = []
    consume_started = threading.Event()
    consume_finished = threading.Event()

    def consumer():
        consume_started.set()
        results.append(buf.get())
        results.append(buf.get())
        consume_finished.set()

    consumer_thread = threading.Thread(target=consumer)
    consumer_thread.start()
    consume_started.wait(timeout=1)

    buf.put("first")
    buf.put("second")

    consume_finished.wait(timeout=1)
    consumer_thread.join(timeout=1)
    assert results == ["first", "second"]
