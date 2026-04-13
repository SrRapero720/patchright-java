package io.github.patchright.patcher;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point for the Patchright Java Patcher.
 * 
 * This tool applies AST-based patches to the playwright-java source code
 * to create patchright-java, mirroring what the .NET version does with Roslyn.
 * 
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
    private final String driverVersion;
    private final String packageVersion;

    public PatchrightPatcher(Path playwrightPath, String groupId, String driverVersion, String packageVersion) {
        this.playwrightPath = playwrightPath;
        this.groupId = groupId;
        this.driverVersion = driverVersion;
        this.packageVersion = packageVersion;
    }

    public void patchAll() throws Exception {
        System.out.println();
        System.out.println("--- Patching Playwright Java to create Patchright ---");
        System.out.println();

        // 1. Driver URL patch
        System.out.println("[1/6] Patching driver download URL...");
        DriverPatch.apply(playwrightPath);

        // 2. Source URL removal (anti-detection)
        System.out.println("[2/6] Removing sourceURL annotations...");
        ScriptInjectionPatch.apply(playwrightPath);

        // 3. IsolatedContext parameter injection
        System.out.println("[3/6] Adding isolatedContext parameter to evaluate methods...");
        IsolatedContextPatch.apply(playwrightPath);

        // 4. Route injection for InitScript
        System.out.println("[4/6] Adding InitScript route injection...");
        RouteInjectionPatch.apply(playwrightPath);

        // 5. FocusControl parameter
        System.out.println("[5/6] Adding focusControl parameter...");
        FocusControlPatch.apply(playwrightPath);

        // 6. Maven/package metadata
        System.out.println("[6/6] Patching Maven metadata...");
        MetadataPatch.apply(playwrightPath, groupId, driverVersion, packageVersion);

        System.out.println();
        System.out.println("--- Patching complete! ---");
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        String playwrightDir = args.length > 0 ? args[0] : "playwright-java";
        String groupId = args.length > 1 ? args[1] : "io.github.kailbin";
        String driverVersion = args.length > 2 ? args[2] : null;
        String packageVersion = args.length > 3 ? args[3] : null;

        Path playwrightPath = Paths.get(playwrightDir);
        if (!playwrightPath.toFile().isDirectory()) {
            System.err.println("Error: Playwright source directory not found: " + playwrightDir);
            System.err.println("Usage: java -jar patchright-patcher.jar <playwright-java-dir> [groupId] [driverVersion] [packageVersion]");
            System.exit(1);
        }

        PatchrightPatcher patcher = new PatchrightPatcher(playwrightPath, groupId, driverVersion, packageVersion);
        patcher.patchAll();
    }
}
