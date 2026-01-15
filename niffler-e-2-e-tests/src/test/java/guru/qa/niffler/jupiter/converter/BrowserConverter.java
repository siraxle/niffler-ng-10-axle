package guru.qa.niffler.jupiter.converter;

import guru.qa.niffler.config.Browser;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class BrowserConverter implements ArgumentConverter {

    @Override
    public Object convert(Object source, ParameterContext context)
            throws ArgumentConversionException {

        if (!(source instanceof String)) {
            throw new ArgumentConversionException("Source must be a String");
        }

        String browserStr = ((String) source).toUpperCase();
        try {
            return Browser.valueOf(browserStr);
        } catch (IllegalArgumentException e) {
            throw new ArgumentConversionException(
                    "Invalid browser value: " + source + ". Expected: CHROME or FIREFOX"
            );
        }
    }
}
