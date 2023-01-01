package space.graynk.sie.gui;


import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class LayerCell extends ListCell<Layer> {
    private final ImageView imageView = new ImageView();

    @Override
    public void updateItem(Layer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            // shut up, I know
            this.setDisable(item.getText().equals("Background"));
            imageView.imageProperty().bind(item.previewProperty);
            setText(item.getText());
            setGraphic(imageView);
        }
    }
}
