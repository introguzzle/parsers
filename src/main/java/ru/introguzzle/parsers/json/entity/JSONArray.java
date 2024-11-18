package ru.introguzzle.parsers.json.entity;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.util.UntypedArray;
import ru.introguzzle.parsers.common.visit.Visitable;
import ru.introguzzle.parsers.common.visit.Visitor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents a JSON array as a list of objects.
 * <p>
 * This class extends {@link UntypedArray} and provides functionality for storing
 * and manipulating JSON array data. It implements the {@link JSONStringConvertable}
 * interface for conversion to JSON string representation and supports the visitor pattern
 * via the {@link Visitable} interface.
 * </p>
 *
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Store and retrieve elements of various permitted types.</li>
 *   <li>Type-specific getters for common JSON data types.</li>
 *   <li>Customizable addition of elements with type checking.</li>
 *   <li>Conversion to {@link JSONObject} using key extraction and remapping functions.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * JSONArray jsonArray = new JSONArray();
 * jsonArray.add("String value");
 * jsonArray.add(42);
 * jsonArray.add(true);
 *
 * // Access elements
 * String stringValue = jsonArray.getString(0); // "String value"
 * Number numberValue = jsonArray.getNumber(1); // 42
 * Boolean booleanValue = jsonArray.getBoolean(2); // true
 * );
 * }</pre>
 *
 * <h5>
 * Note: all methods that are related to storing elements may throw {@link IllegalArgumentException}
 * if class of element to be stored is not permitted according to {@linkplain JSONArray#PERMITTED_CLASSES}.
 * This behaviour depends on boolean entityValidationEnabled flag in configuration
 * </h5>
 *
 * @see UntypedArray
 * @see JSONStringConvertable
 * @see Visitable
 * @see JSONObject
 */
@SuppressWarnings("unused")
public class JSONArray extends UntypedArray implements
        JSONStringConvertable, Visitable<JSONArray, Visitor<JSONArray>>, Serializable {

    @Serial
    private static final long serialVersionUID = -1731069894963023770L;

    /**
     * A set of classes that are permitted to be stored in the {@code JSONArray}.
     * This includes {@code Number}, {@code String}, {@code JSONObject}, {@code JSONArray},
     * {@code Boolean}, and {@code CircularReference}.
     */
    public static final Set<Class<?>> PERMITTED_CLASSES;

    static {
        PERMITTED_CLASSES = Validation.PERMITTED_CLASSES;
    }

    /**
     * Creates a new {@code JSONArray} containing the specified items.
     *
     * @param items the items to be added to the new array
     * @return a new {@code JSONArray} containing the specified items
     * @throws IllegalArgumentException if class of any element of {@code items} is not permitted
     */
    public static JSONArray of(Object... items) {
        return new JSONArray(List.of(items));
    }

    /**
     * Constructs an empty {@code JSONArray}.
     */
    public JSONArray() {
        super();
    }

    /**
     * Constructs a {@code JSONArray} initialized with the elements of the specified collection.
     *
     * @param collection the collection whose elements are to be placed into this array
     * @throws IllegalArgumentException if class of any element of {@code collection} is not permitted
     */
    public JSONArray(@NotNull Collection<?> collection) {
        super(Validation.requirePermittedType(collection));
    }

    /**
     * Constructs a {@code JSONArray} initialized with the elements of the specified array.
     *
     * @param array the array whose elements are to be placed into this array
     * @throws IllegalArgumentException if class of any element of {@code array} is not permitted
     */
    public JSONArray(@NotNull Object[] array) {
        super(Validation.requirePermittedType(List.of(array)));
    }

    /**
     * Constructs a {@code JSONArray} initialized with the elements of the specified list.
     *
     * @param list the list whose elements are to be placed into this array
     * @throws IllegalArgumentException if class of any element of {@code list} is not permitted
     */
    public JSONArray(@NotNull List<?> list) {
        super(Validation.requirePermittedType(list));
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if class of any element of {@code c} is not permitted
     */
    @Override
    public boolean addAll(@NotNull Collection<?> c) {
        return super.addAll(Validation.requirePermittedType(c));
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if class of any element of {@code c} is not permitted
     */
    @Override
    public boolean addAll(int index, @NotNull Collection<?> c) {
        return super.addAll(index, Validation.requirePermittedType(c));
    }

    /**
     * Adds the specified element to the end of this array.
     * <p>
     * Only elements of permitted types can be added. Permitted types include
     * {@code Number}, {@code String}, {@code JSONObject}, {@code JSONArray},
     * {@code Boolean}, and {@code CircularReference}.
     * </p>
     *
     * @param element the element to be added
     * @return {@code true} if the element was added successfully
     * @throws IllegalArgumentException if the element's class is not permitted
     */
    @Override
    public boolean add(Object element) {
        return addChecked(element);
    }

    /**
     * Adds the specified element to the end of this array after checking its type.
     * <p>
     * Only elements of permitted types can be added. Permitted types include
     * {@code Number}, {@code String}, {@code JSONObject}, {@code JSONArray},
     * {@code Boolean}, and {@code CircularReference}.
     * </p>
     *
     * @param element the element to be added
     * @return {@code true} if the element was added successfully
     * @throws IllegalArgumentException if the class of {@code element} is not permitted
     */
    public boolean addChecked(Object element) {
        return super.add(Validation.requirePermittedType(element, EntityUnion.ARRAY));
    }

    /**
     * Retrieves the element at the specified position in this array as a {@code Boolean}.
     *
     * @param index index of the element to return
     * @return the element at the specified position as a {@code Boolean}
     * @throws ClassCastException if the element is not a {@code Boolean}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Boolean getBoolean(int index) {
        return get(index, Boolean.class);
    }

    /**
     * Retrieves the element at the specified position in this array as a {@code Number}.
     *
     * @param index index of the element to return
     * @return the element at the specified position as a {@code Number}
     * @throws ClassCastException if the element is not a {@code Number}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Number getNumber(int index) {
        return get(index, Number.class);
    }

    /**
     * Retrieves the element at the specified position in this array as a {@code String}.
     *
     * @param index index of the element to return
     * @return the element at the specified position as a {@code String}
     * @throws ClassCastException if the element is not a {@code String}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public String getString(int index) {
        return get(index, String.class);
    }

    /**
     * Retrieves the element at the specified position in this array as a {@code JSONArray}.
     *
     * @param index index of the element to return
     * @return the element at the specified position as a {@code JSONArray}
     * @throws ClassCastException if the element is not a {@code JSONArray}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public JSONArray getArray(int index) {
        return get(index, JSONArray.class);
    }

    /**
     * Retrieves the element at the specified position in this array as a {@code JSONObject}.
     *
     * @param index index of the element to return
     * @return the element at the specified position as a {@code JSONObject}
     * @throws ClassCastException if the element is not a {@code JSONObject}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public JSONObject getObject(int index) {
        return get(index, JSONObject.class);
    }

    /**
     * Returns an iterator over the elements in this array.
     *
     * @return an iterator over the elements in this array
     */
    @Override
    public Iterator<?> getIterator() {
        return iterator();
    }

    /**
     * Returns the opening symbol for this JSON structure, which is "[".
     *
     * @return the opening symbol "["
     */
    @Override
    public String getOpeningSymbol() {
        return "[";
    }

    /**
     * Returns the closing symbol for this JSON structure, which is "]".
     *
     * @return the closing symbol "]"
     */
    @Override
    public String getClosingSymbol() {
        return "]";
    }
}
