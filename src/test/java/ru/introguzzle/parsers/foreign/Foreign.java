package ru.introguzzle.parsers.foreign;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;

@AllArgsConstructor
@JSONEntity(constructorArguments = {
        @ConstructorArgument("name")
})
@EqualsAndHashCode
public class Foreign {
    private String name;
}
