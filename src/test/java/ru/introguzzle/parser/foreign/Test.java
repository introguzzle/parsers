package ru.introguzzle.parser.foreign;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.entity.annotation.JSONEntity;
import ru.introguzzle.parser.json.mapping.serialization.Bindable;

@AllArgsConstructor
@JSONEntity(constructorArguments = {"name"})
@EqualsAndHashCode
public class Test implements Bindable {
    private String name;

    @Override
    public JSONObject toJSONObject() {
        return Bindable.super.toJSONObject();
    }
}