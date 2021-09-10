package space.graynk.sie.gui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.image.Image;

public class LayerItem {
    public ReadOnlyObjectProperty<Image> previewProperty;
    private String text;

    public LayerItem(ReadOnlyObjectProperty<Image> preview, String text) {
        this.previewProperty = preview;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
