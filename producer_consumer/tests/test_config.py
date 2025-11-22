import logging

from src import config


def test_configure_logging_removes_existing_handlers_and_sets_level():
    original_handlers = list(logging.root.handlers)
    try:
        dummy = logging.NullHandler()
        logging.root.addHandler(dummy)
        config.configure_logging(logging.WARNING)
        assert dummy not in logging.root.handlers
        assert logging.getLogger().level == logging.WARNING
    finally:
        logging.root.handlers = original_handlers
        logging.getLogger().setLevel(logging.NOTSET)
