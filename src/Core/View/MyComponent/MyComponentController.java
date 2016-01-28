package Core.View.MyComponent;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class MyComponentController implements Initializable {

    int i = 0;
    @FXML
    TextField textField;

    @FXML
    protected void doSomething() {
        textField.setText("The button was clicked #" + ++i);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textField.setText("Just click the button!");
    }
}