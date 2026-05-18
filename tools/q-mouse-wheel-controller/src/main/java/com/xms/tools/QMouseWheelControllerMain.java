package com.xms.tools;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 启动后监听全局键盘，用上/下方向键模拟鼠标滚轮。
 *
 * <p>运行 main 方法后生效；关闭 main 进程后会注销全局键盘监听并失效。</p>
 */
public class QMouseWheelControllerMain implements NativeKeyListener {

    private static final int WHEEL_STEP = 1;
    private static final long SCROLL_INTERVAL_MS = 120L;

    private final Robot robot;
    private final ScheduledExecutorService wheelExecutor;
    private volatile boolean upPressed;
    private volatile boolean downPressed;

    public QMouseWheelControllerMain() throws AWTException {
        this.robot = new Robot();
        this.wheelExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "q-mouse-wheel-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 程序入口。启动后保持阻塞，让全局键盘监听持续工作qq。
     *
     *q @param args 启动参数，当前未使用
     * @throws Exception 初始化全局键盘监听或 Robot 失败时抛出
     */
    public static void main(String[] args) throws Exception {
        closeJNativeHookLog();

        QMouseWheelControllerMain controller = new QMouseWheelControllerMain();
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(controller);
        controller.startWheelWorker();

        Runtime.getRuntime().addShutdownHook(new Thread(controller::shutdown, "q-mouse-wheel-shutdown"));

        System.out.println("已启动：按住 ↑ 向上滚动，按住 ↓ 向下滚动。关闭 main 后失效。");
        new CountDownLatch(1).await();
    }

    /**
     * 持续检查按键状态。main 运行期间，只要按住上/下方向键就模拟鼠标滚轮。
     */
    private void startWheelWorker() {
        wheelExecutor.scheduleAtFixedRate(() -> {
            if (upPressed && !downPressed) {
                robot.mouseWheel(-WHEEL_STEP);
            } else if (downPressed && !upPressed) {
                robot.mouseWheel(WHEEL_STEP);
            }
        }, 0L, SCROLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭后台线程并注销全局键盘监听，确保 main 退出后快捷键不再生效。
     */
    private void shutdown() {
        wheelExecutor.shutdownNow();
        GlobalScreen.removeNativeKeyListener(this);
        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (NativeHookException ignored) {
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        updateKeyState(event.getKeyCode(), true);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        updateKeyState(event.getKeyCode(), false);
    }

    /**
     * 记录当前方向键状态。只关心上方向键、下方向键。
     *
     * @param keyCode JNativeHook 键码
     * @param pressed true 表示按下，false 表示松开
     */
    private void updateKeyState(int keyCode, boolean pressed) {
        if (keyCode == NativeKeyEvent.VC_UP) {
            upPressed = pressed;
        } else if (keyCode == NativeKeyEvent.VC_DOWN) {
            downPressed = pressed;
        }
    }

    /**
     * 关闭 JNativeHook 默认日志，避免控制台持续输出底层监听信息。
     */
    private static void closeJNativeHookLog() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
    }
}
