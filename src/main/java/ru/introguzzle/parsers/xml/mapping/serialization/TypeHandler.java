package ru.introguzzle.parsers.xml.mapping.serialization;

import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.util.function.BiConsumer;

/**
 * Functional interface representing a handler that processes an input of type {@code T}
 * and applies it to an {@link XMLElement}. This interface extends {@link BiConsumer},
 * where the first parameter is the {@code XMLElement} to modify, and the second parameter
 * is the input data of type {@code T}.
 *
 * <p>This interface is typically used in XML serialization to customize how objects
 * of a certain type are converted and added to an XML element. Implementations define
 * how to manipulate the {@code XMLElement} based on the provided input, allowing for
 * flexible and extensible XML mapping strategies.
 *
 * @param <T> the type of the input to the handler
 */
public interface TypeHandler<T> extends BiConsumer<XMLElement, T> {

    /**
     * Performs this operation on the given {@code XMLElement} and input.
     *
     * <p>This method applies the processing logic to the specified XML element using
     * the provided input data. The implementation should define how the input modifies
     * or augments the XML element, such as adding attributes, child elements, or text content.
     *
     * @param element the XML element to which the input should be applied
     * @param input   the input data to process and apply to the XML element
     */
    @Override
    void accept(XMLElement element, T input);
}
