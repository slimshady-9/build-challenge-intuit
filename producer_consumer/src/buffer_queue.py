"""Blocking queue backed buffer implementation."""

from __future__ import annotations

import logging
import queue
from typing import Any


class BlockingQueueBuffer:
    """Wraps queue.Queue to provide a blocking shared buffer."""

    def __init__(self, maxsize: int) -> None:
        self._queue: queue.Queue[Any] = queue.Queue(maxsize=maxsize)

    def put(self, item: Any) -> None:
        if self._queue.full():
            logging.debug(
                "⛔ Buffer FULL — producer WAITING (size=%d/%d)",
                self._queue.qsize(),
                self._queue.maxsize,
            )
        self._queue.put(item, block=True)
        logging.debug("Buffer put %r (size=%d)", item, self._queue.qsize())

    def get(self) -> Any:
        if self._queue.empty():
            logging.debug(
                "⏳ Buffer EMPTY — consumer WAITING (size=0/%d)",
                self._queue.maxsize,
            )
        item = self._queue.get(block=True)
        logging.debug("Buffer got %r (size=%d)", item, self._queue.qsize())
        return item

    def __len__(self) -> int:  # pragma: no cover - convenience
        return self._queue.qsize()
