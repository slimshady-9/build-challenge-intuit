"""Condition-based buffer to demonstrate explicit wait/notify."""

from __future__ import annotations

import logging
import threading
from typing import Any, List


class ConditionBuffer:
    """Shared buffer using threading.Condition for wait/notify."""

    def __init__(self, capacity: int) -> None:
        if capacity <= 0:
            raise ValueError("Capacity must be positive")
        self._capacity = capacity
        self._buffer: List[Any] = []
        self._condition = threading.Condition()
        self._was_empty = True
        self._was_full = False

    def put(self, item: Any) -> None:
        with self._condition:
            while len(self._buffer) >= self._capacity:
                if not self._was_full:
                    logging.info(
                        "⛔ Buffer FULL — producer WAITING (size=%d/%d)",
                        len(self._buffer),
                        self._capacity,
                    )
                    self._was_full = True
                self._condition.wait()
            self._buffer.append(item)
            self._was_empty = False
            if len(self._buffer) < self._capacity:
                self._was_full = False
            logging.info("Producer added: %r (size=%d/%d)", item, len(self._buffer), self._capacity)
            self._condition.notify_all()

    def get(self) -> Any:
        with self._condition:
            while not self._buffer:
                if not self._was_empty:
                    logging.debug("⏳ Buffer EMPTY — consumer WAITING (size=0/%d)", self._capacity)
                    self._was_empty = True
                self._condition.wait()
            item = self._buffer.pop(0)
            self._was_full = False
            if self._buffer:
                self._was_empty = False
            logging.info("Consumer got: %r (size=%d/%d)", item, len(self._buffer), self._capacity)
            self._condition.notify_all()
            return item

    def __len__(self) -> int:  # pragma: no cover - convenience
        with self._condition:
            return len(self._buffer)
