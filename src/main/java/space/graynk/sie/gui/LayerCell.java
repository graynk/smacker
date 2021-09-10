package space.graynk.sie.gui;


import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class LayerCell extends ListCell<LayerItem> {
    private final ImageView imageView = new ImageView();

    @Override
    public void updateItem(LayerItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            imageView.imageProperty().bind(item.previewProperty);
            setText(item.getText());
            setGraphic(imageView);
        }
    }
}
