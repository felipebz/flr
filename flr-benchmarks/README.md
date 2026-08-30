# Parser-context JMH benchmark

`ParserContextBenchmark` is a lexerful JMH benchmark for parser-context
overhead. It exercises ordered choices with late failure, backtracking, named
rule calls, token matching, positive lookahead, and memoized and non-memoized
paths over 2,000 generated statements.

JMH runs the four combinations of these parameters in independent forks:

- `context`: `false`, `true`
- `memoized`: `false`, `true`

Defaults are five one-second warmup iterations, ten one-second measurement
iterations, and three forks. Results use average time in milliseconds. The JMH
GC profiler additionally reports allocation rate and normalized bytes per
operation. There are no timing assertions.

Run every scenario with the repository-selected JDK:

```shell
./gradlew :flr-benchmarks:jmh
```

Build the standalone JMH jar to pass arbitrary JMH arguments for shorter
diagnostic runs or filtered scenarios:

```shell
./gradlew :flr-benchmarks:jmhJar
java -jar flr-benchmarks/build/libs/flr-benchmarks-1.6.0-SNAPSHOT-jmh.jar \
  -prof gc -p context=false -p memoized=true -f 1 -wi 2 -i 3
```

To compare another FLR build, substitute its core jar when configuring the
benchmark module:

```shell
./gradlew :flr-benchmarks:jmhJar \
  -PflrBenchmarkCoreJar=/path/to/flr-core.jar
java -jar flr-benchmarks/build/libs/flr-benchmarks-1.6.0-SNAPSHOT-jmh.jar \
  -prof gc -p context=false
```

Human-readable and JSON results are written under `flr-benchmarks/build/reports/jmh/`.
