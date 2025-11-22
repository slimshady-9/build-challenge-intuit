"""Producer thread implementation."""

from __future__ import annotations

import logging
import threading
import time
from typing import Any, Iterable


class Producer(threading.Thread):
    """Reads items from the source and pushes them into the buffer."""

    def __init__(
        self,
        items: Iterable[Any],
        buffer: Any,
        sentinel: Any,
        delay: float = 0.0,
        emit_sentinel: bool = True,
    ) -> None:
        super().__init__(name="Producer")
        self._items = items
        self._buffer = buffer
        self._sentinel = sentinel
        self._delay = delay
        self._emit_sentinel = emit_sentinel

    def run(self) -> None:
        for item in self._items:
            logging.info("Producing %r", item)
            self._buffer.put(item)
            if self._delay:
                time.sleep(self._delay)
        if self._emit_sentinel:
            logging.info("Producer finished; sending sentinel")
            self._buffer.put(self._sentinel)
