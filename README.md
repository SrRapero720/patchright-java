<h1 align="center">Patchright Java</h1>

<p align="center">
  Undetected Java version of the Playwright testing and automation library.
</p>

> **Note**
> This repository serves the Patchright Java Package. To use Patchright with Python or NodeJS, check out the [Python Package](https://github.com/Kaliiiiiiiiii-Vinyzu/patchright-python) or the [NodeJS Package](https://github.com/Kaliiiiiiiiii-Vinyzu/patchright-nodejs). Also check out the main [Patchright Driver Repository](https://github.com/Kaliiiiiiiiii-Vinyzu/patchright).

## Installation

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>cc.marbro.patchright</groupId>
  <artifactId>patchright</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

Since this is published to GitHub Packages, you'll also need to configure the repository:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Kaliiiiiiiiii-Vinyzu/patchright-java</url>
  </repository>
</repositories>
```

## Usage

Patchright is a **drop-in replacement** for Playwright. Since only the Maven coordinates (`groupId` and `artifactId`) were modified and the internal Java package names remain unchanged, **no code adjustments are necessary**. Just keep using the standard Playwright imports:

```java
import com.microsoft.playwright.*;
```

> **Warning**
> Patchright only patches CHROMIUM based browsers. Firefox and Webkit are not supported.

### Recommended Configuration

```java
try (Playwright playwright = Playwright.create()) {
    Browser browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions()
            .setChannel("chrome")
            .setHeadless(false)
    );
    BrowserContext context = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(null) // No default viewport
    );
    Page page = context.newPage();
    page.navigate("https://example.com");
}
```

> **Note**
> We recommend using Google Chrome instead of Chromium. Install it via `playwright install chrome` and use it with `.setChannel("chrome")`.

## Extended Patchright API

### `isolatedContext` Parameter

All `evaluate`, `evaluateHandle`, `evalOnSelector`, and `evalOnSelectorAll` methods accept an additional `isolatedContext` parameter (default: `true`):

```java
// Execute in isolated context (default, stealth)
Object result = page.evaluate("() => document.title");

// Execute in main context (NOT stealth, same as original Playwright)
Object result = page.evaluate("() => document.title", null, false);
```

- `true` (default): Executes JavaScript in an isolated utility context, avoiding `Runtime.enable` detection.
- `false`: Executes in the main world context (same as original Playwright).

### `focusControl` Parameter

Browser launch and context creation options include `focusControl`:

```java
Browser browser = playwright.chromium().launch(
    new BrowserType.LaunchOptions()
        .setFocusControl(true)
);
```

## How It Works

### [Runtime.enable](https://vanilla.aslushnikov.com/?Runtime.enable) Leak
Patchright avoids using `Runtime.enable` by executing JavaScript in isolated ExecutionContexts, which is the biggest patch it applies.

### [Console.enable](https://vanilla.aslushnikov.com/?Console.enable) Leak
Patchright disables the Console API to prevent this leak. Console functionality will not work.

### Command Flags Leaks
Patchright tweaks the default browser args:
- `--disable-blink-features=AutomationControlled` (added)
- `--enable-automation` (removed)
- `--disable-popup-blocking` (removed)
- `--disable-component-update` (removed)
- `--disable-default-apps` (removed)
- `--disable-extensions` (removed)

## Stealth

With the right setup, Patchright is considered undetectable and passes:
- [Brotector](https://kaliiiiiiiiii.github.io/brotector/) ✅
- [Cloudflare](https://cloudflare.com/) ✅
- [Kasada](https://www.kasada.io/) ✅
- [Akamai](https://www.akamai.com/products/bot-manager/) ✅
- [Datadome](https://datadome.co/products/bot-protection/) ✅
- [Fingerprint.com](https://fingerprint.com/products/bot-detection/) ✅

## Development

This project uses a JavaParser-based AST patcher to transform the official [playwright-java](https://github.com/microsoft/playwright-java) source code into patchright-java.

### Building from source

```bash
# Build with specific version
bash scripts/build.sh 1.52.0

# Build with latest
bash scripts/build.sh
```

### Project Structure

```
patchright-java/
├── patcher/          # JavaParser-based AST patching tool
├── scripts/          # Build and download scripts
├── .github/          # CI/CD workflows
├── README.md
└── LICENSE
```

## Copyright and License

© [Vinyzu](https://github.com/Vinyzu/)

Patchright is licensed [Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)

## Disclaimer

This repository is provided for educational purposes only. No warranties are provided regarding accuracy, completeness, or suitability for any purpose. Use at your own risk.
