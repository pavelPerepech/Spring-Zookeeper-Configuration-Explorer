package ru.zsc.util;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.TerminalFactory;
import java.util.Objects;

/**
 * Created by Pavel Perepech on 01/07/2021.
 */
public class AppUICtx {

    private static volatile AppUICtx instance;

    private final TerminalFactory terminalFactory;

    private final Screen screen;

    private final WindowBasedTextGUI windowBasedTextGUI;

    private AppUICtx(TerminalFactory terminalFactory, Screen screen,
            WindowBasedTextGUI windowBasedTextGUI) {
        this.terminalFactory = terminalFactory;
        this.screen = screen;
        this.windowBasedTextGUI = windowBasedTextGUI;
    }

    public static AppUICtx init(
            final TerminalFactory terminalFactory,
            final Screen screen,
            final WindowBasedTextGUI windowBasedTextGUI) {
        if (Objects.nonNull(instance)) {
            throw new IllegalStateException("Application UI Context has been already initialized");
        }

        instance = new AppUICtx(terminalFactory, screen, windowBasedTextGUI);
        return instance;
    }

    public static AppUICtx get() {
        return Objects.requireNonNull(instance, "Context is not initialized");
    }

    public TerminalFactory getTerminalFactory() {
        return terminalFactory;
    }

    public Screen getScreen() {
        return screen;
    }

    public WindowBasedTextGUI getWindowBasedTextGUI() {
        return windowBasedTextGUI;
    }
}
