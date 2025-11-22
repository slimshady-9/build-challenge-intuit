import pytest

from src import main


def test_build_buffer_rejects_unknown_type():
    with pytest.raises(ValueError):
        main.build_buffer("invalid", capacity=1)


def test_main_runs_with_zero_items(capsys):
    results = main.main(["--items", "0", "--buffer", "queue", "--capacity", "2", "--log-level", "ERROR"])
    assert results == []
    captured = capsys.readouterr()
    assert "Consumed items: []" in captured.out


def test_main_runs_with_multiple_producers_and_consumers(capsys):
    args = [
        "--items",
        "6",
        "--buffer",
        "queue",
        "--capacity",
        "1",
        "--log-level",
        "ERROR",
        "--producers",
        "2",
        "--consumers",
        "3",
    ]
    results = main.main(args)
    assert sorted(results) == list(range(1, 7))
    captured = capsys.readouterr()
    assert "Consumed items: " in captured.out
