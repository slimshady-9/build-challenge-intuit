"""Entry point wiring the producer-consumer demo together."""

from __future__ import annotations

import argparse
import logging
from typing import Iterable, List

from . import config
from .buffer_condition import ConditionBuffer
from .buffer_queue import BlockingQueueBuffer
from .consumer import Consumer
from .data_source import DestinationContainer, SourceContainer
from .producer import Producer


def build_buffer(buffer_type: str, capacity: int):
    if buffer_type == "queue":
        return BlockingQueueBuffer(capacity)
    if buffer_type == "condition":
        return ConditionBuffer(capacity)
    raise ValueError(f"Unknown buffer type: {buffer_type}")


def parse_args(argv: Iterable[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Producer-consumer demo")
    parser.add_argument(
        "--buffer",
        choices=["queue", "condition"],
        default="queue",
        help="Shared buffer implementation to use",
    )
    parser.add_argument(
        "--capacity",
        type=int,
        default=config.QUEUE_SIZE,
        help="Maximum items in the shared buffer",
    )
    parser.add_argument(
        "--items",
        type=int,
        default=5,
        help="Number of incremental items to produce",
    )
    parser.add_argument(
        "--producers",
        type=int,
        default=1,
        help="Number of producer threads",
    )
    parser.add_argument(
        "--consumers",
        type=int,
        default=1,
        help="Number of consumer threads",
    )
    parser.add_argument(
        "--log-level",
        default="INFO",
        choices=["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
        help="Logging verbosity",
    )
    return parser.parse_args(argv)


def main(argv: Iterable[str] | None = None) -> List[int]:
    args = parse_args(argv)
    config.configure_logging(getattr(logging, args.log_level))

    source_items = list(range(1, args.items + 1))
    source = SourceContainer(source_items)
    destination = DestinationContainer()
    buffer = build_buffer(args.buffer, args.capacity)

    logging.info(
        "Starting demo with %d items using %s buffer (capacity=%d)",
        len(source_items),
        args.buffer,
        args.capacity,
    )

    consumer_threads = [
        Consumer(buffer, destination, config.SENTINEL, config.CONSUMER_DELAY)
        for _ in range(max(1, args.consumers))
    ]

    producer_chunks = [
        source_items[i :: max(1, args.producers)] for i in range(max(1, args.producers))
    ]
    producer_threads = [
        Producer(chunk, buffer, config.SENTINEL, config.PRODUCER_DELAY, emit_sentinel=False)
        for chunk in producer_chunks
    ]

    for consumer in consumer_threads:
        consumer.start()
    for producer in producer_threads:
        producer.start()

    for producer in producer_threads:
        producer.join()

    for _ in range(len(consumer_threads)):
        buffer.put(config.SENTINEL)

    for consumer in consumer_threads:
        consumer.join()

    results = destination.get_all()
    logging.info("All threads completed. Destination collected: %s", results)
    print(f"Consumed items: {results}")
    return results


if __name__ == "__main__":  # pragma: no cover
    main()
