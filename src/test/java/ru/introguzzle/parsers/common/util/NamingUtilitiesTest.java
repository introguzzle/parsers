package ru.introguzzle.parsers.common.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static ru.introguzzle.parsers.common.util.NamingUtilities.toSnakeCase;

public class NamingUtilitiesTest {

    @Test
    public void to_snake_test_2_chars() {
        String string = "aA";
        String result = toSnakeCase(string);
        assertEquals("a_a", result);
    }

    @Test
    public void to_snake_test_1_uppercase_char() {
        assertEquals("z", toSnakeCase("Z"));
    }

    @Test
    public void to_snake_test_1_lowercase_char() {
        assertEquals("z", toSnakeCase("z"));
    }

    @Test
    public void to_snake_test_2_words() {
        assertEquals("camel_case", toSnakeCase("camelCase"));
    }

    @Test
    public void to_snake_to_title_case_test_3_words() {
        assertEquals("title_case_number", toSnakeCase("TitleCaseNumber"));
    }

    @Test
    public void to_snake_test_3_words_with_numbers() {
        assertEquals("value_value_12345", toSnakeCase("valueValue12345"));
    }

    @Test
    public void to_snake_test_4_words_with_numbers_1() {
        assertEquals("word_word_2_word", toSnakeCase("wordWord2Word"));
    }

    @Test
    public void to_snake_test_4_words_with_numbers_2() {
        assertEquals("word_word_987_word", toSnakeCase("wordWord987Word"));
    }

    @Test
    public void to_snake_test_5_words_with_numbers() {
        assertEquals("z_xy_w_477", toSnakeCase("zXyW477"));
    }

    @Test
    public void to_snake_test_null() {
        assertNull(toSnakeCase(null));
    }

    @Test
    public void to_snake_test_empty_string() {
        assertEquals("", toSnakeCase(""));
    }

    @Test
    public void to_snake_test_all_uppercase() {
        assertEquals("word", toSnakeCase("WORD"));
    }

    @Test
    public void to_snake_test_all_lowercase() {
        assertEquals("word", toSnakeCase("word"));
    }

    @Test
    public void to_snake_test_leading_digits() {
        assertEquals("123_word", toSnakeCase("123Word"));
    }

    @Test
    public void to_snake_test_trailing_digits() {
        assertEquals("word_123", toSnakeCase("word123"));
    }

    @Test
    public void to_snake_test_multiple_number_sequences() {
        assertEquals("word_123_word_456_word", toSnakeCase("word123Word456Word"));
    }

    @Test
    public void to_snake_test_special_characters() {
        assertEquals("word-word", toSnakeCase("word-word"));
        assertEquals("word word", toSnakeCase("word word"));
    }

    @Test
    public void to_snake_test_existing_underscores() {
        assertEquals("word_word", toSnakeCase("word_Word"));
    }

    @Test
    public void to_snake_test_only_numbers() {
        assertEquals("12345", toSnakeCase("12345"));
    }

    @Test
    public void to_snake_test_digits_between_upper_and_lower() {
        assertEquals("word_2_word", toSnakeCase("word2Word"));
    }
}