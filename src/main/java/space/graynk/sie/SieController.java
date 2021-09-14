package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

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
    private MenuItem deleteLayerMenu;
    @FXML
    private MenuItem mergeLayerMenu;
    @FXML
    private TabInternalsController defaultTabController;
    private ObjectProperty<TabInternalsController> activeTabController;
    private final Map<Tab, TabInternalsController> controllerMap = new HashMap<>(16);

    private final File userDirectory;
    private final FileChooser fileChooser = new FileChooser();

    public SieController() {
        var userDirectoryString = System.getProperty("user.home");
        var pictures = new File(String.format("%s%s%s", userDirectoryString, File.separator, "Pictures"));
        if (pictures.canRead() && pictures.isDirectory()) {
            userDirectory = pictures;
            return;
        }
        var home = new File(userDirectoryString);
        if(!home.canRead()) {
            home = new File(".");
        }
        userDirectory = home;
    }

    @FXML
    private void initialize() {
        activeTabController = new SimpleObjectProperty<>(defaultTabController);
        controllerMap.put(tabPane.getTabs().get(0), activeTabController.getValue());
        activeTabController.getValue().newImage();
        var isZeroBinding = this.activeTabController.getValue().activeLayerIndexProperty.isEqualTo(this.activeTabController.getValue().layersCountProperty.subtract(1));
        deleteLayerMenu.disableProperty().bind(isZeroBinding);
        mergeLayerMenu.disableProperty().bind(isZeroBinding);
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                        {
//                            deleteLayerMenu.disableProperty().unbind(); // TODO: not very pretty, but...
//                            mergeLayerMenu.disableProperty().unbind();
                            activeTabController.setValue(controllerMap.get(newValue));
//                            var isZeroBindingt = this.activeTabController.activeLayerIndex.isEqualTo(0);
//                            deleteLayerMenu.disableProperty().bind(isZeroBindingt);
//                            mergeLayerMenu.disableProperty().bind(isZeroBindingt);
                        }
                );
    }

    private void createNewTab(String name) {
        var fxmlLoader = new FXMLLoader(SieController.class.getResource("TabInternals.fxml"));
        Platform.runLater(() -> {
            try {
                Parent tabInternals = fxmlLoader.load();
                TabInternalsController tabInternalsController = fxmlLoader.getController();
                var tab = new Tab(name, tabInternals);
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
        Platform.runLater(() -> activeTabController.getValue().drawImage(image));
    }

    private void saveImageToFile(File file) {
        var renderedImage = activeTabController.getValue().getImageForSaving();
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
        fileChooser.setInitialDirectory(userDirectory);
        fileChooser.getExtensionFilters().addAll(filters);
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        saveImageToFile(file);
    }

    @FXML
    private void onOpenFile() {
        fileChooser.setTitle("Open image");
        fileChooser.setInitialDirectory(userDirectory);
        fileChooser.getExtensionFilters().addAll(filters);
        File file = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        worker.submit(() -> loadImageFromFile(file));
    }

    @FXML
    private void quit() {
        Platform.exit();
    }

    @FXML
    private void newFile() {
        worker.submit(() -> {
            createNewTab("New Image");
            Platform.runLater(() -> activeTabController.getValue().newImage());
        });
    }

    @FXML
    private void addLayer() {
        activeTabController.getValue().addLayer();
    }

    @FXML
    private void deleteLayer() {
        activeTabController.getValue().deleteActiveLayer();
    }

    @FXML
    private void mergeDown() {
        activeTabController.getValue().mergeDownActiveLayer();
    }
}