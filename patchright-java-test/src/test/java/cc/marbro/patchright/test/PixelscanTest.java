package cc.marbro.patchright.test;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

public class PixelscanTest extends BaseStealthTest {

    @Test
    public void testCreepJSLoad() {
        Page page = context.newPage();

        // CreepJS is an exhaustive framework. We ensure the script doesn't crash identifying generic bots.
        page.navigate("https://pixelscan.net/bot-check");

        page.pause();

    }
}
