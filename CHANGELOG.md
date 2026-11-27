# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.2.0 - 2026-11-27

### Added

- **Kontext builds on kontinuity and Konnect directly.** `kontext-core` depends on `kontinuity-retry` and
  `konnect-internet-protocol`, so resilience and the network types of Davils are part of every container, not an
  extra module.
- **A suspending creation that throws is tried again.** `ContainerBuilder.creationRetry` starts as
  `DEFAULT_CREATION_RETRY`, three attempts with an exponential backoff from 100 milliseconds to 2 seconds and a
  proportional jitter, and is inherited by a child: a suspending single that connects to a service which starts with
  the application waits for it instead of failing the container. The errors of Kontext itself, such as a missing
  definition, are never tried again, an instance created while the container closes is still closed, and
  `creationRetry = null` creates once.
- **The environment of kontinuity of a container.** `Resolver.resilience()` and `ContainerConfiguration.resilience`
  report to the tracer, the meters and the event bus of the container and measure time on its time source, so a retry,
  a circuit breaker or a cache a definition builds reports next to the container.
- **Network properties.** `port`, `portOrNull`, `portRange`, `ipAddress`, `ipAddressOrNull` and `ipNetwork` read a
  property as a `Port`, `PortRange`, `IpAddress` or `IpNetwork` of Konnect, with the constant reasons of its
  converters, which never show the value.

### Changed

- **Built on kontinuity 1.2.0 and Konnect 1.2.0.**

## 1.1.0 - 2026-11-26

### Added

- **`kontext-kaml`, properties from YAML.** `YamlPropertySource.parse(text)` and `properties { yaml(text) }` read the
  single document of a YAML text the application supplies, parsed by kaml within its limits, with nested mappings as
  dotted keys; a sequence, a key that is no property key or a document kaml refuses is refused at once.

### Changed

- **Built with Kreate 5.5.2 and Gradle 9.9.2 on Kore 2.5.0, konfetti 1.1.0, kollect 1.1.0 and Knot 1.2.0**, with the
  version floors of the build tools raised.

## 1.0.0 - 2026-11-25

The first version of Kontext: the dependency injection of kide as an open source library under the Apache License
2.0, for Kotlin on the JVM and on `wasmJs`, built with Kreate 5.4.1 on Kore 2.3.2, kollect 1.0.0, konfetti 1.0.0 and
Knot 1.0.0 and published to `https://repo.davils.com`. It offers what `kide-common-di` offers without a
third-party engine and without anything of kide.

### Added

- **The build** of Kontext with Kreate 5.4.1: one convention plugin per concern, strict dependency locking and
  verification, Detekt with every rule over main and test sources, unit tests on the JVM and Node.js, integration
  tests on the JVM, Kover at 97 %, the API and semver check, Trivy, the standards audit, the assurance gate and JMH
  benchmarks.
- **`kontext-core`**: modules with singles, factories, assisted factories, suspending definitions, constructor
  definitions for 0 to 22 parameters, scopes nested within scopes, scoped and provided definitions, bindings,
  includes, variants per `Profile` and lazy modules; containers with validation of every configuration at once,
  thread-safe and suspending resolution with typed failures and their path, typed parameters, named and typed
  qualifiers, properties from `PropertySource`s, child containers, loading and unloading modules at runtime, runtime
  declarations, scope ids, links and close handlers, coroutine scopes per container and scope, close functions run
  in the reverse order of creation and a `CloseReport`; `ContainerHolder`, the opt-in `GlobalContainer`, components
  with `by inject()`; the `ContainerExtension`, `ResolutionFallback` and `Retention` extension points; `verify { }`
  and `verifyStatically { }`; the `ContainerSink`, the `TextSink`, an event flow, meters of kollect, spans, health and
  events on a konfetti topic, without a log or a file of its own; and Java interop on the JVM.
- **`kontext-environment`**: `EnvironmentPropertySource` and `PropertiesBuilder.environment` over the environment
  variables of Knot.
- **`kontext-shutdown`**: `Shutdown.adopt`, which closes a container in its phase of a shutdown of Knot.
- **`kontext-testing`**: `testContainer { }` and `withTestContainer { }` with replaced modules and declared fakes,
  `TestProfiles`, `RecordingContainerSink`, `FailingContainerSink` and `RecordingTextSink`.
- **`kontext-bom`**: the bill of materials of every module.
