package cc.marbro.patchright.test;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrotectorTest extends BaseStealthTest {

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
}
