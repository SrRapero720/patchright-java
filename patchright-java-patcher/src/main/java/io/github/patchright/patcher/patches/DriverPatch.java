package io.github.patchright.patcher.patches;

import io.github.patchright.patcher.assist.PatchLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Patches the driver download script to use Patchright releases, instead of the official Playwright CDN.
 */
public class DriverPatch {

    /**
     * @link /playwright-java/scripts/DRIVER_VERSION
     */
    private static final String PATCH_DRIVER_VERSION = "scripts/DRIVER_VERSION";
    /**
     * @link /playwright-java/scripts/download_driver.sh
     */
    private static final String PATCH_DOWNLOAD_SH = "scripts/download_driver.sh";

    /**
     * https://github.com/Kaliiiiiiiiii-Vinyzu/patchright/releases
     */
    private static final String OLD_CDN_URL = "https://cdn.playwright.dev/builds/driver";
    private static final String NEW_CDN_URL = "https://github.com/Kaliiiiiiiiii-Vinyzu/patchright/releases/download";

    /**
     * _______________________playwright-${DRIVER_VERSION}-${PLATFORM}.zip
     * v${DRIVER_VERSION}/playwright-${DRIVER_VERSION}-${PLATFORM}.zip
     */
    private static final String OLD_URL_PATTERN = "URL=$URL/$FILE_NAME";
    private static final String NEW_URL_PATTERN = "URL=$URL/v${DRIVER_VERSION}/$FILE_NAME";

    public static void apply(Path playwrightPath, String version) throws IOException {
        // 覆盖 DRIVER_VERSION 文件里面的版本，官网正式版中版本号可能会有 子后缀，导致 DownloadScript 读不到文件里面的版本，下载失败
        setPatchDriverVersion(playwrightPath, version);
        //
        patchDownloadScript(playwrightPath);
    }

    private static void setPatchDriverVersion(Path playwrightPath, String version) throws IOException {
        Path scriptPath = playwrightPath.resolve(PATCH_DRIVER_VERSION);
        if (!Files.exists(scriptPath)) {
            PatchLog.error("  WARNING: " + PATCH_DRIVER_VERSION + " not found at " + scriptPath);
            return;
        }

        Files.write(scriptPath, version.getBytes(StandardCharsets.UTF_8));
        PatchLog.info("  Patched: " + scriptPath);
    }


    private static void patchDownloadScript(Path playwrightPath) throws IOException {
        Path scriptPath = playwrightPath.resolve(PATCH_DOWNLOAD_SH);
        if (!Files.exists(scriptPath)) {
            PatchLog.error("  WARNING: " + PATCH_DOWNLOAD_SH + " not found at " + scriptPath);
            return;
        }

        String content = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);

        // Replace CDN URL
        content = content.replace(OLD_CDN_URL, NEW_CDN_URL);

        // Fix URL construction to include version prefix in path
        content = content.replace(OLD_URL_PATTERN, NEW_URL_PATTERN);

        Files.write(scriptPath, content.getBytes(StandardCharsets.UTF_8));
        PatchLog.info("  Patched: " + scriptPath);
    }
}
