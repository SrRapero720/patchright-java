package io.github.patchright.patcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Patches the driver download script to use Patchright releases
 * instead of the official Playwright CDN.
 */
public class DriverPatch {

    private static final String OLD_CDN_URL = "https://playwright.azureedge.net/builds/driver";
    private static final String NEW_CDN_URL = "https://github.com/Kaliiiiiiiiii-Vinyzu/patchright/releases/download";

    // The download_driver.sh constructs URLs like: playwright-${DRIVER_VERSION}-${PLATFORM}.zip
    // Patchright releases use: v${VERSION}/playwright-${VERSION}-${PLATFORM}.zip
    private static final String OLD_URL_PATTERN = "${DRIVER_URL}/playwright-${DRIVER_VERSION}";
    private static final String NEW_URL_PATTERN = "${DRIVER_URL}/v${DRIVER_VERSION}/playwright-${DRIVER_VERSION}";

    public static void apply(Path playwrightPath) throws IOException {
        patchDownloadScript(playwrightPath);
    }

    private static void patchDownloadScript(Path playwrightPath) throws IOException {
        Path scriptPath = playwrightPath.resolve("scripts/download_driver.sh");
        if (!Files.exists(scriptPath)) {
            System.out.println("  WARNING: download_driver.sh not found at " + scriptPath);
            return;
        }

        String content = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);

        // Replace CDN URL
        content = content.replace(OLD_CDN_URL, NEW_CDN_URL);

        // Fix URL construction to include version prefix in path
        content = content.replace(OLD_URL_PATTERN, NEW_URL_PATTERN);

        Files.write(scriptPath, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("  Patched: " + scriptPath);
    }
}
