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
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import space.graynk.sie.gui.SimpleFileTreeItem;
import space.graynk.sie.tools.Tool;
import space.graynk.sie.tools.manipulation.Select;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class SieController {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        var thread = new Thread(r, "Worker");
        thread.setDaemon(true);
        return thread;
    });

    private final static FileChooser.ExtensionFilter[] filters = {
            new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png"),
    };


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
    private TabInternalsController activeTabController;
    private final Map<Tab, TabInternalsController> controllerMap = new HashMap<>(16);

    // It's probably only ever going to be Select, but whatever
    private ReadOnlyObjectProperty<Tool> activeTool;
    private File readDirectory;
    private File writeDirectory;
    private final FileChooser fileChooser = new FileChooser();

    public SieController() {
        var userDirectoryString = System.getProperty("user.home");
        var pictures = new File(String.format("%s%s%s", userDirectoryString, File.separator, "Pictures"));
        if (pictures.canRead() && pictures.isDirectory()) {
            readDirectory = pictures;
            writeDirectory = pictures;
            return;
        }
        var home = new File(userDirectoryString);
        if(!home.canRead()) {
            home = new File(".");
        }
        readDirectory = home;
        writeDirectory = pictures;
    }

    private void updateFileTreeView(File root) {
        fileTreeView.setRoot(new SimpleFileTreeItem(root));
        fileTreeView.getRoot().setExpanded(true);
    }

    @FXML
    private void initialize() {
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
                return new File(string);
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
    private void onSaveAsFile() {
        fileChooser.setTitle("Save image");
        fileChooser.setInitialDirectory(writeDirectory);
        fileChooser.setInitialFileName("*.png");
        fileChooser.getExtensionFilters().addAll(filters);
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) {
            return;
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