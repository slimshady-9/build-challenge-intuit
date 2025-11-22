"""Consumer thread implementation."""

from __future__ import annotations

import logging
import threading
import time
from typing import Any

from .data_source import DestinationContainer


class Consumer(threading.Thread):
    """Drains the shared buffer into the destination container."""

    def __init__(
        self,
        buffer: Any,
        destination: DestinationContainer,
        sentinel: Any,
        delay: float = 0.0,
    ) -> None:
        super().__init__(name="Consumer")
        self._buffer = buffer
        self._destination = destination
        self._sentinel = sentinel
        self._delay = delay

    def run(self) -> None:
        while True:
            item = self._buffer.get()
            if item is self._sentinel:
                logging.info("Consumer received sentinel; stopping")
                break
            logging.info("Consuming %r", item)
            self._destination.add(item)
            if self._delay:
                time.sleep(self._delay)
