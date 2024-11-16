package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.parse.JSONParser;

import java.util.ArrayList;
import java.util.List;

public class Example3 {
    @RequiredArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("firstName"),
            @ConstructorArgument("lastName"),
            @ConstructorArgument("email"),
            @ConstructorArgument("website"),
            @ConstructorArgument("address"),
    })
    @Getter
    @ToString
    public static class Customer {
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String website;
        private final Address address;
    }

    @AllArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("street"),
            @ConstructorArgument("city"),
            @ConstructorArgument("country")
    })
    @Getter
    @ToString
    public static class Address {
        private final String street;
        private final String city;
        private final String country;
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        JSONArray array = parser.parse(getData(), JSONArray.class);
        List<Customer> list = ObjectMapper.newMethodHandleMapper().toCollection(array, Customer.class, ArrayList::new);
        list.forEach(System.out::println);
    }

    private static String getData() {
        return """
                [
                  {
                    "first_name": "Simple",
                    "last_name": "Solution",
                    "email": "contact@simplesolution.dev",
                    "website": "https://simplesolution.dev",
                    "address": {
                      "street": "Simple Street",
                      "city": "City Name",
                      "country": "Country Name"
                    }
                  },
                  {
                    "first_name": "Java",
                    "last_name": "Tutorial",
                    "email": "java@simplesolution.dev",
                    "website": "https://simplesolution.dev",
                    "address": {
                      "street": "Test Street",
                      "city": "City Name",
                      "country": "Country Name"
                    }
                  },
                  {
                    "first_name": "Gson",
                    "last_name": "Learn",
                    "email": "gson@simplesolution.dev",
                    "website": "https://simplesolution.dev/tag/gson",
                    "address": {
                      "street": "Gson Street",
                      "city": "City Name",
                      "country": "Country Name"
                    }
                  }
                ]""";
    }
}
