package ru.zsc.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.Style;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by Pavel Perepech.
 */
public class PropertyDialog extends BasicWindow {


    public static class StoreContext {
        private final String path;

        private final List<String> excludedHolder;

        private final PropertyDialog self;

        public StoreContext(String path, List<String> excludedHolder, PropertyDialog self) {
            this.path = path;
            this.excludedHolder = excludedHolder;
            this.self = self;
        }

        public String getPath() {
            return path;
        }

        public List<String> getExcludedHolder() {
            return excludedHolder;
        }

        public PropertyDialog getSelf() {
            return self;
        }
    }

    private final List<String> excludedHolder;

    private final Consumer<StoreContext> onOk;

    private TextBox propertiesTextBox;

    private final String path;

    public PropertyDialog(
            final String path,
            final String value,
            final List<String> excludedHolder,
            final Consumer<StoreContext> onOk) {
        super(path);
        this.path = path;
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.MODAL));

        this.excludedHolder = excludedHolder;
        this.onOk = onOk;

        final LayoutManager layoutManager = new BorderLayout();
        final Panel contentPanel = new Panel(layoutManager);

        propertiesTextBox = Objects.isNull(value) ? new TextBox("", Style.MULTI_LINE): new TextBox(value, Style.MULTI_LINE);
        propertiesTextBox.setReadOnly(false);
        contentPanel.addComponent(propertiesTextBox, Location.CENTER);

        final Panel btnPanel = new Panel(new BorderLayout());
        final Panel subBtnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        contentPanel.addComponent(btnPanel, Location.BOTTOM);

        final Button okBtn = new Button("OK");
        subBtnPanel.addComponent(okBtn);


        final Button cancelBtn = new Button("Cancel");
        subBtnPanel.addComponent(cancelBtn);
        btnPanel.addComponent(new EmptySpace(TerminalSize.ONE), Location.TOP);
        btnPanel.addComponent(subBtnPanel, Location.RIGHT);

        okBtn.addListener(this::onOk);
        cancelBtn.addListener(this::onClose);

        setComponent(contentPanel);
        setCloseWindowWithEscape(true);
    }

    private void onOk(Button button) {
        onOk.accept(new StoreContext(path, excludedHolder, this));
    }

    private void onClose(Button button) {
        close();
    }

    public void addLine(final String line) {
        propertiesTextBox.addLine(Objects.requireNonNull(line, "Line of property must be defined"));
    }

    public String getLine(final int index) {
        return propertiesTextBox.getLine(index);
    }

    public int getLineCount() {
        return propertiesTextBox.getLineCount();
    }


    public void removeLine(final int lineIndex) {
        propertiesTextBox.removeLine(lineIndex);
    }
}
