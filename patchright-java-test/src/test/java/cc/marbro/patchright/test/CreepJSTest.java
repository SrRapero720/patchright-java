package cc.marbro.patchright.test;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class CreepJSTest extends BaseStealthTest {

    @Test
    public void testCreepJSLoad() {
        Page page = context.newPage();

        // CreepJS is an exhaustive framework. We ensure the script doesn't crash identifying generic bots.
        page.navigate("https://abrahamjuliot.github.io/creepjs/");

        try {
            // Wait for trust score element
            page.waitForSelector(".trust-score", new Page.WaitForSelectorOptions().setTimeout(15000));
            String score = (String) page.locator(".trust-score").first().innerText();
            System.out.println("CreepJS Trust Score: " + score);

            // We just assert that it loaded properly and yielded a score
            assertFalse(score.isEmpty(), "CreepJS trust score was empty or inaccessible.");
        } catch (Exception e) {
            fail("CreepJS failed to evaluate the browser properly. Err: " + e.getMessage());
        }
    }
}
