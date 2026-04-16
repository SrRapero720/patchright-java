package cc.marbro.patchright.test;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PixelscanTest extends BaseStealthTest {

    @Test
    public void testPixelscan() {
        Page page = context.newPage();

        // CreepJS is an exhaustive framework. We ensure the script doesn't crash identifying generic bots.
        page.navigate("https://pixelscan.net/bot-check");

        ElementHandle elementHandle = page.waitForSelector(".checker-header");
        String state = elementHandle.getAttribute("state");

        System.out.println("PixelscanTest: " + state);
        assertEquals("success", state, "pixelscan.net detected the bot, which should not happen.");
    }
}
