from src.data_source import DestinationContainer, SourceContainer


def test_source_returns_copy():
    source = SourceContainer([1, 2, 3])
    items1 = list(source.get_items())
    items2 = list(source.get_items())
    assert items1 == [1, 2, 3]
    assert items2 == [1, 2, 3]
    items1.append(4)
    assert list(source.get_items()) == [1, 2, 3]


def test_destination_collects_items_thread_safe():
    dest = DestinationContainer()
    dest.add("a")
    dest.add("b")
    assert dest.get_all() == ["a", "b"]
