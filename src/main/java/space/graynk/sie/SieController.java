package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import space.graynk.sie.gui.SimpleFileTreeItem;
import space.graynk.sie.tools.Tool;
import space.graynk.sie.tools.manipulation.Select;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SieController {
    private final static FileChooser.ExtensionFilter[] filters = {
            new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png"),
    };
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        var thread = new Thread(r, "Worker");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<Tab, TabInternalsController> controllerMap = new HashMap<>(16);
    private final FileChooser fileChooser = new FileChooser();
    @FXML
    private Pane rootPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeView<File> fileTreeView;
    @FXML
    private MenuItem deleteLayerMenu;
    @FXML
    private MenuItem mergeLayerMenu;
    @FXML
    private Spinner<Double> textHeightSpinner;
    @FXML
    private Spinner<Integer> paddingSpinner;
    @FXML
    private Label scalingStatus;
    @FXML
    private Label resettingStatus;
    private TabInternalsController activeTabController;
    // It's probably only ever going to be Select, but whatever
    private ReadOnlyObjectProperty<Tool> activeTool;
    private File readDirectory;
    private File writeDirectory;
    private int count;

    public SieController() {
        var userDirectoryString = System.getProperty("user.home");
        var pictures = new File(String.format("%s%s%s", userDirectoryString, File.separator, "Pictures"));
        if (pictures.canRead() && pictures.isDirectory()) {
            readDirectory = pictures;
            writeDirectory = pictures;
            return;
        }
        var home = new File(userDirectoryString);
        if (!home.canRead()) {
            home = new File(".");
        }
        readDirectory = home;
        writeDirectory = pictures;
    }

    private void updateFileTreeView(File root) {
        fileTreeView.setRoot(new SimpleFileTreeItem(root));
        fileTreeView.getRoot().setExpanded(true);
    }

    private void setGreen(Label label) {
        label.setText("✓");
        label.setTextFill(Color.GREEN);
    }

    private void setRed(Label label) {
        label.setText("❌");
        label.setTextFill(Color.RED);
    }

    @FXML
    private void initialize() {
        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isAltDown()) {
                setGreen(resettingStatus);
                setRed(scalingStatus);
                return;
            }
            if (keyEvent.isControlDown()) {
                setGreen(scalingStatus);
            }
        });
        rootPane.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (!keyEvent.isAltDown()) {
                setRed(resettingStatus);
            }
            if (keyEvent.isControlDown()) {
                setGreen(scalingStatus);
            } else {
                setRed(scalingStatus);
            }
        });
        textHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 55, 42));
        paddingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 200, 80));
        this.updateFileTreeView(this.readDirectory);
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            var file = newValue.getValue();
            if (file.isDirectory()) {
                return;
            }
            loadImageFromFile(file);
        });
        fileTreeView.setCellFactory(tv -> new TextFieldTreeCell<>(new StringConverter<File>() {
            @Override
            public String toString(File object) {
                return object.getName();
            }

            @Override
            public File fromString(String string) {
                throw new UnsupportedOperationException("Construction of file from string should never be called");
            }
        }));
        var activeTabBackgroundSelectedWrapper = new ReadOnlyBooleanWrapper();
        deleteLayerMenu.disableProperty().bind(activeTabBackgroundSelectedWrapper.getReadOnlyProperty());
        mergeLayerMenu.disableProperty().bind(activeTabBackgroundSelectedWrapper.getReadOnlyProperty());
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                        {
                            // not very pretty, but...
                            activeTabBackgroundSelectedWrapper.unbind();
                            activeTabController = controllerMap.get(newValue);
                            if (activeTabController == null) return;
                            activeTabBackgroundSelectedWrapper.bind(activeTabController.backgroundSelected);
                        }
                );
    }

    private void createNewTab(String name) {
        var fxmlLoader = new FXMLLoader(SieController.class.getResource("TabInternals.fxml"));
        Platform.runLater(() -> {
            try {
                Parent tabInternals = fxmlLoader.load();
                TabInternalsController tabInternalsController = fxmlLoader.getController();
                this.activeTool = new ReadOnlyObjectWrapper<>(new Select(this.paddingSpinner.valueProperty(), this.textHeightSpinner.valueProperty()));
                tabInternalsController.bindActiveTool(this.activeTool);
                tabInternalsController.bindTextHeight(this.textHeightSpinner.valueProperty());
                var tab = new Tab(name, tabInternals);
                tab.setClosable(true);
                controllerMap.put(tab, tabInternalsController);
                tab.setOnClosed(event -> controllerMap.remove(tab));
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().selectLast();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadImageFromFile(File file) {
        var image = new Image(file.toURI().toString());
        createNewTab(file.getName());
        Platform.runLater(() -> activeTabController.drawImage(image));
    }

    private void saveImageToFile(File file) {
        var renderedImage = activeTabController.getImageForSaving();
        worker.submit(() -> {
            try {
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onSave() {
        if (count == 0) {
            onSaveAsFile();
            return;
        }
        var file = new File(writeDirectory, String.format("%d.png", ++count));
        saveImageToFile(file);
    }

    @FXML
    private void onSaveAsFile() {
        fileChooser.setTitle("Save image");
        fileChooser.setInitialDirectory(writeDirectory);
        if (count != 0) {
            fileChooser.setInitialFileName(String.format("%d.png", count + 1));
        } else {
            fileChooser.setInitialFileName("*.png");
        }
        fileChooser.getExtensionFilters().addAll(filters);
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            var name = file.getName();
            count = Integer.parseInt(name.substring(0, name.length() - 4));
        } catch (Exception e) {
            // noop
        }
        writeDirectory = file.getParentFile();
        saveImageToFile(file);
    }

    @FXML
    private void onOpenFile() {
        fileChooser.setTitle("Open image");
        fileChooser.setInitialDirectory(readDirectory);
        fileChooser.getExtensionFilters().addAll(filters);
        File file = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        readDirectory = file.getParentFile();
        updateFileTreeView(this.readDirectory);
        worker.submit(() -> loadImageFromFile(file));
    }

    @FXML
    private void quit() {
        Platform.exit();
    }

    @FXML
    private void addLayer() {
        activeTabController.addLayer();
    }

    @FXML
    private void deleteLayer() {
        activeTabController.deleteActiveLayer();
    }

    @FXML
    private void mergeDown() {
        activeTabController.mergeDownActiveLayer();
    }
}