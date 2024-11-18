package ru.introguzzle.parsers.xml.entity;

import org.jetbrains.annotations.NotNull;

public interface XMLStringConvertable {
    /**
     * Converts this object to XML format in pretty-print form
     * @return XML
     */
    @NotNull String toXMLString();

    /**
     * Converts this object to compact XML format
     * @return
     */
    @NotNull String toXMLStringCompact();
}
