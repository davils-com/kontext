# Security Policy

## Supported Versions

Security fixes are released for the latest minor version of the current major release.

| Version | Supported |
|---------|-----------|
| 1.0.x   | Yes       |
| < 1.0   | No        |

## Reporting a Vulnerability

**Do not open a public issue for a security problem.**

Report it privately through GitHub's
[private vulnerability reporting](https://github.com/davils-com/kontext/security/advisories/new),
or by email to development@davils.com.

Please include:

- the affected Kontext version, module and target (JVM or Wasm),
- a description of the impact — what an attacker gains,
- the steps to reproduce it, ideally as a minimal Kotlin snippet or test,
- any mitigation you are already aware of.

We acknowledge a report within three working days and agree on a disclosure date with you.

## Scope

Kontext is a library: it runs with the privileges of the application that uses it. It treats as untrusted the names
a caller declares and what the factories, close functions, close handlers and sinks of the application do.

The following are **in scope**:

- a name or a chain of definitions that makes Kontext use memory or CPU beyond `ContainerLimits`, or overflow the
  stack,
- a single created twice, or a scoped instance shared between two scopes, under concurrent resolution,
- an instance that is never closed although its container or scope was closed, or that is closed twice,
- a failing factory, close function or sink that stops a container from closing what it created, or that is
  swallowed when it is a fatal error or a cancellation,
- a refused name that reaches a message unescaped, or a parameter or property value that reaches a message, an
  event or a `toString`,
- a resolution that returns an instance of another type than its key, such as from a `ResolutionFallback`,
- modules loaded or unloaded at runtime that leave the container in a state no validation would accept,
- a file, a log line or a write to a standard stream made by Kontext itself.

The following are **not** vulnerabilities in Kontext:

- a vulnerability in the JDK, Node.js, the Kotlin standard library, Kore, kollect, konfetti or Knot — report those to
  the respective project,
- what a factory, close function or sink of the application does with what it is given,
- a deadlock between two singles that resolve each other on different threads, which is a cycle that
  `ResolutionFailure.Circular` reports on one thread.

## Known Limitations

- On `wasmJs` a container holds no lock: WebAssembly runs one thread, so none is needed.
- Two singles that resolve each other on two threads at once can wait for each other; on one thread the same graph
  fails with `ResolutionFailure.Circular`, which `verify` reports before the application starts.
- A `ContainerSink` is called on the thread that resolves; a slow sink slows resolution down.
- The `GlobalContainer` is process-wide state behind an opt-in; code that uses it shares the container with every
  other user in the process.
- An instance replaced because its `Retention` expired is closed asynchronously; a holder that keeps it beyond the
  retention may see it closed.

## Verifying a Release

Releases are published to `https://repo.davils.com/releases` and signed with the release key of Davils; its
fingerprint is published in this section with the first release, so a consumer can pin it as a `trusted-key` in its
own `gradle/verification-metadata.xml`. A release also carries a signed build provenance attestation made by the
GitHub Actions run that built it:

```bash
gh attestation verify kontext-core-jvm-1.2.0.jar --repo davils-com/kontext --signer-repo davils-com/kotlin-workflow
```

Snapshots in `https://repo.davils.com/snapshots` are neither signed nor attested and are not meant for production.
