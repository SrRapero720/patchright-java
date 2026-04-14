package cc.marbro.patchright.assist;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PatchrightManager {

    /**
     * Playwright : 物理上的浏览器实例（如 Chromium.exe）。它是最重的对象，启动开销最大。
     * BrowserContext : 核心概念。每个 Context 拥有独立的 Cookie、LocalStorage 和 Session 数据，是实现多账号的核心
     * Page : 在同一个 Context 下打开的标签页。它们共享该 Context 的登录状态。
     */
    private static Playwright playwright;

    /**
     * 持久化上下文参数配置，包含一些反检测参数，提升兼容性
     *
     * @see com.microsoft.playwright.BrowserType.LaunchPersistentContextOptions
     */
    private static BrowserType.LaunchPersistentContextOptions newPersistentContextOptions(boolean headless, int slowMo) {

        return new BrowserType.LaunchPersistentContextOptions()
                .setChannel("chrome")
                // 无头模式通常在调试时设置为 false 来观察操作过程
                .setHeadless(headless)
                // 每个操作之间的延迟，单位毫秒，调试时可以设置为 100-500 来观察操作过程
                .setSlowMo(slowMo)
                // 设为 null 支持手动调节，默认是一个 1280x720 固定窗口，且无法设置大小
                .setViewportSize(null)
                //
                //  使用 patchright 的时候以下可选
                //
                // https://peter.sh/experiments/chromium-command-line-switches/
                .setArgs(Arrays.asList(
                        // 核心：禁用 Blink 引擎的自动化控制特征。Blink 引擎（Chrome 的核心）内部有一个标志位。
                        // 当浏览器被自动化工具（如 WebDriver）控制时，它会默认将 JavaScript 变量 navigator.webdriver 设置为 true
                        "--disable-blink-features=AutomationControlled",
                        // Chrome 在 Linux/Mac 上通常会尝试访问系统的密码管理器（如 Keychain）
                        "--use-mock-keychain",
                        // 同样常用于 Docker 环境，防止因内存共享限制导致的浏览器崩溃，确保爬虫运行稳定
                        "--disable-dev-shm-usage"
                ))
                // 忽略的默认参数
                // https://github.com/Kaliiiiiiiiii-Vinyzu/patchright-python?tab=readme-ov-file#command-flags-leaks
                .setIgnoreDefaultArgs(Arrays.asList(
                        // 避免 navigator.webdriver 检测
                        "--enable-automation",
                        // 以避免弹出窗口崩溃
                        "--disable-popup-blocking",
                        // 以避免被检测为隐形驾驶员
                        "--disable-component-update",
                        // 以启用默认应用程序
                        "--disable-default-apps",
                        // 以启用扩展程序
                        "--disable-extensions"
                ));
    }

    /**
     * @param profileDir 每个 BrowserContext 拥有独立的 Cookie、LocalStorage 和 Session 数据
     * @param headless   是否无头模式，通常在调试时设置为 false 来观察操作过程
     * @param slowMo     每个操作之间的延迟，单位毫秒，调试时可以设置为 100-500 来观察操作过程
     */
    public static BrowserContext launchPersistentContext(String profileDir, boolean headless, int slowMo) {
        if (playwright == null) {
            playwright = Playwright.create();
        }
        //
        BrowserType.LaunchPersistentContextOptions options = newPersistentContextOptions(headless, slowMo);
        //
        return playwright.chromium().launchPersistentContext(Paths.get(profileDir), options);
    }

    public static void closeContext(BrowserContext context) {
        if (context != null) {
            context.close();
        }
    }

    public static void close() {
        if (playwright != null) {
            playwright.close();
        }
        playwright = null;
    }
}
