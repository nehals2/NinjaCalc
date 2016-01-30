package Core;

import javafx.scene.control.*;

/**
 * A specialisation of a generic CalcVar which is for variables which are always
 * an input. Removes the ability to add a input/output radio button and provide
 * an equation.
 */
public class CalcVarNumericalInput extends CalcVarNumerical {

    /// <summary>
    /// Base constructor, which requires all possible arguments.
    /// </summary>
    /// <param name="name"></param>
    /// <param name="calcValTextBox"></param>
    /// <param name="unitsComboBox"></param>
    /// <param name="units"></param>
    /// <param name="defaultRawValue"></param>
    public CalcVarNumericalInput(
        String name,
        TextField calcValTextBox,
        ComboBox unitsComboBox,
        NumberUnit[] units,
        int numDigitsToRound,
        Double defaultRawValue,
        String helpText) {

        super(
            name,
            calcValTextBox,
            unitsComboBox,
            //null,
            //null,
            null,
            units,
            numDigitsToRound,
            () -> CalcVarDirections.Input,
            defaultRawValue,
            helpText);

    }

}