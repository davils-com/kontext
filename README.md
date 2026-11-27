# Kontext

Kontext is the dependency injection of Davils applications for Kotlin on the JVM and on WebAssembly (`wasmJs`). It
works without reflection, code generation, an annotation processor or a global container by default, and leaves out
the usual weaknesses of dependency injection: errors only at runtime, silent overrides, erased generics, untyped
parameters, stack overflows on cycles, leaking scopes, and no suspending creation. It writes no file and no log
of its own, reports through sinks, [kollect](https://github.com/davils-com/kollect) and
[konfetti](https://github.com/davils-com/konfetti), is built on [Kore](https://github.com/davils-com/kore) and is
published under the [Apache License 2.0](LICENSE).

| Module | Artifact | What it holds |
|---|---|---|
| `kontext-core` | `com.davils:kontext-core` | Modules, containers, scopes, child containers, profiles, parameters, properties, coroutines, verification, telemetry, Java interop |
| `kontext-environment` | `com.davils:kontext-environment` | Properties from the environment variables of [Knot](https://github.com/davils-com/knot) |
| `kontext-shutdown` | `com.davils:kontext-shutdown` | Closing a container in its phase of a shutdown of Knot |
| `kontext-kaml` | `com.davils:kontext-kaml` | Properties from YAML text the application supplies, parsed by [kaml](https://github.com/davils-com/kaml) |
| `kontext-krypt` | `com.davils:kontext-krypt` | The environment of [krypt](https://github.com/davils-com/krypt) as a single and properties read as secrets |
| `kontext-kheck` | `com.davils:kontext-kheck` | A logging of [kheck](https://github.com/davils-com/kheck) closed with the container, and its loggers by name |
| `kontext-failure` | `com.davils:kontext-failure` | The failure handler of Knot as a single, drained in a shutdown of Knot next to the container |
| `kontext-testing` | `com.davils:kontext-testing` | Test containers, declared fakes, recording and failing sinks |
| `kontext-bom` | `com.davils:kontext-bom` | The bill of materials of every module |

Every published module is a Kotlin Multiplatform library for `jvm` and `wasmJs`, written for Kotlin 2.4 or newer. On
the JVM it requires JDK 25 and is a named Java module, such as `com.davils.kontext.core`.

## Installation

Kontext is published to the Davils repository, not to Maven Central. Declare it for the `com.davils` group only:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        exclusiveContent {
            forRepository { maven("https://repo.davils.com/releases") }
            filter { includeGroupAndSubgroups("com.davils") }
        }
        mavenCentral()
    }
}
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("com.davils:kontext-bom:1.2.0"))
    implementation("com.davils:kontext-core")
    testImplementation("com.davils:kontext-testing")
}
```

## Modules and containers

```kotlin
val storage = module("app.storage") {
    single(eager = true, close = { pool: Pool -> pool.close() }) { Pool(property("db.url")) }
    singleOf(::PostgresRepository).bind<Repository>()
    factory { Transaction(get(), parameters.get<IsolationLevel>()) }
    singleAsync { Migrations(get()).applied() }
    variant(Profile("live")) { single<Mailer> { SmtpMailer() } }
    variant(Profile("mock")) { single<Mailer> { FakeMailer() } }
}

val container = container(Profile("live")) {
    name = ContainerName("app")
    properties { values("db.url" to "postgres://db") }
    modules(storage)
}

val repository = container.get<Repository>()
val transaction = container.get<Transaction>(parameters = parametersOf(IsolationLevel.Serializable))
val migrations = container.await<Migrations>()
container.close()
```

- **Definitions:** `single` (eager, close function, retention), `factory`, `assisted<P, T>`, the suspending
  `singleAsync`, `factoryAsync` and `scopedAsync`, and the constructor definitions `singleOf`, `factoryOf`, `scopedOf`
  for 0 to 22 parameters. `module(name, eager = true)` makes every single eager; `include` and `+` combine modules.
- **Keys:** a definition is found by its full type, generic arguments included, and an optional `Qualifier`: by name
  (`Qualifier("db.primary")`, `Qualifier.of(enum)`) or by marker type (`qualifier<Primary>()`). `bind<A>()` adds a
  further type.
- **Resolution:** `get`, `getOrNull`, `getAll`, `getLazy` and, suspending, `await`, `awaitOrNull`, `awaitAll`. A
  failure is a `ResolutionException` with a typed `ResolutionFailure` and the path that led to it: `Missing`,
  `Circular`, `Ambiguous`, `Failed`, `Unprovided`, `Closed`, `TooDeep`, `Suspending`, `NotReady`,
  `ParametersRefused` or `Mismatched`. `getOrNull` gives `null` only for a missing definition.
- **Parameters:** `parametersOf(…)` are typed, bounded, never shown in a message and visible only to the definition
  they are passed to. A single or scoped definition refuses them, because a second resolution could not honour them.
- **Validation:** a configuration is checked at once, before anything is created: duplicate and shadowed
  definitions, duplicate modules, reserved keys and conflicting, unknown or cyclic scopes. Overriding is only
  possible with `replace`, and a replacement must replace something.

## Scopes

```kotlin
val web = module("app.web") {
    scope(ScopeKey("session")) { scopedOf(::Cart) }
    scope(ScopeKey("request"), within = ScopeKey("session")) {
        provided<HttpRequest>()
        scoped(close = { handler: Handler -> handler.finish() }) { Handler(get(), get()) }
    }
}

val session = container.openOrGetScope(ScopeKey("session"), ScopeId("user-42"))
session.openScope(ScopeKey("request")) { provide(request) }.use { scope -> scope.get<Handler>() }
```

Scopes have optional ids (`scope(id)`, `openOrGetScope`), link to other scopes (`link`, `unlink`), take provided
values late (`declare`), tell close handlers (`onClosed`) and own a coroutine scope cancelled with them. A container
holds at most `scopeLimit` open scopes, so a leak fails loudly, and closes every open scope when it closes.

## Coroutines

- Suspending definitions are created at most once at a time, in the coroutine scope of their holder: a cancelled
  caller does not cancel the creation for the others, and closing the holder ends it.
- Eager suspending singles start concurrently and roll the start back when one fails.
- `lazyModule` declares a module after the start; `awaitReady` and `await` wait for it, `get` reports `NotReady`.
- `Container.events` is a `SharedFlow`, `Container.state` a `StateFlow`; `use`, `withScope` close for sure.

## Dynamic modules

`container.load(modules)` validates modules together with what the container holds and loads them atomically;
`handle.unload()` removes them, closes their scopes and instances and reports failures. `container.declare(instance)`
declares a single at runtime. A `ContainerHolder` starts, stops and reloads the container of an application; the
process-wide `GlobalContainer` and `GlobalComponent` exist for migrations behind `@GlobalContainerApi`.

## Components

```kotlin
class Screen(override val resolver: Resolver) : ContainerComponent {
    private val presenter: Presenter by inject()
}
```

## Extension points

| SPI | Purpose |
|---|---|
| `ContainerSink` | Every `ContainerEvent`, synchronously |
| `TextSink` | Events as escaped lines for a logger such as SLF4J, `asContainerSink(minimum)` |
| `PropertySource` | Where properties come from; `kontext-environment` reads environment variables |
| `ContainerExtension` | Hooks on start, load, unload and close |
| `ResolutionFallback` | Instances from elsewhere, checked against the key |
| `Retention` | How long a single or scoped instance is reused, such as `Retention.expireAfter(10.minutes)` |

Meters (`kontext.instances`, `kontext.instance.creation`, `kontext.resolution.failures`, `kontext.scopes.open`,
`kontext.children.open`, `kontext.events.rejected`, `kontext.container.state`), the spans `kontext.start` and
`kontext.close`, events on `ContainerEvent.TOPIC` and the container as a `HealthIndicator` complete the telemetry.

## Verification

```kotlin
verifyStatically(setOf(Profile("live"), Profile("mock"))) { modules(storage, web) }
verify(setOf(Profile("live"), Profile("mock"))) { modules(storage, web) }
```

`verifyStatically` follows the dependencies of constructor definitions without creating anything, on every platform,
and reports missing and ambiguous dependencies and cycles. `verify` starts every profile and resolves every
definition and scope.

## Java

```java
Module module = JavaModules.module("app", builder -> builder
    .single(Repository.class, context -> new Repository())
    .factory(Service.class, context -> new Service(context.get())));
Container container = JavaModules.start(Profile.Companion.getDEFAULT(), builder -> builder.modules(module));
Service service = JavaResolvers.get(container, Service.class);
JavaModules.close(container);
```

## Davils libraries

```kotlin
val platform = module("app.platform") {
    single { shutdown(applicationScope) }
    cryptoEnvironment { policy = CryptoPolicy.Cnsa2 }
    logging { console { } }
    failures { sinks { sink("disk", diskSink) } }
    single { Database(property("db.url"), secret("db.password")) }
}

shutdown.adoptFailures(container)
shutdown.adopt(container)
val log = container.logger("app.payments")
```

- **`kontext-krypt`:** `cryptoEnvironment` declares the `Environment` of krypt with the tracer, the meters and the
  event bus of the container; `secret` and `secretOrNull` read a property as a `SecretText` that never shows its
  value.
- **`kontext-kheck`:** `logging` declares a `Logging` on the time source and the event bus of the container, which
  the container flushes and closes within a timeout; `logger(name)` and `logger<T>()` resolve its loggers.
- **`kontext-failure`:** `failures` declares the `FailureHandler` of Knot with the telemetry of the container and the
  `Shutdown` it declares as the target of a fatal failure; `Shutdown.adoptFailures` drains it in the drain phase,
  next to `Shutdown.adopt` of `kontext-shutdown`.

## Benchmarks

`./gradlew :kontext-benchmark:benchmark` measures resolution with JMH; the module is never published.

## Security

See [SECURITY.md](SECURITY.md).

## Building

```bash
./gradlew check
```

## License

Kontext is licensed under the [Apache License 2.0](LICENSE).
