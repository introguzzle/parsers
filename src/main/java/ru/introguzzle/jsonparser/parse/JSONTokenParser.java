package ru.introguzzle.jsonparser.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.jsonparser.convert.primitive.DefaultPrimitiveTypeConverter;
import ru.introguzzle.jsonparser.convert.primitive.PrimitiveTypeConverter;
import ru.introguzzle.jsonparser.entity.JSONArray;
import ru.introguzzle.jsonparser.entity.JSONObject;
import ru.introguzzle.jsonparser.parse.tokenize.DefaultTokenizer;
import ru.introguzzle.jsonparser.parse.tokenize.Token;
import ru.introguzzle.jsonparser.parse.tokenize.Tokenizer;
import ru.introguzzle.jsonparser.parse.tokenize.Type;

import java.util.List;
import java.util.function.Predicate;

public class JSONTokenParser implements Parser {
    private final Tokenizer tokenizer = new DefaultTokenizer();
    private final PrimitiveTypeConverter primitiveTypeConverter = new DefaultPrimitiveTypeConverter();

    private static final class Buffer {
        int position;
        final List<Token> tokens;

        Buffer(List<Token> tokens) {
            this.position = 0;
            this.tokens = tokens;
        }

        Token current() {
            return tokens.get(position);
        }

        void next() {
            position++;
        }

        int size() {
            return tokens.size();
        }
    }

    @Override
    public <T> T parse(@Nullable String data, @NotNull Class<? extends T> type) {
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

        return map(new Buffer(tokens), type);
    }

    private <T> T map(Buffer buffer, Class<? extends T> type) {
        Token token = buffer.current();
        Type tokenType = token.getType();

        Object result;
        if (tokenType == Type.OBJECT_START) {
            result = parseObject(buffer);
        } else if (tokenType == Type.ARRAY_START) {
            result = parseArray(buffer);
        } else {
            result = primitiveTypeConverter.map(token.getData(), type);
        }

        return type.cast(result);
    }

    private JSONObject parseObject(Buffer buffer) {
        JSONObject object = new JSONObject();
        buffer.next();
        Token current = buffer.current();

        while (buffer.position < buffer.size() && current.getType() != Type.OBJECT_END) {
            String key = current.getData().replace("\"", "");
            buffer.next();
            Object value = map(buffer, Object.class);

            object.put(key, value);
            buffer.next();
            current = buffer.current();
        }

        return object;
    }

    private JSONArray parseArray(Buffer buffer) {
        JSONArray array = new JSONArray();
        buffer.next();
        Token current = buffer.current();

        while (buffer.position < buffer.size() && current.getType() != Type.ARRAY_END) {
            Object value = map(buffer, Object.class);
            array.add(value);
            buffer.next();
            current = buffer.current();
        }

        return array;
    }
}
