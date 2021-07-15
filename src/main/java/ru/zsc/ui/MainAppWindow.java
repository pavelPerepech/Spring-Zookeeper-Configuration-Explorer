package ru.zsc.ui;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import ru.zsc.ui.PropertyDialog.StoreContext;
import ru.zsc.util.AppUICtx;
import ru.zsc.util.ComponentFactory;
import ru.zsc.util.LineConsumerWriter;
import ru.zsc.util.LineSupplierReader;
import ru.zsc.zoo.ZooAccessor;

/**
 * Created by Pavel Perepech
 */
public class MainAppWindow extends BasicWindow {

    public static final String PARENT_PATH = "..";
    private final ParamTable paramsTable;
    private final Label pathLabel;
    private final MainStateLine stateLine;
    private final TextBox valueBox;

    private String currentPath = "/";
    private Map<String, byte[]> levelData = new HashMap<>();
    private int lastIndex = 0;

    public MainAppWindow() {
        super("Zookeeper Spring Parameters Explorer");
        setHints(Arrays.asList(Hint.FULL_SCREEN));

        final LayoutManager layoutManager = new BorderLayout();
        final Panel contentPanel = new Panel(layoutManager);

        pathLabel = new Label("...");
        contentPanel.addComponent(pathLabel, Location.TOP);

        paramsTable = new ParamTable();
        contentPanel.addComponent(paramsTable, Location.LEFT);

        valueBox = new TextBox();
        valueBox.setReadOnly(true);
        contentPanel.addComponent(valueBox, Location.CENTER);

        stateLine = new MainStateLine();
        contentPanel.addComponent(stateLine, Location.BOTTOM);

        setComponent(contentPanel);
        setCloseWindowWithEscape(true);

        paramsTable.setInputFilter(this::handleTableInput);
        load(currentPath);

        paramsTable.setRowSelectionHandler(row -> {
            final String key = paramsTable.getTableModel().getCell(0, row);
            if (PARENT_PATH.equals(key)) {
                valueBox.setText("");
            } else {
                valueBox.setText(getValueForView(levelData.get(key)));
            }

        });
    }

    private boolean handleTableInput(final Interactable interactable, final KeyStroke keyStroke) {
        if (keyStroke.getKeyType() == KeyType.Enter && noCtrlAltShift(keyStroke)) {
            return handleGo();
        }

        if (keyStroke.getKeyType() == KeyType.ArrowUp && keyStroke.isShiftDown()) {
            paramsTable.setSelectedRow(0);
            paramsTable.invalidate();
            return false;
        }

        if (keyStroke.getKeyType() == KeyType.ArrowDown && keyStroke.isShiftDown()) {
            paramsTable.setSelectedRow(paramsTable.getTableModel().getRowCount() - 1);
            paramsTable.invalidate();
            return false;
        }

        return true;
    }

    private boolean handleGo() {
        final int row = paramsTable.getSelectedRow();
        final String key = paramsTable.getTableModel().getCell(0, row);
        if (PARENT_PATH.equals(key)) {
            final int lastSlashIdx = currentPath.lastIndexOf('/');
            if (lastSlashIdx > 0) {
                load(currentPath.substring(0, lastSlashIdx));
            }
            if (lastSlashIdx == 0) {
                load("/");
            }
            return false;
        } else {
            load("/".equals(currentPath) ? '/' + key : currentPath + '/' + key);
            return false;
        }
    }

    private boolean noCtrlAltShift(final KeyStroke keyStroke) {
        return !keyStroke.isShiftDown() && !keyStroke.isAltDown() && !keyStroke.isCtrlDown();
    }

    private void load(final String path) {
        final ZooAccessor zooAccessor = ComponentFactory.getInstance().zooAccessor();
        levelData = zooAccessor.readLevel(path);
        final List<String> keys = levelData.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        lastIndex = 0;
        valueBox.setText("");

        final TableModel<String> tableModel = paramsTable.getTableModel();
        tableModel.clear();
        if (!"/".equals(path)) {
            tableModel.addRow(PARENT_PATH, "");
        }

        for (int i = 0; i < keys.size(); i++) {
            final String key = keys.get(i);
            tableModel.addRow(key, getValueShort(levelData.get(key)));
            if (lastIndex == i) {
                valueBox.setText(getValueForView(levelData.get(key)));
            }
        }
        currentPath = path;
        pathLabel.setText(currentPath);
    }

    private String getValueShort(final byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return "<0 bytes>";
        }

        String value;
        try {
            value = new String(data);
            final int crlfIdx = value.indexOf("\n");
            if (crlfIdx >= 0) {
                value = value.substring(0, crlfIdx) + "...";
            }
        } catch (Exception e) {
            value = "<" + data.length + " bytes>";
        }

        return value;
    }

    private String getValueForView(final byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return "<0 bytes>";
        }

        String value;
        try {
            value = new String(data);
        } catch (Exception e) {
            value = "<" + data.length + " bytes>";
        }

        return value;
    }

    private void readAsProperties(final String path, final List<String> excludedHolder, Consumer<String> lineConsumer) {
        final Properties properties = ComponentFactory.getInstance().zooAccessor()
                .readNodeAsProperty(path, excludedHolder);
        try {
            properties.store(new LineConsumerWriter(lineConsumer), "");
        } catch (IOException e) {
            throw new RuntimeException(
                    MessageFormat.format("Error convert properties to text caused by: {}", e.getMessage()), e);
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.F4) {
            final List<String> excludedHolder = new ArrayList<>();
            final String currentNode = paramsTable.getTableModel().getCell(0, paramsTable.getSelectedRow());
            final String editPath =
                    "/".equals(currentPath) ? currentPath + currentNode : currentPath + "/" + currentNode;

            final PropertyDialog propertyDialog = new PropertyDialog(editPath, null, excludedHolder,
                    this::onSaveRequest);

            final CompletableFuture<Void> loadedFuture = CompletableFuture.supplyAsync(() -> {
                readAsProperties(editPath, excludedHolder, propertyDialog::addLine);
                if (propertyDialog.getLineCount() > 1) {
                    propertyDialog.removeLine(0);
                }
                return null;
            });

            final Exception loadError = doWait(
                    "Loading...", "Please wait...", "Loading error", loadedFuture);

            if (Objects.isNull(loadError)) {
                AppUICtx.get().getWindowBasedTextGUI().addWindowAndWait(propertyDialog);
                propertyDialog.invalidate();
            }
        }

        return super.handleInput(key);
    }

    private void onSaveRequest(StoreContext storeContext) {
        final Properties properties;
        try {
            properties = readPropertiesFromDialog(storeContext.getSelf());
        } catch (Exception e) {
            new MessageDialogBuilder()
                    .setTitle("Error convert to properties")
                    .setText(e.getMessage())
                    .addButton(MessageDialogButton.Close)
                    .build()
                    .showDialog(AppUICtx.get().getWindowBasedTextGUI());
            return;
        }

        final CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            try {
                ComponentFactory.getInstance().zooAccessor().storeProperty(
                        storeContext.getPath(), properties, storeContext.getExcludedHolder());
            } catch (Exception e) {
                throw new RuntimeException(MessageFormat.format("Error store properties: {0}", e.getMessage()), e);
            }
        });

        final Exception storeError = doWait(
                "Saving", "Please wait", "Error store properties", storeFuture);

        if (Objects.isNull(storeError)) {
            storeContext.getSelf().close();
        }
    }

    private Properties readPropertiesFromDialog(final PropertyDialog propertyDialog) {
        final Properties properties = new Properties();

        final Supplier<String> lineSupplier = new Supplier<String>() {

            private int lineIndex = 0;

            @Override
            public String get() {
                if (lineIndex < propertyDialog.getLineCount()) {
                    return propertyDialog.getLine(lineIndex++);
                } else {
                    return null;
                }
            }
        };

        try {
            properties.load(new LineSupplierReader(lineSupplier));
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Error convert text to parameters caused by: {0}", e.getMessage()), e);
        }

        return properties;
    }

    private Exception doWait(
            final String title, final String text, final String errorTitle, final CompletableFuture<Void> routine) {
        final WaitingDialog waitingDialog = WaitingDialog.createDialog(title, text);

        waitingDialog.showDialog(AppUICtx.get().getWindowBasedTextGUI(), false);
        boolean compleeted = false;
        Exception error = null;
        while (!compleeted) {
            try {
                routine.get(200, TimeUnit.MILLISECONDS);
                compleeted = true;
            } catch (InterruptedException e) {
                compleeted = true;
                error = e;
            } catch (ExecutionException e) {
                compleeted = true;
                error = e;
            } catch (TimeoutException ignored) {
                compleeted = false;
            }

            try {
                AppUICtx.get().getWindowBasedTextGUI().updateScreen();
            } catch (IOException ignored) {
            }
        }

        waitingDialog.close();

        if (Objects.nonNull(error)) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return error;
            }

            if (Objects.nonNull(errorTitle)) {
                new MessageDialogBuilder()
                        .setTitle(errorTitle)
                        .setText(getCause(error))
                        .setExtraWindowHints(Arrays.asList(Hint.CENTERED, Hint.MODAL))
                        .build()
                        .showDialog(AppUICtx.get().getWindowBasedTextGUI());
            }

            return error;
        } else {
            return error;
        }
    }

    private String getCause(final Exception e) {
        if (Objects.isNull(e)) {
            return null;
        }

        if (e instanceof ExecutionException) {
            return ((ExecutionException) e).getCause().getMessage();
        }

        return e.getMessage();
    }
}
