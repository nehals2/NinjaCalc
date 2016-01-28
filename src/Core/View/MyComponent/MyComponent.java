package Core.View.MyComponent;



import java.io.IOException;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class MyComponent extends Pane {

    private Node view;
    private MyComponentController controller;

    public MyComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("myComponent.fxml"));
        fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return controller = new MyComponentController();
            }
        });
        try {
            view = (Node) fxmlLoader.load();

        } catch (IOException ex) {
        }
        getChildren().add(view);
    }

    public void setWelcome(String str) {
        controller.textField.setText(str);
    }

    public String getWelcome() {
        return controller.textField.getText();
    }

    public StringProperty welcomeProperty() {
        return controller.textField.textProperty();
    }
}