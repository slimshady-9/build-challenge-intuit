import time

from src.consumer import Consumer
from src.data_source import DestinationContainer
from src.producer import Producer


class _DummyBuffer:
    def __init__(self, initial=None):
        self.items = list(initial or [])

    def put(self, item):
        self.items.append(item)

    def get(self):
        return self.items.pop(0)


def test_producer_respects_delay(monkeypatch):
    sleeps = []

    def fake_sleep(duration):
        sleeps.append(duration)

    monkeypatch.setattr(time, "sleep", fake_sleep)
    sentinel = object()
    buffer = _DummyBuffer()
    producer = Producer([1, 2], buffer, sentinel, delay=0.01)

    producer.run()

    assert buffer.items == [1, 2, sentinel]
    assert sleeps == [0.01, 0.01]


def test_consumer_respects_delay(monkeypatch):
    sleeps = []

    def fake_sleep(duration):
        sleeps.append(duration)

    sentinel = object()
    buffer = _DummyBuffer([1, 2, sentinel])
    destination = DestinationContainer()
    monkeypatch.setattr(time, "sleep", fake_sleep)

    consumer = Consumer(buffer, destination, sentinel, delay=0.02)
    consumer.run()

    assert destination.get_all() == [1, 2]
    assert sleeps == [0.02, 0.02]
