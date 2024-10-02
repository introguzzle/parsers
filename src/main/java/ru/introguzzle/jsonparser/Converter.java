package ru.introguzzle.jsonparser;

public interface Converter {
    <T> T map(String data, Class<? extends T> type);
}
