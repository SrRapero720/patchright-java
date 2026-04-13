package io.github.patchright.patcher.patches;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Patches Maven POM metadata to rebrand playwright-java as patchright-java.
 * 
 * Changes:
 * - groupId: com.microsoft.playwright -> user-specified groupId
 * - artifactId: playwright -> patchright
 * - Package descriptions and URLs
 */
public class MetadataPatch {

    private static final String OLD_GROUP_ID = "com.microsoft.playwright";
    private static final String OLD_ARTIFACT_ID_PLAYWRIGHT = "<artifactId>playwright</artifactId>";
    private static final String NEW_ARTIFACT_ID_PATCHRIGHT = "<artifactId>patchright</artifactId>";

    public static void apply(Path playwrightPath, String newGroupId, String driverVersion, String packageVersion)
        throws IOException {

        // Patch all pom.xml files
        patchPomFiles(playwrightPath, newGroupId);

        // Patch version if specified
        if (packageVersion != null) {
            patchVersion(playwrightPath, packageVersion);
        }
    }

    private static void patchPomFiles(Path playwrightPath, String newGroupId) throws IOException {
        Files.walkFileTree(playwrightPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("pom.xml")) {
                    patchPomFile(file, newGroupId);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Skip hidden directories
                if (dir.getFileName().toString().startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void patchPomFile(Path pomPath, String newGroupId) throws IOException {
        String content = new String(Files.readAllBytes(pomPath), StandardCharsets.UTF_8);
        String original = content;

        // Replace groupId
        content = content.replace(
            "<groupId>" + OLD_GROUP_ID + "</groupId>",
            "<groupId>" + newGroupId + "</groupId>");

        // Replace main artifactId (playwright -> patchright)
        content = content.replace(OLD_ARTIFACT_ID_PLAYWRIGHT, NEW_ARTIFACT_ID_PATCHRIGHT);

        // Replace driver-bundle artifactId
        content = content.replace(
            "<artifactId>driver-bundle</artifactId>",
            "<artifactId>patchright-driver-bundle</artifactId>");

        // Replace driver artifactId
        content = content.replace(
            "<artifactId>driver</artifactId>",
            "<artifactId>patchright-driver</artifactId>");

        // Update name and description
        content = content.replace(
            "<name>Playwright</name>",
            "<name>Patchright</name>");
        content = content.replace(
            "<name>playwright</name>",
            "<name>patchright</name>");

        // Update description
        content = content.replace(
            "Java library to automate",
            "Undetected Java library to automate");

        // Update URLs
        content = content.replace(
            "https://github.com/microsoft/playwright-java",
            "https://github.com/Kaliiiiiiiiii-Vinyzu/patchright-java");

        if (!content.equals(original)) {
            Files.write(pomPath, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("  Patched: " + pomPath);
        }
    }

    private static void patchVersion(Path playwrightPath, String version) throws IOException {
        // The version is typically in the root pom.xml
        Path rootPom = playwrightPath.resolve("pom.xml");
        if (!rootPom.toFile().exists()) return;

        String content = new String(Files.readAllBytes(rootPom), StandardCharsets.UTF_8);

        // Replace the first <version> after the <artifactId> in the project declaration
        // This is fragile but matches the typical Maven POM structure
        content = content.replaceFirst(
            "(<artifactId>patchright</artifactId>\\s*<version>)[^<]+(</version>)",
            "$1" + version + "$2");

        Files.write(rootPom, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("  Patched version to: " + version);
    }
}
