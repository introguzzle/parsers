package ru.introguzzle.parsers.xml.mapping.serialization;

/**
 * Interface for inflecting words, such as converting them from plural to singular forms.
 * <p>
 * This interface defines a method for singularizing strings. It can be extended in the future to include other
 * inflection operations such as pluralization, case conversion, etc.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * Inflector inflector = new SimpleInflector();
 * String singularWord = inflector.singularize("cats");
 * System.out.println(singularWord);  // Outputs: "cat"
 * </pre>
 * </p>
 */
public interface Inflector {

    /**
     * Converts a plural word into its singular form.
     *
     * @param string the plural word to be converted to singular form.
     * @return the singular form of the input word.
     * @throws IllegalArgumentException if the input is not a plural word or cannot be singularized.
     */
    String singularize(String string);
}
