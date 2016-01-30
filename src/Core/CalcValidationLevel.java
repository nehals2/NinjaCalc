package Core;


/**
 * Encapsulates a validation result.
 *
 * @author gbmhunter
 * @since 2015-11-02
 */
public class CalcValidationLevel {

    public String name;

    public String borderColor;

    public String backgroundColor;

    public CalcValidationLevel(String name, String borderColor, String backgroundColor) {
        this.name = name;
        this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
    }
}
