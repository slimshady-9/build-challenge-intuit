"""Source and destination containers for the demo."""

from __future__ import annotations

import threading
from typing import Iterable, List, Sequence, TypeVar

T = TypeVar("T")


class SourceContainer:
    """Simple holder for immutable source items."""

    def __init__(self, items: Sequence[T]) -> None:
        self._items: List[T] = list(items)

    def get_items(self) -> Iterable[T]:
        """Return a fresh iterable of items to produce."""
        return list(self._items)

    def __len__(self) -> int:  # pragma: no cover - convenience
        return len(self._items)


class DestinationContainer:
    """Thread-safe collection for consumed items."""

    def __init__(self) -> None:
        self._items: List[T] = []
        self._lock = threading.Lock()

    def add(self, item: T) -> None:
        with self._lock:
            self._items.append(item)

    def get_all(self) -> List[T]:
        with self._lock:
            return list(self._items)

    def __len__(self) -> int:  # pragma: no cover - convenience
        with self._lock:
            return len(self._items)
