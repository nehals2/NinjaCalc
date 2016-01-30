import Core.Calculator;
import MainWindow.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry class for NinjaCalc application.
 *
 * @author gbmhunter
 * @since 2015-11-02
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow/MainWindow.fxml"));
        Parent root = loader.load();

        // Load the default style sheet
        root.getStylesheets().add("/Core/StyleSheets/default.css");

        MainWindowController controller = loader.getController();

        //===============================================================================================//
        //======================================= REGISTER CALCULATORS ==================================//
        //===============================================================================================//

        //========== BASIC ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Basic.OhmsLaw.OhmsLawCalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Basic.ResistorDivider.ResistorDividerCalcModel());

        //========== FILTERS ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Filters.LowPassRC.LowPassRCCalcModel());

        //========== PCB ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Pcb.TrackCurrentIpc2221A.TrackCurrentIpc2221ACalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Pcb.TrackCurrentIpc2152.TrackCurrentIpc2152CalcModel());

        //===============================================================================================//
        //================================== SETUP AND SHOW APPLICATION =================================//
        //===============================================================================================//

        primaryStage.setTitle("NinjaCalc");
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.setMaximized(true);

        primaryStage.show();

    }



    public static void main(String[] args) {
        launch(args);
    }


}