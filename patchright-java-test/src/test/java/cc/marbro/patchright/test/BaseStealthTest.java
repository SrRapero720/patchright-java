package cc.marbro.patchright.test;

import cc.marbro.patchright.assist.PatchrightManager;
import com.microsoft.playwright.BrowserContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @see BrotectorTest ✅
 * @see FPscannerTest ✅
 * @see CloudflareTest ❌ToDo Check
 * @see CreepJSTest ❌ToDo Check
 */
public class BaseStealthTest {

    static BrowserContext context;

    @BeforeEach
    public void setupContext() {
        context = PatchrightManager.launchPersistentContext("../.data/profiles/StealthEvaluatorTest", false, 100);
    }

    @AfterEach
    public void teardownContext() {
        if (context != null) context.close();
    }


    @AfterAll
    public static void teardownBrowser() {
        PatchrightManager.close();
    }

}
