from src import config
from src.buffer_condition import ConditionBuffer
from src.buffer_queue import BlockingQueueBuffer
from src.consumer import Consumer
from src.data_source import DestinationContainer, SourceContainer
from src.producer import Producer


def run_pipeline(buffer):
    source = SourceContainer([1, 2, 3, 4])
    dest = DestinationContainer()

    producer = Producer(source.get_items(), buffer, config.SENTINEL)
    consumer = Consumer(buffer, dest, config.SENTINEL)
    producer.start()
    consumer.start()
    producer.join()
    consumer.join()
    return source, dest


def test_pipeline_with_queue_buffer():
    buffer = BlockingQueueBuffer(maxsize=2)
    source, dest = run_pipeline(buffer)
    assert dest.get_all() == list(source.get_items())


def test_pipeline_with_condition_buffer():
    buffer = ConditionBuffer(capacity=1)
    source, dest = run_pipeline(buffer)
    assert dest.get_all() == list(source.get_items())


def test_empty_source_finishes_cleanly():
    source = SourceContainer([])
    dest = DestinationContainer()
    buffer = BlockingQueueBuffer(maxsize=1)
    producer = Producer(source.get_items(), buffer, config.SENTINEL)
    consumer = Consumer(buffer, dest, config.SENTINEL)
    producer.start()
    consumer.start()
    producer.join()
    consumer.join()
    assert dest.get_all() == []


def test_multiple_consumers_and_producers_with_small_capacity():
    buffer = BlockingQueueBuffer(maxsize=1)
    destination = DestinationContainer()
    source_items = list(range(6))
    producers = [
        Producer(source_items[i::2], buffer, config.SENTINEL, emit_sentinel=False)
        for i in range(2)
    ]
    consumers = [Consumer(buffer, destination, config.SENTINEL) for _ in range(3)]

    for c in consumers:
        c.start()
    for p in producers:
        p.start()
    for p in producers:
        p.join()
    for _ in consumers:
        buffer.put(config.SENTINEL)
    for c in consumers:
        c.join()

    assert sorted(destination.get_all()) == source_items
