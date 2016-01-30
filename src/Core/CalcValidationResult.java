package Core;

/**
 * Designed to represent a single result from performing a validation on a calculator variable.
 *
 * @author gbmhunter
 * @since 2015-11-02
 */
public class CalcValidationResult {
    public CalcValidationLevel CalcValidationLevel;

    public String Message;

    public CalcValidationResult(CalcValidationLevel calcValidationLevel, String message) {
        this.CalcValidationLevel = calcValidationLevel;
        this.Message = message;
    }
}
