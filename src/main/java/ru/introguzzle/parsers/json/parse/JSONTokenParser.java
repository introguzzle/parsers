package ru.introguzzle.parsers.json.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.parse.tokenize.Token;
import ru.introguzzle.parsers.json.parse.tokenize.Tokenizer;
import ru.introguzzle.parsers.json.parse.tokenize.Type;

import java.util.List;
import java.util.function.Predicate;

class JSONTokenParser extends Parser {
    private final Tokenizer tokenizer;

    public JSONTokenParser() {
        this(Tokenizer.newTokenizer());
    }

    public JSONTokenParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public <T> @Nullable T parse(@Nullable String data, @NotNull Class<? extends T> type) {
        if (data == null) return null;
        if (data.isEmpty()) {
            return handleEmptyString(data, type);
        }

        Predicate<Token> predicate = token -> {
            Type t = token.getType();
            return t != Type.COLON && t != Type.COMMA;
        };

        List<Token> tokens = tokenizer.tokenize(data).stream()
                .filter(predicate)
                .toList();

        return map(new TokenBuffer(tokens), type);
    }

    <T> T map(@NotNull TokenBuffer buffer, @NotNull Class<? extends T> type) {
        Token token = buffer.current();
        Type tokenType = token.getType();

        Object result;
        if (tokenType == Type.OBJECT_START) {
            result = parseObject(buffer);
        } else if (tokenType == Type.ARRAY_START) {
            result = parseArray(buffer);
        } else {
            result = handlePrimitiveType(token.getData(), type);
        }

        return type.cast(result);
    }

    @NotNull JSONObject parseObject(@NotNull TokenBuffer buffer) {
        JSONObject object = new JSONObject();
        buffer.next();
        Token current = buffer.current();

        while (buffer.position < buffer.size() && current.getType() != Type.OBJECT_END) {
            String key = current.getData().replace("\"", "");
            buffer.next();
            Object value = map(buffer, Object.class);

            object.put(key, value);
            current = buffer.next();
        }

        return object;
    }

    @NotNull JSONArray parseArray(@NotNull TokenBuffer buffer) {
        JSONArray array = new JSONArray();
        buffer.next();
        Token current = buffer.current();

        while (buffer.position < buffer.size() && current.getType() != Type.ARRAY_END) {
            Object value = map(buffer, Object.class);
            array.add(value);
            current = buffer.next();
        }

        return array;
    }
}
