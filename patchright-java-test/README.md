# Patchright Java Integration Tests

This module (`patchright-java-test`) evaluates the **stealth and evasion capabilities** of the patched Playwright endpoints.

## 🎯 Purpose
It acts as a black-box tester against real-world, high-security bot detection services. We rely on the publicly available instances of the strictest anti-bot protections to dynamically ensure that `patchright-java` remains undetected after every patch release.

## 🧪 Testing Endpoints
- **Brotector**: Tests the framework against community-driven, ultra-strict fingerprinting tests (`https://ttlns.github.io/brotector/`).
- **Cloudflare**: A validation against generic CF firewall/Turnstile challenges (e.g., `https://nowsecure.nl`).
- **CreepJS**: One of the most exhaustive canvas/fingerprint probing sites (`https://abrahamjuliot.github.io/creepjs/`).

## 🛠 Usage
This module relies on the *patched and installed* release artifacts. Therefore, it is triggered **after** `build.sh` finishes placing `cc.marbro.patchright:patchright` in the local Maven repository.

Run tests manually assuming `build.sh` was just run for version `1.52.0`:
```bash
mvn test -Dpatchright.version=1.52.0
```

> **Note**: Depending on location/network, some external challenge URLs (like Cloudflare) may behave differently. Flaky network responses are expected to occasionally cause soft-failures.
