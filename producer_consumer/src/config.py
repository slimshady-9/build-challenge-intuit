"""Configuration constants for the producer-consumer demo."""

import logging

QUEUE_SIZE = 2
SENTINEL = "<SENTINEL>"
PRODUCER_DELAY = 0.0
CONSUMER_DELAY = 0.0


def configure_logging(level: int = logging.INFO) -> None:
    """Set up a simple logging format shared across modules."""
    for handler in logging.root.handlers[:]:
        logging.root.removeHandler(handler)
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(threadName)s] %(levelname)s: %(message)s",
    )
