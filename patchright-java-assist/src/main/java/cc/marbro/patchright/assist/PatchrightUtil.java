package cc.marbro.patchright.assist;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.Random;

/**
 * Utility class to simulate human-like interactions with Playwright.
 */
public class PatchrightUtil {

    private static final Random RANDOM = new Random();

    /**
     * Warmup a page to trigger background scripts without immediately jumping into the action
     */
    public static void navigateWarmup(Page page, String url) {

        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        //
        randomSleepMs(200, 500);
        //
        page.mouse().move(
                300 + RANDOM.nextDouble() * 400,
                200 + RANDOM.nextDouble() * 200
        );
        //
        randomSleepMs(500, 1000);
    }

    /**
     * Random delay between min and max ms
     */
    public static void randomSleepMs(int min, int max) {
        int ms = RANDOM.nextInt(max - min) + min;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void clearAndType(Locator locator, String text) {
        // 点击输入框以获得焦点
        locator.click();

        // 全选： macOS 系统使用 Meta (Command) + A，Windows/Linux 使用 Control + A
        String modifier = System.getProperty("os.name").toLowerCase().contains("mac") ? "Meta" : "Control";
        locator.press(modifier + "+A");

        // 直接输入指定值
        typer(locator, text);
    }

    /**
     * Simulate human typing with varying delays
     */
    public static void typer(Locator locator, String text) {
        for (char c : text.toCharArray()) {
            int delay = RANDOM.nextInt(100) + 50;
            locator.pressSequentially(String.valueOf(c), new Locator.PressSequentiallyOptions().setDelay(delay));
        }
    }

    /**
     * Simulate human mouse movement and click
     */
    public static void click(Page page, Locator locator) {
        BoundingBox box = locator.boundingBox();
        if (box == null) {
            locator.click();
            return;
        }

        double x = box.x + box.width * (0.3 + RANDOM.nextDouble() * 0.4);
        double y = box.y + box.height * (0.3 + RANDOM.nextDouble() * 0.4);

        int steps = RANDOM.nextInt(5) + 3;
        page.mouse().move(x, y, new com.microsoft.playwright.Mouse.MoveOptions().setSteps(steps));

        randomSleepMs(100, 300);

        page.mouse().click(x, y);
    }

    /**
     * 鼠标往下滚动一定距离，模拟用户滚轮操作
     */
    public static void wheelDown(Page page, double px) {
        page.mouse().wheel(0, 500);
    }


}
