package ru.zsc.ui;

import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Pavel Perepech 
 */
public class ParamTable extends Table<String> {

    private int lastRowSelectionNotified = -1;

    private Optional<Consumer<Integer>> rowSelectionHandler = Optional.empty();

    public ParamTable() {
        super("Key", "Value as string");
    }

    @Override
    protected void onAfterDrawing(TextGUIGraphics graphics) {
        super.onAfterDrawing(graphics);

        if (getSelectedRow() != lastRowSelectionNotified) {
            lastRowSelectionNotified = getSelectedRow();
            rowSelectionHandler.ifPresent(handler -> handler.accept(lastRowSelectionNotified));
        }
    }

    public ParamTable setRowSelectionHandler(
            Consumer<Integer> rowSelectionHandler) {
        this.rowSelectionHandler = Optional.ofNullable(rowSelectionHandler);
        return this;
    }
}
