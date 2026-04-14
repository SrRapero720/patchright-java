package io.github.patchright.test;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StealthEvaluatorTest extends BaseStealthTest {

    @Test
    public void testBrotectorBypass() {
        Page page = context.newPage();

        page.navigate("https://ttlns.github.io/brotector/");

        // Wait for the assessment to populate. We look for rows to exist in the table.
        page.waitForSelector("td");
        page.waitForTimeout(2000); // give it a moment to run async scripts

        // Count elements with class 'false' which indicates a failed stealth check
        int failedTestsCount = (int) page.evaluate("() => document.querySelectorAll('td.false').length");

        // Count elements with class 'true' which indicates a successful bypass
        int passedTestsCount = (int) page.evaluate("() => document.querySelectorAll('td.true').length");

        System.out.println("Brotector Passed checks: " + passedTestsCount);
        System.out.println("Brotector Failed checks: " + failedTestsCount);

        // We expect NO failed checks under stealth mode
        assertTrue(passedTestsCount > 0, "No tests actually executed on Brotector");
        assertTrue(failedTestsCount == 0, "Brotector detected automation! Failure count: " + failedTestsCount);
    }

    @Test
    public void testCloudflareTurnstileBypass() {
        Page page = context.newPage();
        // A commonly known CF validation endpoint
        page.navigate("https://nowsecure.nl");

        try {
            // If CF interrupts us, it usually presents a Turnstile challenge in an iframe or shows 'Just a moment...'.
            // Un-stealthy Playwright hangs indefinitely or gets blocked.
            // A successful bypass loads the target actual content.

            // Wait up to 10 seconds for the main header to become visible
            page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(10000));
            String title = page.title();

            System.out.println("Cloudflare Endpoint Page Title: " + title);
            assertFalse(title.contains("Just a moment"), "Stuck on Cloudflare challenge page.");

        } catch (Exception e) {
            fail("Failed to bypass Cloudflare and load the target page in time. Err: " + e.getMessage());
        }
    }

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
