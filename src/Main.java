

// SYSTEM IMPORTS
import Calculators.Electronics.Basic.StandardResistanceFinder.StandardResistanceFinderCalcModel;
import Core.Calculator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

// USER IMPORTS
import MainWindow.MainWindowController;

import java.io.*;
import java.util.ArrayList;

/**
 * Entry class for NinjaCalc application.
 *
 * @author          gbmhunter <gbmhunter@gmail.com> (www.mbedded.ninja)
 * @since           2015-11-02
 * @last-modified   2016-05-15
 */
public class Main extends Application {

    //===============================================================================================//
    //========================================== CONSTANTS ==========================================//
    //===============================================================================================//

    public static final String appStateFileName = "NinjaCalcState.data";

    //===============================================================================================//
    //============================================ FIELDS ===========================================//
    //===============================================================================================//

    MainWindowController controller;

    //===============================================================================================//
    //======================================== CONSTRUCTORS =========================================//
    //===============================================================================================//

    //===============================================================================================//
    //======================================= GENERAL METHODS =======================================//
    //===============================================================================================//

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Hide the install4j splash screen, as this is not done automatically as
        // no AWT window is shown (we are using javafx instead)
        // <install4j install directory>/resource/i4juntime.jar has to be added to the classpath
        // to be able to call this. install4j automatically bundles the runtime with the application
        com.install4j.api.launcher.SplashScreen.hide();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow/MainWindowView.fxml"));
        Parent root = loader.load();

        // Load the default style sheet
        root.getStylesheets().add("/Core/StyleSheets/default.css");

        // Get a handle of the controller (backend) for the MainWindowView.fxml file
        controller = loader.getController();

        //===============================================================================================//
        //======================================= REGISTER CALCULATORS ==================================//
        //===============================================================================================//

        // Any of the following can be commented out to remove the calculator(s) from the app

        //======== SCIENTIFIC =========//
        controller.addCalculatorTemplate(new Calculators.Scientific.ScientificCalcModel());

        //===============================================================================================//
        //========================================== ELECTRONICS ========================================//
        //===============================================================================================//

        //========== BASIC ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Basic.OhmsLaw.OhmsLawCalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Basic.ResistorDivider.ResistorDividerCalcModel());
        controller.addCalculatorTemplate(new StandardResistanceFinderCalcModel());

        //========== FILTERS ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Filters.LowPassRC.LowPassRCCalcModel());

        //========== PCB ==========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Pcb.TrackCurrentIpc2221A.TrackCurrentIpc2221ACalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Pcb.TrackCurrentIpc2152.TrackCurrentIpc2152CalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Pcb.ViaCurrentIpc2221A.ViaCurrentIpc2221ACalcModel());

        //========== SENSORS =========//
        controller.addCalculatorTemplate(new Calculators.Electronics.Sensors.NtcThermistor.NtcThermistorCalcModel());
        controller.addCalculatorTemplate(new Calculators.Electronics.Sensors.DewPointMagnus.DewPointMagnusCalcModel());

        //===============================================================================================//
        //============================================ SOFTWARE =========================================//
        //===============================================================================================//

        controller.addCalculatorTemplate(new Calculators.Software.Crc.CrcCalcModel());

        //===============================================================================================//
        //================================== SETUP AND SHOW APPLICATION =================================//
        //===============================================================================================//

        primaryStage.setTitle("NinjaCalc");

        // Setup the dimensions for the NinjaCalc window.
        // By default, it should be large enough to display and operate
        // comfortably, but not take up the entire window (e.g. not maximised), to give it a "calculator" feel.
        // EDIT: NinjaCalc is now shown full-screen to give user a better experience with the app
        primaryStage.setScene(new Scene(root, 1200, 800, false, SceneAntialiasing.BALANCED));

        primaryStage.setMaximized(true);

        //====== SAVING/LOADING APP STATE =====//

        // Install event handler for when the app is closed
        primaryStage.setOnCloseRequest((windowEvent) -> {
                //this.saveCalculatorState();
            }
        );

        // Load saved calculator state (if any)
        //loadCalculatorState();

        // Show the NinjaCalc app
        primaryStage.show();

    }


    private void saveCalculatorState() {

        try {
            // Write to disk with FileOutputStream
            FileOutputStream fileOS = new
                    FileOutputStream(appStateFileName);

            // Write object with ObjectOutputStream
            ObjectOutputStream objOS = new
                    ObjectOutputStream(fileOS);

            // Write object out to disk
            objOS.writeObject(controller.getOpenCalculators());

            objOS.close();
            fileOS.close();
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FileNotFoundException");
            //alert.setHeaderText("Look, an Information Dialog");
            alert.setContentText("File was not found when trying to save the app state.");

            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("IOException");
            //alert.setHeaderText("Look, an Information Dialog");
            alert.setContentText("An IOException occurred while trying to save the app state.");
            alert.showAndWait();
        }
    }

    private void loadCalculatorState() {

        System.out.println("Main.loadCalculatorState() called.");

        try {
            FileInputStream fileIS = new FileInputStream(appStateFileName);

            ObjectInputStream objectIS = new ObjectInputStream(fileIS);

            //Object obj = objectIS.readObject();

            ArrayList<Calculator> calculators = (ArrayList<Calculator>)objectIS.readObject();

            // Finally, call the controller, passing it the calculators we want
            // to be open
            controller.setOpenCalculators(calculators);

        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("FileNotFoundException");
            alert.setContentText("File was not found when trying to read the app state.");
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("IOException");
            alert.setContentText("An IOException occurred while trying to read the app state.");
            alert.showAndWait();
        } catch (ClassNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ClassNotFoundException");
            alert.setContentText("An ClassNotFoundException occurred while trying to read the app state.");
            alert.showAndWait();
        }
    }


}
