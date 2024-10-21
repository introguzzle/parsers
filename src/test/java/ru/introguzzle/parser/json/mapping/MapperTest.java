package ru.introguzzle.parser.json.mapping;

import org.junit.Test;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.context.CircularReferenceStrategy;
import ru.introguzzle.parser.json.mapping.context.MappingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapperTest {
    private final Mapper mapper = new MapperImpl();

    @Test
    public void test_recursive_ref() {
        var inner = new Data.RecursiveRef(
                "inner",
                42,
                "aaa@bbb.com",
                null
        );

        var data = new Data.RecursiveRef(
                "John Doe",
                10,
                "john.doe@example.com",
                inner
        );

        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.RecursiveRef.class));
    }

    @Test
    public void test_transient() {
        var data = new Data.Transient("John Doe", 42);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.Transient.class));
    }

    @Test
    public void test_inheriting() {
        var data = new Data.Inheriting("sss", (byte) 1, 333.3);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.Inheriting.class));
    }

    @Test
    public void test_recursive_list() {
        var listItem1 = new Data.RecursiveList(
                "John",
                25,
                "john@example.com",
                List.of()
        );

        var listItem2 = new Data.RecursiveList(
                "Shadow Fiend",
                999,
                "shadowfiend",
                List.of()
        );


        var data = new Data.RecursiveList(
                "Alice",
                30,
                "alice@example.com",
                List.of(listItem1, listItem2)
        );

        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.RecursiveList.class));
    }

    @Test
    public void test_recursive_array() {
        var arrayItem = new Data.RecursiveArray(
                "John",
                25,
                "john@example.com",
                null
        );

        var data = new Data.RecursiveArray(
                "Alice",
                30,
                "alice@example.com",
                new Data.RecursiveArray[]{arrayItem}
        );

        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.RecursiveArray.class));
    }

    @Test
    public void test_list_of_strings() {
        var data = new Data.ListOfStrings(List.of("1111", "2222", "3333"));
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.ListOfStrings.class));
    }

    @Test
    public void test_list_of_ints() {
        var data = new Data.ListOfIntegers(Set.of(1, 2, 3, 4));
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.ListOfIntegers.class));
    }

    @Test
    public void test_recursive_self_placeholder() {
        var data = new Data.RecursiveSelf(null);
        data.bytes = (byte) 13;
        data.string = "21921";
        data.next = data;
        JSONObject resultObject = mapper.toJSONObject(data, new MappingContext(
                CircularReferenceStrategy.USE_PLACEHOLDER
        ));

        System.out.println(resultObject.toJSONString());
        Data.RecursiveSelf map = resultObject.map(Data.RecursiveSelf.class);
        System.out.println(map);
    }

    @Test
    public void test_recursive_self_return_null() {
        var data = new Data.RecursiveSelf(null);
        data.bytes = (byte) 13;
        data.string = "21921";
        data.next = data;
        JSONObject resultObject = mapper.toJSONObject(data, new MappingContext(
                CircularReferenceStrategy.RETURN_NULL
        ));

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.RecursiveSelf.class));
    }

    @Test(expected = CircularReferenceException.class)
    public void test_recursive_self_throw_exception() {
        var data = new Data.RecursiveSelf(null);
        data.bytes = (byte) 13;
        data.string = "21921";
        data.next = data;
        JSONObject resultObject = mapper.toJSONObject(data, new MappingContext(
                CircularReferenceStrategy.THROW_EXCEPTION
        ));

        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.RecursiveSelf.class));
    }

    @Test
    public void test_person() {
        var data = new Data.Person(1, "1337", 20000, true);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);
        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.Person.class));
    }

    @Test
    public void test_person_boxed() {
        var data = new Data.PersonBoxed(1, "1337", 20000D, true);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);
        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.PersonBoxed.class));
    }

    @Test
    public void test_person_with_parent() {
        var data = new Data.PersonWithParent(999, "NAME", 12121D, false);
        var parent = new Data.PersonWithParent(888, "PARENT", 1.3, false);
        var another = new Data.Person(14, "ANOTHER", 13.3, false);

        data.setParent(parent);
        data.setAnother(another);

        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.DEFAULT);
        System.out.println(resultObject.toJSONString());
        System.out.println(resultObject.map(Data.PersonWithParent.class));
    }

    @Test
    public void test_company_with_employees() {
        var employee1 = new Data.Employee("John", "Doe", "Developer", 60000);
        var employee2 = new Data.Employee("Jane", "Smith", "Manager", 80000);
        var employee3 = new Data.Employee("Alice", "Johnson", "Designer", 50000);

        var company = new Data.Company("Tech Corp", "123 Tech Lane");
        company.getEmployees().add(employee1);
        company.getEmployees().add(employee2);
        company.getEmployees().add(employee3);
        company.getEmployees().add(employee1);
        company.setObject(company);

        // Сериализуем объект компании в JSONObject
        JSONObject resultObject = mapper.toJSONObject(company, new MappingContext(
                CircularReferenceStrategy.USE_PLACEHOLDER
        ));

        // Выводим результат сериализации
        System.out.println(resultObject.toJSONString());

        // Десериализуем обратно в объект Company
        var mappedCompany = resultObject.map(Data.Company.class);
        var mapped = resultObject.map(Map.class);

        // Выводим результат десериализации
        System.out.println(mappedCompany);
        System.out.println(mapped);
    }
}