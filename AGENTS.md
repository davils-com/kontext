# AGENTS.md

Binding rules for every agent and every person working in this repository. Kontext is the dependency injection of
Davils applications: modules, profiles, scopes, child containers, verification and telemetry, for Kotlin on the JVM and
on WebAssembly (`wasmJs`). It is built on [Kore](https://github.com/davils-com/kore), reports through
[kollect](https://github.com/davils-com/kollect) and [konfetti](https://github.com/davils-com/konfetti), follows the
rules of Kore and is published under the Apache License 2.0, with security first.

## Repository

| Module | Artifact | Purpose |
|---|---|---|
| `kontext-core` | `com.davils:kontext-core` | Modules and their DSL (lambda and constructor definitions, suspending definitions, variants, lazy modules), containers, scopes with ids and links, child containers, typed parameters, properties, dynamic loading, holders, components, extensions, fallbacks, retention, validation, verification, the sinks and the meters, spans, health and events of a container, and Java interop on the JVM |
| `kontext-environment` | `com.davils:kontext-environment` | A `PropertySource` over the environment variables of Knot |
| `kontext-shutdown` | `com.davils:kontext-shutdown` | `Shutdown.adopt`, which closes a container in its phase of a shutdown of [Knot](https://github.com/davils-com/knot) |
| `kontext-testing` | `com.davils:kontext-testing` | `testContainer` with replaced modules, the test profiles and recording and failing sinks |
| `kontext-bom` | `com.davils:kontext-bom` | The bill of materials of every module |
| `kontext-benchmark` | - | JMH throughput benchmarks of resolution, never published |
| `build-logic` | - | One convention plugin per Kreate concern, composed by `kontext.kotlin-library` for the modules and `kontext.root` for the root project; they hold nothing but `plugins { }` and `kreate { }` |

The root package of a module is `com.davils.kontext.<module without kontext->`, such as `com.davils.kontext.core`, and
its Java module has the same name. A module with public API declares it in `src/jvmMain/java/module-info.java` and
exports only packages with public API; the runtime lives in `com.davils.kontext.core.internal`, which is never
exported.

Kontext uses Kore wherever Kore has the building block: `ValidatingBuilder` and `@DavilsDsl`, validation and violation
codes, `SafeText`, `immutableList()`, `contained { }` and the `Davils*` annotations. Never write a second version of
something Kore has.

Kontext offers what the dependency injection of kide (`kide-common-di`) offers, without a third-party engine and
without anything of kide: the live and mock data modes of kide are the generic `Profile`s here, the logger of kide is
the `ContainerSink`, and the shutdown binding lives in `kontext-shutdown`. Kontext never writes a file, a log or a
standard stream; everything it reports goes to a sink, a meter registry, a tracer or an event bus of the caller.

## Targets

Every module is a Kotlin Multiplatform library with two targets, `jvm` and `wasmJs`. The Wasm tests run under Node.js.

- Code lives in `src/commonMain/kotlin`. The only platform differences are what makes a container thread-safe:
  `platformGuard()` and `currentWorker()` in `Guard.kt`, a `ReentrantLock` with a condition and the current thread in
  `Guard.jvm.kt`, and `platformReentry()` in `Reentry.kt`, a thread-local flag in `Reentry.jvm.kt`; the Wasm actuals
  do nothing, because there is one thread.
- A host never holds its lock while caller code runs: a single is claimed for one worker, created outside the lock
  and stored afterwards; another worker waits for the claim, and a worker that would wait on itself, directly or
  through other waiting workers, fails with `Circular`. A closing host is sealed before its close functions run and
  refuses every later creation.
- Resolution is not suspending, so that a factory can resolve; starting, closing and opening a child are.

## Verify

```bash
./gradlew check
```

Nothing is done until it is green. `check` compiles with warnings as errors (Kotlin with progressive mode and
extra warnings, javac with `-Xlint:all -Werror`), runs Detekt with type resolution, the Kreate rule set and
`allRules`, the unit tests on the JVM and on Node.js, Kover verification at 97 % of lines, branches and
instructions, the API and semver check, the license header check, the changelog check, every Trivy scan and the
Kreate assurance gate, which blocks on every finding. The gate reads OSV.dev and deps.dev and Trivy downloads its
database, so `check` needs the network. The build runs on JDK 25; `gradle/gradle-daemon-jvm.properties` pins the
daemon to it and toolchains are never downloaded.

To build against a local checkout of Kore, such as a branch that is not published yet, substitute it for the published
artifact without changing a file:

```bash
./gradlew --include-build ../kore check
```

## Code rules

Enforced by Detekt with the Kreate rule set, the `libraries` rule set, `allRules` and warnings as errors, without
a baseline. Build scripts and `build-logic` follow the same rules, although Detekt does not analyse them.

- **License header first.** Every `.kt`, `.kts`, `.java`, `.properties` and YAML file starts with the Apache 2.0
  header, byte for byte as in the existing files; only the files Gradle generates in `gradle/` are exempt.
  `kreateLicenseHeaderCheck` and Detekt enforce it.
- **No comments.** No `//`, no `/* */` other than the license header. Names, types and tests carry the intent.
- **KDoc on public API only**, with `@param`, `@return`, `@throws` and `@since`, and never on internal or private
  declarations; protected members of public open or abstract classes are API and get KDoc. A new declaration gets
  the version it first ships in; existing `@since` values never change. KDoc states what a caller must know:
  behaviour on invalid input, the limits that apply, thread safety and every security consideration.
- **No `else`.** Return early; a `when` over a sealed type or an enum is exhaustive without `else ->`.
- **At most two chained calls.** Name intermediate results and computed conditions.
- **Named arguments** wherever a call passes two or more values of which the meaning is not obvious from the call.
- **One top-level type per file**, named after it; a file of functions is named after its role, such as
  `Containers.kt`.
- **No package for a single file.** A subpackage holds at least two files. A file that would stand alone in a
  package lives in the parent package instead. Exceptions are the root package of a module and an `internal` package.
- **No `!!`, no `lateinit`.** Model absence as a type or return early.
- **Explicit API.** Everything is `internal` unless a consumer needs it. A type only Kontext creates, such as a built
  container, has an `internal` constructor.
- **Names.** No type, file, function or property carries the name of the library or of kide as a prefix: `Container`,
  never `KontextContainer` or `KideContainer`. The package says where a declaration comes from. The only prefix is
  `Davils` on the annotations of Kore. The ids of the Gradle plugins in `build-logic` keep their `kontext.` prefix.
- **`@DavilsInternal`** API of Kore is used only through a local or file-level `@OptIn(DavilsInternal::class)`, and
  every such use is listed in the AGENTS.md of Kore, so that Kore keeps it stable.
- **Immutable values.** Public types are immutable; public collections are copied with `immutableList()`,
  `immutableSet()` or `immutableMap()` of Kore, never stored from `toList()` or `+`.
- **No public `data class`.** It freezes `copy` and `componentN` into the binary API. Write a regular class with
  `equals`, `hashCode` and `toString`; `value class` stays allowed.
- **Explicit return types** on public functions with an expression body or a result; a block body that returns
  nothing omits `: Unit`, which the compiler rejects as redundant.
- **Kotlin first**, Java only where Kotlin offers no equivalent, and only in `jvmMain`. Experimental Kotlin API is
  allowed only through a local `@OptIn` and never appears in a public signature.
- **Nothing global.** No mutable singletons, no static configuration, no threads or timers of the library's own.
  An application holds its container or a `ContainerHolder` and passes it on. The one sanctioned exception is
  `GlobalContainer`, behind the opt-in `@GlobalContainerApi`, for code migrating from a global injection context.
  Detekt does not enforce this rule; reviews do.
- **Generated code.** The constructor definitions (`singleOf`, `factoryOf`, `scopedOf`, `new`) for 0 to 22
  parameters are an arity expansion, marked `@DavilsGenerated` and excluded from coverage; a change to one arity is
  made to all of them, and tests cover several arities.

## Security rules

Everything a caller declares and everything its factories, close functions and sinks do may be hostile or broken.
Kontext is built so that a mistake of a caller fails closed.

- **Limits before work.** Names are bounded by `ContainerLimits.MAX_NAME_LENGTH` before they are matched, and a
  resolution stops at `ContainerLimits.MAX_RESOLUTION_DEPTH` instead of overflowing the stack. A new limit goes into
  `ContainerLimits`, never into a private constant.
- **Foreign code is contained.** Factories, close functions, close handlers and sinks run inside `contained { }` or
  `attempted { }` of Kore: a failure becomes a `ResolutionFailure`, a `DisposalFailure` or a rejected event, and a
  fatal error or a cancellation is never swallowed.
- **Escape untrusted text exactly once, at the sink.** A refused name is shown through `SafeText` of Kore.
- **No side effects of its own.** Kontext writes no file, no log and no standard stream; what it reports goes to the
  `ContainerSink`, the meter registry, the tracer and the event bus the caller configures.
- **Hide what Java must not reach.** The runtime lives in `com.davils.kontext.core.internal`, which
  `module-info.java` does not export.
- **Secure randomness only.** No `java.util.Random`, `kotlin.random.Random` or `ThreadLocalRandom`.

## Tests

- Kotest `FunSpec` only, Kotest assertions only, no mocking library; fakes live in a `fixture` package next to the
  tests that use them, or in `kontext-testing` when users need them too.
- One `context` per behaviour or function; test names read as sentences.
- Tests live in `src/commonUnitTest/kotlin`, which runs on the JVM and on Wasm, in the package of the code they test.
  A test that needs the JVM lives in `src/jvmUnitTest/kotlin`; a test of common code stays common wherever it can.
  `src/test` is rejected by the build.
- Property tests with adversarial generators for every parser and every security invariant.
- Every change ships with its tests; a fixed weakness ships with a test that would have caught it.

## Build

- Every module applies exactly one plugin, `id("kontext.kotlin-library")`, and declares nothing but its description
  and its dependencies, through the configurations of the source sets such as `commonMainApi`. `kore-testing` is
  added to the unit tests through the configurations that Kreate creates late. Detekt, Kover, Kotest, Trivy, the
  compiler and publishing are configured only in `build-logic`, through Kreate.
- `build-logic` holds no Kotlin classes, no tasks of its own and no Gradle API calls beyond reading the version
  catalog. What Kreate cannot express yet is added to Kreate, never programmed here.
- Every plugin in `build-logic` configures one Kreate concern and applies `kontext.base`:

  | Plugin | Configures |
  |---|---|
  | `kontext.base` | The Kreate plugin, the group and the version |
  | `kontext.dependency-locking` | Strict locking of every configuration |
  | `kontext.git-hooks` | The Git hooks |
  | `kontext.trivy` | Every Trivy scan |
  | `kontext.assurance` | The assurance gate and its `ignore` rules |
  | `kontext.platform` | The JDK, the Kotlin compiler, the targets and the Java module name |
  | `kontext.web-tools` | Node.js and Yarn for the Wasm target, from the repositories of the settings |
  | `kontext.testing` | Kotest and the test suites |
  | `kontext.detekt` | Detekt and its reports |
  | `kontext.coverage` | Kover and its verification |
  | `kontext.api-validation` | The API dump and the semver check |
  | `kontext.docs` | Dokka |
  | `kontext.publish` | The POM, the release guard and the Davils repository |
  | `kontext.archives` | The legal files and the manifest of every archive |
  | `kontext.tool-classpaths` | The version floors on the class paths of the build tools |
  | `kontext.changelog` | The changelog check |
  | `kontext.license-header` | The license header check |

  `kontext.kotlin-library` composes them for a module and adds only the class paths the assurance gate reads;
  `kontext.root` composes them for the root project. A new concern gets a plugin of its own, never a block in a
  composing plugin.
- Dependencies are declared in `gradle/libs.versions.toml`. Kontext depends on `kore-core`, `kollect-core`, `konfetti-core` and `knot-shutdown`, and its tests on
  `kore-testing`. Libraries of JetBrains and of Davils may be added when they are needed; every other dependency
  needs the approval of a maintainer.
- Only `repo.davils.com` serves `com.davils` and its subgroups, through `exclusiveContent`. Everything else comes
  from Maven Central, except the Gradle plugins `kotlin-dsl`, `kotlinx-benchmark-plugin` and
  `kotest-framework-plugin-gradle`, which only the Gradle Plugin Portal has, and Node.js and Yarn, which come from
  `nodejs.org` and the GitHub releases of Yarn. `gradle/verification-metadata.xml` names the Node.js archive of every
  platform the pipeline runs on. Nothing is ever resolved from `mavenLocal()`.
- After a dependency changes, refresh the locks and the verification metadata and review what changed:

  ```bash
  ./gradlew kreateResolveAndLockAll :build-logic:dependencies --write-locks --no-configuration-cache
  ```

  ```bash
  ./gradlew --write-verification-metadata pgp,sha256 --export-keys --no-configuration-cache check
  ```

- Kontext builds only with released versions of Kreate and Kore. `gradle/verification-metadata.xml` trusts
  `com.davils` artifacts only by the signature of the Davils release key `36F578E8AD2D4391E0FA6C18154C625FAD370063`;
  a snapshot is never put into the catalog, and no trust rule for unsigned snapshots is ever added.
- A finding of the assurance gate that is a known, harmless property of a dependency gets an `ignore` rule in
  `build-logic/src/main/kotlin/kontext.assurance.gradle.kts`, with a reason, the exact module and an expiry date no more
  than six months ahead. CRITICAL and HIGH findings can never be ignored.

## Public API

The `api/*.api` dumps are the contract. `kreateApiCheck` fails on every unrecorded change and
`kreateApiSemverCheck` on a version that does not match it. A deliberate change is recorded with
`./gradlew kreateApiDump` and gets a `CHANGELOG.md` entry. A breaking change needs a major version and a commit
marked as breaking. Deprecate before removing: `@Deprecated` with a `ReplaceWith`, for at least one minor release.
The values of the violation codes and the names of meters, spans and topics never change once released.

## Releases

The version lives in `gradle.properties` and nowhere else; `kreateReleaseGuard`, which runs before every
publication, refuses a build whose resolved version differs from it, so no environment variable can change what is
published. Every build without a release tag is a `-SNAPSHOT`. A release is a tag without a `v`, such as `1.0.0`,
that equals that version, sits on a commit of `main` and is built without any snapshot tooling; the guard refuses
every other combination before anything is published. Snapshots go to `https://repo.davils.com/snapshots`, releases
to `https://repo.davils.com/releases`, and never to Maven Central. The pipeline publishes with
`publishAllPublicationsToDavilsRepository` and `--no-configuration-cache`, with the credentials in
`DAVILS_MAVEN_NAME` and `DAVILS_MAVEN_SECRET`, as kotlin-workflow hands them to Gradle, and the signing key in
`ORG_GRADLE_PROJECT_signingInMemoryKey`, `…KeyId` and `…KeyPassword`; a release without a signing key fails. A
release of Kontext needs a released Kore.

## Git

Work on `develop`, release from `main`. Conventional Commits in English with the module as scope, such as
`feat(core): ...` or `fix(shutdown): ...`. Small commits, one concern each. Commit only when asked. No
`Co-Authored-By` trailer and no other attribution to tools or AI assistants.
