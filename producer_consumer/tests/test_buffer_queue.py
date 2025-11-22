import threading
import time

from src.buffer_queue import BlockingQueueBuffer


def test_put_and_get_order():
    buf = BlockingQueueBuffer(maxsize=2)
    buf.put(1)
    buf.put(2)
    assert buf.get() == 1
    assert buf.get() == 2


def test_put_blocks_when_full_until_get_allows_progress():
    buf = BlockingQueueBuffer(maxsize=1)
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
