package cc.marbro.patchright.test;

import cc.marbro.patchright.assist.PatchrightUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrotectorTest extends BaseStealthTest {

    @Test
    public void testBrotectorBypass() {
        Page page = context.newPage();

        PatchrightUtil.navigateWarmup(page, "https://ttlns.github.io/brotector/");

        PatchrightUtil.click(page, page.locator("#clickHere"));

        PatchrightUtil.randomSleepMs(1000, 3000);

        PatchrightUtil.wheelDown(page, 200);

        String avgMsLoad = page.locator("#avg-ms-load").innerText();
        String avgScore = page.locator("#avg-score").innerText();

        System.out.println("Average Load Time: " + avgMsLoad + ", Average Score: " + avgScore);
        assertEquals("0", avgScore, " Brotector detected the bot with a non-zero score.");

    }
}
