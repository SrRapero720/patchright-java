package cc.marbro.patchright.test;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class CloudflareTest extends BaseStealthTest {

//    @Test
//    public void testCloudflareTurnstileBypass() {
//        Page page = context.newPage();
//
//        // A commonly known CF validation endpoint
//        page.navigate("https://nowsecure.nl");
//
//        try {
//            // If CF interrupts us, it usually presents a Turnstile challenge in an iframe or shows 'Just a moment...'.
//            // Un-stealthy Playwright hangs indefinitely or gets blocked.
//            // A successful bypass loads the target actual content.
//
//            // Wait up to 10 seconds for the main header to become visible
//            page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(10000));
//            String title = page.title();
//
//            System.out.println("Cloudflare Endpoint Page Title: " + title);
//            assertFalse(title.contains("Just a moment"), "Stuck on Cloudflare challenge page.");
//
//        } catch (Exception e) {
//            fail("Failed to bypass Cloudflare and load the target page in time. Err: " + e.getMessage());
//        }
//    }
}
