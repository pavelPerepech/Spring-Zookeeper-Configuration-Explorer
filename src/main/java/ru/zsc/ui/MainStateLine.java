package ru.zsc.ui;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy;
import com.googlecode.lanterna.gui2.Panel;

/**
 * Created by Pavel Perepech.
 */
public class MainStateLine extends Panel {
    private final static TextColor KEY_BACKGROUND_COLOR = ANSI.WHITE_BRIGHT;
    private final static TextColor KEY_FOREGROUND_COLOR = ANSI.BLACK;
    private final static TextColor MEAN_BACKGROUND_COLOR = ANSI.WHITE;
    private final static TextColor MEAN_FOREGROUND_COLOR = ANSI.BLACK_BRIGHT;
    private final static TextColor SPACE_BACKGROUND_COLOR = ANSI.WHITE;
    private final static TextColor SPACE_FOREGROUND_COLOR = ANSI.WHITE;

    public MainStateLine() {
        super(new LinearLayout(Direction.HORIZONTAL));

        add(keyLabel(Symbols.ARROW_UP + "/" + Symbols.ARROW_DOWN));
        add(meanLabel("Move Up/Down"));
        add(spcLabel());
        add(keyLabel("ENTER"));
        add(meanLabel("Go"));
        add(spcLabel());
        add(keyLabel("F4"));
        add(meanLabel("Edit as properties"));
        add(spcLabel());
        add(keyLabel("ESC"));
        add(meanLabel("Exit"));
    }

    private void add(final Component component) {
        addComponent(component, LinearLayout.createLayoutData(Alignment.Beginning, GrowPolicy.None));
    }

    private Label keyLabel(final String text) {
        final Label result = new Label(text);
        result.setBackgroundColor(KEY_BACKGROUND_COLOR);
        result.setForegroundColor(KEY_FOREGROUND_COLOR);

        return result;
    }

    private Label meanLabel(final String text) {
        final Label result = new Label(text);
        result.setBackgroundColor(MEAN_BACKGROUND_COLOR);
        result.setForegroundColor(MEAN_FOREGROUND_COLOR);

        return result;
    }

    private Label spcLabel() {
        final Label result = new Label("");
        result.setBackgroundColor(SPACE_BACKGROUND_COLOR);
        result.setForegroundColor(SPACE_FOREGROUND_COLOR);

        return result;
    }
}
