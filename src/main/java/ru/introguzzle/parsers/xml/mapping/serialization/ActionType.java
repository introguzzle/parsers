package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

@Getter
@AllArgsConstructor
public enum ActionType {
    ADD_CHILD(XMLType.ELEMENT),
    ADD_ATTRIBUTE(XMLType.ATTRIBUTE),
    ADD_CHARACTER_DATA(XMLType.CHARACTER_DATA);

    private final XMLType type;
}
