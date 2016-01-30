package Core;

import javafx.css.PseudoClass;

/**
 * The different ValidationLevels a validation function can return.
 *
 * @author gbmhunter
 * @since 2015-11-02
 */
public class CalcValidationLevels {
    public final static CalcValidationLevel Ok = new CalcValidationLevel("ok", "green", "#e5ffe5");
    public final static CalcValidationLevel Warning = new CalcValidationLevel("warning", "orange", "#fff5e5");
    public final static CalcValidationLevel Error = new CalcValidationLevel("error", "red", "#ffe5e5");
}