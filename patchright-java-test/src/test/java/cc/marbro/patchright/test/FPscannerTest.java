package cc.marbro.patchright.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class FPscannerTest extends BaseStealthTest {

    @Test
    public void test() {
        Page page = context.newPage();

        page.navigate("https://fpscanner.com/demo/");

        page.waitForSelector("#fsid-content:has-text('Bot')", new Page.WaitForSelectorOptions().setTimeout(15000));

        String resultJson = page.locator("#json-content").first().innerText();

        //
        JsonObject jsonObject = new Gson().fromJson(resultJson, JsonObject.class);
        boolean fastBotDetection = jsonObject.get("fastBotDetection").getAsBoolean();

        //
        if (fastBotDetection) {
            JsonObject fastBotDetectionDetails = jsonObject.getAsJsonObject("fastBotDetectionDetails");
            fastBotDetectionDetails.asMap().forEach((key, value) -> {
                boolean detected = value.getAsJsonObject().get("detected").getAsBoolean();
                if (detected) {
                    System.out.println("Fast Bot Detection: " + key + " = " + value);
                }
            });
            System.out.println("Full Json: " + resultJson);
        }

        assertFalse(fastBotDetection, "Stuck on fpscanner.com challenge page.");

    }
}
