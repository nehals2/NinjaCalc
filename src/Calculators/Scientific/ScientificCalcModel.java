package Calculators.Scientific;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

// USER LIBRARIES
import Core.Calculator;

/**
 * A general purpose scientific calculator for doing day-to-day calculations. Uses expression parsing to calculate
 * the result of a mathematic formula entered as a string.
 *
 * @author gbmhunter
 * @since 2013-04-09
 * @last-modified 2016-04-10
 */
public class ScientificCalcModel extends Calculator{

    @FXML private VBox expressionsVBox;

    @FXML private TextField expressionInput;

    // The following variables get assigned in
    // loadJavascript().
    ScriptEngineManager scriptEngineManager;
    ScriptEngine scriptEngine;
    Invocable inv;
    Object mathJsObj;
    Object mathJsParserObj;

    public ScientificCalcModel() {

        super("Scientific Calculator",
                "A generic scientific calculator for doing basic mathematical calculations.",
                new String[]{"Scientific"},
                new String[]{"scientific, generic, general, mathematics, calculations, equations"});

        super.setIconImagePath(getClass().getResource("grid-icon.jpg"));

        //===============================================================================================//
        //======================================== LOAD .FXML FILE ======================================//
        //===============================================================================================//

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ScientificCalcView.fxml"));
        //fxmlLoader.setRoot(this.view);
        fxmlLoader.setController(this);
        try {
            // Create a UI node from the FXML file, and save it to the view variable.
            // This will be used by the main window to create a new instance of this calculator when
            // the "Open" button is clicked.
            this.view = fxmlLoader.load();
        } catch (IllegalStateException e) {
            System.err.println("IllegalStateException occurred, is the fxml path valid?");
            throw e;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        //===============================================================================================//
        //============================== LOAD CALCULATOR-SPECIFIC STYLING ===============================//
        //===============================================================================================//

        // Grab .css file and convert contents to string
        String css = getClass().getResource("style.css").toExternalForm();
        // Add this css data to this calculator node
        this.view.getStylesheets().add(css);

        //===============================================================================================//
        //====================================== INSTALL EVENT HANDLERS =================================//
        //===============================================================================================//

        // Setup listener for text area
        this.expressionInput.setOnKeyPressed((event) -> {
            //System.out.println("setOnKeyPressed(). called.");

            if(event.getCode() == KeyCode.ENTER) {
                //System.out.println("Enter key pressed.");
                this.parseExpression();
            }
        });

    }

    /**
     * Loading the javascript was removed from the constructor because it takes many
     * seconds to complete and causes the UI to lag. Lazily initialised by parseExpression().
     */
    private void loadJavascript() {
        scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        if (!(scriptEngine instanceof Invocable)) {
            System.out.println("Invoking methods is not supported.");
            return;
        }
        this.inv = (Invocable) scriptEngine;
        String scriptPath = getClass().getResource("math.min.js").getPath();

        try {
            // This next line takes many seconds to execute
            scriptEngine.eval("load('" + scriptPath + "')");
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        this.mathJsObj = scriptEngine.get("math");

        // Get a math.js parser object (a parser object remembers variable/function
        // history)
        try {
            this.mathJsParserObj = inv.invokeMethod(mathJsObj, "parser");
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private void parseExpression() {

        System.out.println("parseExpression() called.");

        // Lazily initialise the javascript engine if this is the first time
        // parseExpression has been called
        if(this.mathJsObj == null) {
            this.loadJavascript();
        }


        // We need to extract the last line of text from the text area.
        String calculatorText = this.expressionInput.getText();

        System.out.println("***TextArea text = ***");
        System.out.print(calculatorText);
        System.out.println("***End of TextArea text***");


        //Expression expression = new Expression(calculatorText);
        String expressionResult = "";

        /*try {
            BigDecimal result = expression.eval();
            expressionResult = result.toString();
        } catch(RuntimeException e) {


            expressionResult = "ERROR: " + e.getMessage();
        }*/

        try {
            Object addResult = inv.invokeMethod(mathJsParserObj, "eval", calculatorText);
            System.out.println(addResult.toString());
            expressionResult = addResult.toString();
        } catch (ScriptException e) {
            // RunTimeExceptions usually occur if there is an unrecognised
            // variable in the expression (or the syntax is just bad)

            // We want to set the expression result to an error message. We
            // don't want to include the java.lang.RunTimeException... bit,
            // so just get the message part of the exception
            System.err.println(e.toString());
            // Calling e.getCause().getMessage() provides a short error useful for displaying
            // to the user, without the filepath.
            expressionResult = e.getCause().getMessage();
        } catch (NoSuchMethodException e) {
            System.err.println(e.toString());
            return;
        }
        
        System.out.println("expressionResult = " + expressionResult);

        // Display the result of the expression to the user
        this.addExpressionResultToUI(calculatorText, expressionResult);

        // Now we have added the result of the expression to the GUI, clear the
        // input for user to enter the next expression
        expressionInput.clear();

    }

    /***
     * Adds the provided result of an expression to the correct place in the VBox
     * which holds all the the expression history.
     */
    private void addExpressionResultToUI(String expressionInput, String expressionResult) {

        // Create a new UI object to display to the user
        TextArea textArea = new TextArea();
        Text textHolder = new Text();

        textArea.setMinHeight(20);

        // We don't want the user to be able to edit the previous expression displays
        textArea.setEditable(false);

        textHolder.textProperty().bind(textArea.textProperty());

        textHolder.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            //if (oldHeight != newValue.getHeight()) {
                System.out.println("newValue = " + newValue.getHeight());
                //oldHeight = newValue.getHeight();
                textArea.setPrefHeight(textHolder.getLayoutBounds().getHeight() + 20); // +20 is for paddings
            //}
        });

        // The text MUST be set after the text properties have been bound AND the listener has been added to the textHolder
        String textToDisplay = expressionInput + "\n\t" + expressionResult;
        textArea.setText(textToDisplay);

        ObservableList<Node> vBoxChildren = this.expressionsVBox.getChildren();

        Integer numVBoxChildren = vBoxChildren.size();

        // Insert new UI object as second to last object (last object being the text area
        // to enter new expressions into
        vBoxChildren.add(numVBoxChildren - 1, textArea);
    }


}
