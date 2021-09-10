package space.graynk.sie;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
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
    private TabInternalsController tabInternalsController;

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

//    @FXML
//    private void initialize()  {
//        //TODO: for fast testing
//        worker.submit(() -> {
//            try {
//                Thread.sleep(200);
////                var file = new File("/home/graynk/Pictures/gzwuspaspm641.png");
//                var file = new File("/home/graynk/Pictures/godot.png");
//                loadImageFromFile(file);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    private void loadImageFromFile(File file) {
        var image = new Image(file.toURI().toString());
        //TODO open tab
        tabInternalsController.drawImage(image);
    }

    private void saveImageToFile(File file) {
        //TODO get from tab
        var renderedImage = tabInternalsController.getImage();
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
        File file = fileChooser.showSaveDialog(null);
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
        File file = fileChooser.showOpenDialog(null);
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
    private void addLayer() {
        tabInternalsController.addLayer();
    }

    @FXML
    private void deleteLayer() {
        tabInternalsController.deleteActiveLayer();
    }

    @FXML
    private void mergeDown() {
        tabInternalsController.mergeDownActiveLayer();
    }
}