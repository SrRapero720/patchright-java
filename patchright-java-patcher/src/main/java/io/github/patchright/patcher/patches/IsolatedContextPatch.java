package io.github.patchright.patcher.patches;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import io.github.patchright.patcher.assist.PatchUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Adds the isolatedContext parameter to all evaluate/evaluateHandle methods.
 * 
 * This allows users to control whether JavaScript is executed in the main
 * world or an isolated utility world (default: utility/isolated to avoid detection).
 * 
 * Pattern for implementation classes (e.g., FrameImpl, JSHandleImpl):
 * - Add boolean isolatedContext parameter with default value true
 * - Change "world" property from hardcoded "main" to conditional on isolatedContext
 * - Add overload that delegates with default isolatedContext=true
 * 
 * Pattern for interface classes (e.g., Frame, JSHandle):
 * - Add new method signature with isolatedContext parameter
 * - Add default method overload without the parameter
 * 
 * Pattern for delegation classes (e.g., PageImpl, LocatorImpl):
 * - Add isolatedContext parameter and pass through to delegate
 */
public class IsolatedContextPatch {

    public static void apply(Path playwrightPath) throws IOException {
        Path implDir = playwrightPath.resolve("playwright/src/main/java/com/microsoft/playwright/impl");
        Path apiDir = playwrightPath.resolve("playwright/src/main/java/com/microsoft/playwright");

        // Patch implementation classes that have "world" parameter
        patchImplClass(implDir.resolve("FrameImpl.java"), "FrameImpl",
            new String[]{"evaluate", "evaluateHandle"});
        patchImplClass(implDir.resolve("JSHandleImpl.java"), "JSHandleImpl",
            new String[]{"evaluate", "evaluateHandle"});
        patchWorkerImpl(implDir.resolve("WorkerImpl.java"));

        // Patch delegation classes
        patchPageImpl(implDir.resolve("PageImpl.java"));
        patchLocatorImpl(implDir.resolve("LocatorImpl.java"));

        // Patch interfaces
        patchInterface(apiDir.resolve("Frame.java"), "Frame",
            new String[]{"evaluate", "evaluateHandle"});
        patchInterface(apiDir.resolve("JSHandle.java"), "JSHandle",
            new String[]{"evaluate", "evaluateHandle"});
        patchInterface(apiDir.resolve("Worker.java"), "Worker",
            new String[]{"evaluate", "evaluateHandle"});
        patchInterface(apiDir.resolve("Page.java"), "Page",
            new String[]{"evaluate", "evaluateHandle"});
        patchLocatorInterface(apiDir.resolve("Locator.java"));
    }

    /**
     * Patch an implementation class that directly sets params.addProperty("world", "main").
     * Changes the existing method to accept isolatedContext and use it for world selection.
     * Adds a backward-compatible overload.
     */
    private static void patchImplClass(Path filePath, String className, String[] methodNames) throws IOException {
        CompilationUnit cu = PatchUtil.parsePreserving(filePath);

        for (String methodName : methodNames) {
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class, m ->
                m.getNameAsString().equals(methodName) &&
                m.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(c -> c.getNameAsString().equals(className)).orElse(false));

            for (MethodDeclaration method : methods) {
                if (hasIsolatedContextParam(method)) continue;

                // Add isolatedContext parameter
                method.addParameter(new Parameter(PrimitiveType.booleanType(), "isolatedContext"));

                // Replace: params.addProperty("world", "main")
                // With:    params.addProperty("world", isolatedContext ? "utility" : "main")
                replaceWorldProperty(method);

                // Remove @Override since the original interface method doesn't have this param yet
                method.getAnnotationByName("Override").ifPresent(a -> a.remove());

                // Add backward-compatible overload
                ClassOrInterfaceDeclaration parentClass = method.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addImplOverload(parentClass, method);

                System.out.println("  Patched: " + className + "." + methodName + "()");
            }
        }

        PatchUtil.writeCompilationUnit(cu, filePath);
    }

    /**
     * WorkerImpl doesn't have "world" property, but we still add isolatedContext
     * to pass through to the protocol.
     */
    private static void patchWorkerImpl(Path filePath) throws IOException {
        CompilationUnit cu = PatchUtil.parsePreserving(filePath);
        String className = "WorkerImpl";

        for (String methodName : new String[]{"evaluate", "evaluateHandle"}) {
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class, m ->
                m.getNameAsString().equals(methodName) &&
                m.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(c -> c.getNameAsString().equals(className)).orElse(false));

            for (MethodDeclaration method : methods) {
                if (hasIsolatedContextParam(method)) continue;

                method.addParameter(new Parameter(PrimitiveType.booleanType(), "isolatedContext"));
                method.getAnnotationByName("Override").ifPresent(a -> a.remove());

                // Add: params.addProperty("world", isolatedContext ? "utility" : "main");
                // after the "expression" line
                method.getBody().ifPresent(body -> {
                    // Find the position after "expression" addProperty and insert world property
                    NodeList<Statement> stmts = body.getStatements();
                    int insertIdx = -1;
                    for (int i = 0; i < stmts.size(); i++) {
                        String stmtStr = stmts.get(i).toString();
                        if (stmtStr.contains("addProperty") && stmtStr.contains("expression")) {
                            insertIdx = i + 1;
                            break;
                        }
                    }
                    if (insertIdx >= 0) {
                        Statement worldStmt = StaticJavaParser.parseStatement(
                            "params.addProperty(\"world\", isolatedContext ? \"utility\" : \"main\");");
                        stmts.add(insertIdx, worldStmt);
                    }
                });

                ClassOrInterfaceDeclaration parentClass = method.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addImplOverload(parentClass, method);

                System.out.println("  Patched: " + className + "." + methodName + "()");
            }
        }

        PatchUtil.writeCompilationUnit(cu, filePath);
    }

    /**
     * PageImpl delegates to mainFrame - add isolatedContext param and pass through.
     */
    private static void patchPageImpl(Path filePath) throws IOException {
        CompilationUnit cu = PatchUtil.parsePreserving(filePath);
        String className = "PageImpl";

        for (String methodName : new String[]{"evaluate", "evaluateHandle"}) {
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class, m ->
                m.getNameAsString().equals(methodName) &&
                m.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(c -> c.getNameAsString().equals(className)).orElse(false));

            for (MethodDeclaration method : methods) {
                if (hasIsolatedContextParam(method)) continue;

                method.addParameter(new Parameter(PrimitiveType.booleanType(), "isolatedContext"));
                method.getAnnotationByName("Override").ifPresent(a -> a.remove());

                // Replace body: delegate to mainFrame with isolatedContext
                method.getBody().ifPresent(body -> {
                    String bodyStr = body.toString();
                    // e.g., "return mainFrame.evaluate(expression, arg);"
                    // becomes "return mainFrame.evaluate(expression, arg, isolatedContext);"
                    for (Statement stmt : body.getStatements()) {
                        stmt.findAll(MethodCallExpr.class).forEach(call -> {
                            String calledName = call.getNameAsString();
                            if (calledName.equals(methodName)) {
                                call.addArgument(new NameExpr("isolatedContext"));
                            }
                        });
                    }
                });

                ClassOrInterfaceDeclaration parentClass = method.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addImplOverload(parentClass, method);

                System.out.println("  Patched: " + className + "." + methodName + "()");
            }
        }

        PatchUtil.writeCompilationUnit(cu, filePath);
    }

    /**
     * LocatorImpl.evaluate delegates to ElementHandle via withElement callback.
     * We add isolatedContext and pass it through.
     */
    private static void patchLocatorImpl(Path filePath) throws IOException {
        CompilationUnit cu = PatchUtil.parsePreserving(filePath);
        String className = "LocatorImpl";

        for (String methodName : new String[]{"evaluate", "evaluateHandle"}) {
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class, m ->
                m.getNameAsString().equals(methodName) &&
                m.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(c -> c.getNameAsString().equals(className)).orElse(false));

            for (MethodDeclaration method : methods) {
                if (hasIsolatedContextParam(method)) continue;

                method.addParameter(new Parameter(PrimitiveType.booleanType(), "isolatedContext"));
                method.getAnnotationByName("Override").ifPresent(a -> a.remove());

                // For evaluate/evaluateHandle: the withElement callback calls h.evaluate/evaluateHandle
                // For evaluateAll: calls frame.evalOnSelectorAll
                // Add isolatedContext to those inner calls
                method.getBody().ifPresent(body -> {
                    body.findAll(MethodCallExpr.class).forEach(call -> {
                        String calledName = call.getNameAsString();
                        if (calledName.equals("evaluate") || calledName.equals("evaluateHandle")
                            || calledName.equals("evalOnSelectorAll")) {
                            call.addArgument(new NameExpr("isolatedContext"));
                        }
                    });
                });

                ClassOrInterfaceDeclaration parentClass = method.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addImplOverload(parentClass, method);

                System.out.println("  Patched: " + className + "." + methodName + "()");
            }
        }

        PatchUtil.writeCompilationUnit(cu, filePath);
    }

    /**
     * Add isolatedContext to public API interface methods.
     * Uses text-based replacement to avoid LexicalPreservingPrinter's setDefault() bug.
     */
    private static void patchInterface(Path filePath, String interfaceName, String[] methodNames) throws IOException {
        String content = PatchUtil.readFile(filePath);

        for (String methodName : methodNames) {
            String returnType = methodName.equals("evaluateHandle") ? "JSHandle" : "Object";
            String abstractDecl = returnType + " " + methodName + "(String expression, Object arg);";
            
            if (content.contains(abstractDecl) && !content.contains(methodName + "(String expression, Object arg, boolean isolatedContext)")) {
                String newAbstractDecl = returnType + " " + methodName + "(String expression, Object arg, boolean isolatedContext);";
                String defaultDecl = "default " + returnType + " " + methodName + "(String expression, Object arg) {\n"
                    + "    return " + methodName + "(expression, arg, true);\n"
                    + "  }";
                content = content.replace(abstractDecl, defaultDecl + "\n  " + newAbstractDecl);
                System.out.println("  Patched interface: " + interfaceName + "." + methodName + "()");
            }
        }

        PatchUtil.writeFile(filePath, content);
    }

    /**
     * Locator interface has different signature - uses text-based replacement.
     */
    private static void patchLocatorInterface(Path filePath) throws IOException {
        String content = PatchUtil.readFile(filePath);
        String interfaceName = "Locator";

        for (String methodName : new String[]{"evaluate", "evaluateHandle"}) {
            String returnType = methodName.equals("evaluateHandle") ? "JSHandle" : "Object";
            String abstractDecl = returnType + " " + methodName + "(String expression, Object arg, EvaluateOptions options);";

            if (content.contains(abstractDecl) && !content.contains(methodName + "(String expression, Object arg, EvaluateOptions options, boolean isolatedContext)")) {
                String newAbstractDecl = returnType + " " + methodName + "(String expression, Object arg, EvaluateOptions options, boolean isolatedContext);";
                String defaultDecl = "default " + returnType + " " + methodName + "(String expression, Object arg, EvaluateOptions options) {\n"
                    + "    return " + methodName + "(expression, arg, options, true);\n"
                    + "  }";
                content = content.replace(abstractDecl, defaultDecl + "\n  " + newAbstractDecl);
                System.out.println("  Patched interface: " + interfaceName + "." + methodName + "()");
            }
        }

        PatchUtil.writeFile(filePath, content);
    }

    // --- Helper methods ---

    private static boolean hasIsolatedContextParam(MethodDeclaration method) {
        return method.getParameters().stream()
            .anyMatch(p -> p.getNameAsString().equals("isolatedContext"));
    }

    /**
     * Replace params.addProperty("world", "main") with
     * params.addProperty("world", isolatedContext ? "utility" : "main")
     */
    private static void replaceWorldProperty(MethodDeclaration method) {
        method.getBody().ifPresent(body -> {
            body.findAll(MethodCallExpr.class).forEach(call -> {
                if (call.getNameAsString().equals("addProperty") && call.getArguments().size() == 2) {
                    Expression firstArg = call.getArgument(0);
                    if (firstArg.isStringLiteralExpr() &&
                        firstArg.asStringLiteralExpr().getValue().equals("world")) {
                        // Replace second argument with conditional
                        call.setArgument(1, new ConditionalExpr(
                            new NameExpr("isolatedContext"),
                            new StringLiteralExpr("utility"),
                            new StringLiteralExpr("main")
                        ));
                    }
                }
            });
        });
    }

    /**
     * Add a backward-compatible overload that calls the new method with isolatedContext=true.
     */
    private static void addImplOverload(ClassOrInterfaceDeclaration parentClass, MethodDeclaration patchedMethod) {
        MethodDeclaration overload = new MethodDeclaration();
        overload.setName(patchedMethod.getNameAsString());
        overload.setType(patchedMethod.getType().clone());
        overload.addAnnotation("Override");
        overload.setModifiers(Modifier.Keyword.PUBLIC);

        // Copy all parameters except isolatedContext
        for (Parameter param : patchedMethod.getParameters()) {
            if (!param.getNameAsString().equals("isolatedContext")) {
                overload.addParameter(param.clone());
            }
        }

        // Create body: return this.methodName(param1, param2, ..., true);
        BlockStmt body = new BlockStmt();
        MethodCallExpr call = new MethodCallExpr(patchedMethod.getNameAsString());
        for (Parameter param : overload.getParameters()) {
            call.addArgument(new NameExpr(param.getNameAsString()));
        }
        call.addArgument(new BooleanLiteralExpr(true)); // default isolatedContext = true

        if (patchedMethod.getType().isVoidType()) {
            body.addStatement(new ExpressionStmt(call));
        } else {
            body.addStatement(new ReturnStmt(call));
        }
        overload.setBody(body);

        // Add after the patched method
        int idx = parentClass.getMembers().indexOf(patchedMethod);
        parentClass.getMembers().add(idx + 1, overload);
    }
}
