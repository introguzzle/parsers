package ru.introguzzle.parser.json.mapping;

import org.junit.Test;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.context.MappingContext;

public class ObjectTOJsonMapperTest {
    private final ObjectToJSONMapper objectToJSONMapper = new ObjectToJSONMapperImpl();

    @Test
    public void test_transient() {
        var data = new Data.Transient("John Doe", 42);
        JSONObject resultObject = objectToJSONMapper.toJSONObject(data, MappingContext.getDefault());

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.Transient.class));
    }

    @Test
    public void test_inheriting() {
        var data = new Data.Inheriting("sss", (byte) 1, 333.3);
        JSONObject resultObject = objectToJSONMapper.toJSONObject(data, MappingContext.getDefault());

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.Inheriting.class));
    }
}