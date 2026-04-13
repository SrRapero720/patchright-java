package io.github.patchright.patcher;

import io.github.patchright.patcher.assist.PatchLog;
import io.github.patchright.patcher.patches.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point for the Patchright Java Patcher.
 * <p>
 * This tool applies AST-based patches to the playwright-java source code
 * to create patchright-java, mirroring what the .NET version does with Roslyn.
 * <p>
 * Categories of patches:
 * 1. DRIVER PATCHES: Point to Patchright driver releases
 * 2. SCRIPT INJECTION PATCHES: Remove sourceURL comments that reveal automation
 * 3. ISOLATED CONTEXT PATCHES: Add isolatedContext parameter to evaluate methods
 * 4. ROUTE INJECTION PATCHES: Install routes to intercept and modify document requests
 * 5. FOCUS CONTROL PATCHES: Add browser launch options for focus behavior control
 * 6. METADATA PATCHES: Update Maven POM metadata
 */
public class PatchrightPatcher {

    private final Path playwrightPath;
    private final String groupId;
    private final String version;

    public PatchrightPatcher(Path playwrightPath, String groupId, String version) {
        this.playwrightPath = playwrightPath;
        this.groupId = groupId;
        this.version = version;
    }

    public void patchAll() throws Exception {
        PatchLog.info();
        PatchLog.info("--- Patching Playwright Java to create Patchright ---");
        PatchLog.info();

        // 1. Driver URL patch
        PatchLog.info("[1/6] Patching driver download URL...");
        DriverPatch.apply(playwrightPath, version);

        // 2. Source URL removal (anti-detection)
        PatchLog.info("[2/6] Removing sourceURL annotations...");
        ScriptInjectionPatch.apply(playwrightPath);

        // 3. IsolatedContext parameter injection
        PatchLog.info("[3/6] Adding isolatedContext parameter to evaluate methods...");
        IsolatedContextPatch.apply(playwrightPath);

        // 4. Route injection for InitScript
        PatchLog.info("[4/6] Adding InitScript route injection...");
        RouteInjectionPatch.apply(playwrightPath);

        // 5. FocusControl parameter
        PatchLog.info("[5/6] Adding focusControl parameter...");
        FocusControlPatch.apply(playwrightPath);

        // 6. Maven/package metadata
        PatchLog.info("[6/6] Patching Maven metadata...");
        MetadataPatch.apply(playwrightPath, groupId, version);

        PatchLog.info();
        PatchLog.info("--- Patching complete! ---");
        PatchLog.info();
    }

    public static void main(String[] args) throws Exception {
        String playwrightDir = args.length > 0 ? args[0] : "playwright-java";
        String groupId = args.length > 1 ? args[1] : "cc.marbro.patchright";
        String version = args.length > 2 ? args[2] : null;

        Path playwrightPath = Paths.get(playwrightDir);
        if (!playwrightPath.toFile().isDirectory()) {
            PatchLog.error("Error: Playwright source directory not found: " + playwrightDir);
            PatchLog.error("Usage: java -jar patchright-java-patcher.jar <playwright-java-dir> [groupId] [driverVersion] [packageVersion]");
            System.exit(1);
        }

        PatchrightPatcher patcher = new PatchrightPatcher(playwrightPath, groupId, version);
        patcher.patchAll();
    }
}
