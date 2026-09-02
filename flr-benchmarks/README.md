# Parser-context JMH benchmark

`ParserContextBenchmark` is a lexerful JMH benchmark for parser-context
overhead. It exercises ordered choices with late failure, backtracking, named
rule calls, token matching, positive lookahead, and memoized and non-memoized
paths over 2,000 generated statements.

JMH runs the six combinations of these parameters in independent forks:

- `scenario`: `A_NO_CONTEXT`, `B_CONTEXT_UNREACHABLE`, `C_CONTEXT_USED`
- `memoized`: `false`, `true`

Scenarios A and B parse the same ordinary input through equivalent grammar
choices. A contains no parser-context expression and uses `Machine`. B contains
context expressions behind an unselected rare branch and therefore uses
`ContextAwareMachine`, but never executes a context-enter instruction. C uses
the same context-capable grammar as B and selects the contextual branch.

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
  -prof gc -p scenario=B_CONTEXT_UNREACHABLE -p memoized=true -f 1 -wi 2 -i 3
```

To compare another FLR build, substitute its core jar when configuring the
benchmark module:

```shell
./gradlew :flr-benchmarks:jmhJar \
  -PflrBenchmarkCoreJar=/path/to/flr-core.jar
java -jar flr-benchmarks/build/libs/flr-benchmarks-1.6.0-SNAPSHOT-jmh.jar \
  -prof gc -p scenario=A_NO_CONTEXT
```

Human-readable and JSON results are written under `flr-benchmarks/build/reports/jmh/`.
