FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app/data-analysis

COPY src ./src
COPY test ./test
COPY sales_data.csv README.md ./

RUN mkdir -p bin && javac -d bin src/*.java test/*.java

CMD ["java", "-cp", "bin", "Main"]
