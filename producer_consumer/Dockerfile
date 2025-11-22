FROM python:3.11-slim

WORKDIR /app/producer_consumer

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python", "-m", "src.main", "--buffer", "condition", "--producers", "1", "--consumers", "1", "--items", "6", "--capacity", "2"]
