package ru.zsc;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;
import ru.zsc.ui.MainAppWindow;
import ru.zsc.util.AppUICtx;
import ru.zsc.util.ArgumentParser;
import ru.zsc.util.ComponentFactory;

/**
 * Created by Pavel Perepech
 */
public class ZooSpringConfigExplorer {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Use with parameters:");
            ArgumentParser.printOptionsHelp();
        }

        final ArgumentParser argumentParser = new ArgumentParser(args);
        ComponentFactory.init(argumentParser);
        ComponentFactory.getInstance().zooAccessor();

        final DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();
            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

            AppUICtx.init(terminalFactory, screen, textGUI);

            textGUI.addWindowAndWait(new MainAppWindow());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
