package io.github.patchright.patcher.patches;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.expr.NameExpr;
import io.github.patchright.patcher.assist.PatchUtil;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Removes sourceURL annotations from scripts to avoid automation detection.
 * 
 * In the original Playwright, scripts get a sourceURL comment appended,
 * which can reveal automation. Patchright removes this.
 */
public class ScriptInjectionPatch {

    public static void apply(Path playwrightPath) throws IOException {
        patchAddSourceUrlToScript(playwrightPath);
    }

    /**
     * Patches Utils.addSourceUrlToScript() to simply return the source unchanged.
     * 
     * Original:
     *   static String addSourceUrlToScript(String source, Path path) {
     *     return source + "\n//# sourceURL=" + path.toString().replace("\n", "");
     *   }
     * 
     * Patched:
     *   static String addSourceUrlToScript(String source, Path path) {
     *     return source;
     *   }
     */
    private static void patchAddSourceUrlToScript(Path playwrightPath) throws IOException {
        Path utilsPath = playwrightPath.resolve(
            "playwright/src/main/java/com/microsoft/playwright/impl/Utils.java");

        CompilationUnit cu = PatchUtil.parsePreserving(utilsPath);

        cu.findAll(MethodDeclaration.class).stream()
            .filter(m -> m.getNameAsString().equals("addSourceUrlToScript"))
            .forEach(method -> {
                // Replace body with just "return source;"
                BlockStmt newBody = new BlockStmt();
                newBody.addStatement(new ReturnStmt(new NameExpr("source")));
                method.setBody(newBody);
            });

        PatchUtil.writeCompilationUnit(cu, utilsPath);
        System.out.println("  Patched: Utils.addSourceUrlToScript()");
    }
}
