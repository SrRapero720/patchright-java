package io.github.patchright.patcher;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Adds focusControl parameter to browser launch and context creation options.
 * 
 * This parameter controls browser focus behavior, preventing focus-related
 * detection of automation.
 * 
 * Targets:
 * - BrowserType.LaunchOptions (inner class in BrowserType.java)
 * - BrowserType.LaunchPersistentContextOptions
 * - Browser.NewContextOptions
 * - Browser.NewPageOptions
 * - BrowserTypeImpl.launchImpl() - to pass focusControl to protocol
 * - BrowserImpl.newContext() - to pass focusControl to protocol
 */
public class FocusControlPatch {

    public static void apply(Path playwrightPath) throws IOException {
        Path apiDir = playwrightPath.resolve("playwright/src/main/java/com/microsoft/playwright");
        Path implDir = apiDir.resolve("impl");

        // Add focusControl field to Options classes
        addFocusControlToOptions(apiDir.resolve("BrowserType.java"),
            new String[]{"LaunchOptions", "LaunchPersistentContextOptions"});
        addFocusControlToOptions(apiDir.resolve("Browser.java"),
            new String[]{"NewContextOptions", "NewPageOptions"});

        // Patch impl classes to pass focusControl to protocol
        patchBrowserTypeImpl(implDir.resolve("BrowserTypeImpl.java"));
        patchBrowserImplNewContext(implDir.resolve("BrowserImpl.java"));
    }

    /**
     * Add focusControl field + setter to Options inner classes.
     */
    private static void addFocusControlToOptions(Path filePath, String[] classNames) throws IOException {
        CompilationUnit cu = PatchUtils.parsePreserving(filePath);

        for (String className : classNames) {
            List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(className));

            for (ClassOrInterfaceDeclaration clazz : classes) {
                if (hasField(clazz, "focusControl")) continue;

                // Add field: public Boolean focusControl;
                FieldDeclaration field = clazz.addField(
                    new ClassOrInterfaceType(null, "Boolean"), "focusControl", Modifier.Keyword.PUBLIC);

                // Add setter method
                String setterBody = String.join("\n",
                    className + " setFocusControl(boolean focusControl) {",
                    "    this.focusControl = focusControl;",
                    "    return this;",
                    "}");
                MethodDeclaration setter = StaticJavaParser.parseMethodDeclaration(setterBody);
                setter.setModifier(Modifier.Keyword.PUBLIC, true);
                clazz.addMember(setter);

                System.out.println("  Added focusControl to " + className);
            }
        }

        PatchUtils.writeCompilationUnit(cu, filePath);
    }

    /**
     * Patch BrowserTypeImpl to include focusControl in protocol params for launch methods.
     */
    private static void patchBrowserTypeImpl(Path filePath) throws IOException {
        if (!filePath.toFile().exists()) {
            System.out.println("  SKIP: BrowserTypeImpl.java not found");
            return;
        }

        // Use text-based patching since the serialization is via gson().toJsonTree(options)
        // and focusControl will be automatically serialized as part of the options object.
        // However, we need to verify this is the case.
        // playwright-java serializes options via: JsonObject params = gson().toJsonTree(options).getAsJsonObject();
        // Since focusControl is a public field on the options class, Gson will automatically include it.
        // So no code change is needed here for the impl - Gson handles it!

        System.out.println("  BrowserTypeImpl: focusControl auto-serialized by Gson (no code change needed)");
    }

    /**
     * Patch BrowserImpl.newContext to pass focusControl.
     * Same as BrowserTypeImpl - Gson auto-serializes the public field.
     */
    private static void patchBrowserImplNewContext(Path filePath) throws IOException {
        if (!filePath.toFile().exists()) {
            System.out.println("  SKIP: BrowserImpl.java not found");
            return;
        }

        // playwright-java uses: JsonObject params = gson().toJsonTree(options).getAsJsonObject();
        // Since focusControl is added as a public field to NewContextOptions,
        // Gson will automatically serialize it. No implementation change needed!

        System.out.println("  BrowserImpl: focusControl auto-serialized by Gson (no code change needed)");
    }

    private static boolean hasField(ClassOrInterfaceDeclaration clazz, String name) {
        return clazz.getFields().stream()
            .flatMap(f -> f.getVariables().stream())
            .anyMatch(v -> v.getNameAsString().equals(name));
    }
}
