package ru.zsc.ui;

import com.googlecode.lanterna.gui2.AnimatedLabel;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import java.util.Arrays;

/**
 * Created by Pavel Perepech
 * {@link https://github.com/mabe02/lanterna/blob/master/src/main/java/com/googlecode/lanterna/gui2/dialogs/WaitingDialog.java}.
 */
public class WaitingDialog extends DialogWindow {


    private WaitingDialog(String title, String text) {
        super(title);
        setHints(Arrays.asList(Hint.CENTERED));

        Panel mainPanel = Panels.horizontal(
                new Label(text),
                AnimatedLabel.createClassicSpinningLine());
        setComponent(mainPanel);
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        showDialog(textGUI, true);
        return null;
    }

    /**
     * Displays the waiting dialog and optionally blocks until another thread closes it
     * @param textGUI GUI to add the dialog to
     * @param blockUntilClosed If {@code true}, the method call will block until another thread calls {@code close()} on
     *                         the dialog, otherwise the method call returns immediately
     */
    public void showDialog(WindowBasedTextGUI textGUI, boolean blockUntilClosed) {
        textGUI.addWindow(this);

        if(blockUntilClosed) {
            //Wait for the window to close, in case the window manager doesn't honor the MODAL hint
            waitUntilClosed();
        }
    }

    /**
     * Creates a new waiting dialog
     * @param title Title of the waiting dialog
     * @param text Text to display on the waiting dialog
     * @return Created waiting dialog
     */
    public static WaitingDialog createDialog(String title, String text) {
        return new WaitingDialog(title, text);
    }

    /**
     * Creates and displays a waiting dialog without blocking for it to finish
     * @param textGUI GUI to add the dialog to
     * @param title Title of the waiting dialog
     * @param text Text to display on the waiting dialog
     * @return Created waiting dialog
     */
    public static WaitingDialog showDialog(WindowBasedTextGUI textGUI, String title, String text) {
        WaitingDialog waitingDialog = createDialog(title, text);
        waitingDialog.showDialog(textGUI, false);
        return waitingDialog;
    }

    @Override
    public void draw(TextGUIGraphics graphics) {
        super.draw(graphics);
    }
}
