package ru.introguzzle.parsers.example;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapper;

public class Example2 {
    @RequiredArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("value")
    })
    @XMLRoot("box")
    @ToString
    public static class Box<T> {
        @XMLField(type = XMLType.ELEMENT, name = "value")
        final T value;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Box<Integer> box = new Box<>(10);
        XMLDocument document = XMLMapper.newMapper().toXMLDocument(box);
        ObjectMapper objectMapper = ObjectMapper.newMapper();
        Box<Integer> after = (Box<Integer>) objectMapper.toObject(document, Box.class);

        System.out.println(after);
    }
}
