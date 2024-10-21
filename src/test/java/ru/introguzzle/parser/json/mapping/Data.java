package ru.introguzzle.parser.json.mapping;

import java.util.*;

public final class Data {
    private Data() {}

    public static class Transient {
        private transient String stringString;
        private transient int intInt;

        public Transient() {

        }

        public Transient(String stringString, int intInt) {
            this.stringString = stringString;
            this.intInt = intInt;
        }
    }

    public static class Base {
        public String string;
        public byte b;

        public Base() {

        }

        public Base(String string, byte b) {
            this.string = string;
            this.b = b;
        }
    }

    public static class Inheriting extends Base {
        public double d;

        public Inheriting() {

        }

        public Inheriting(String string, byte b, double d) {
            super(string, b);
            this.d = d;
        }
    }

    public static class RecursiveSelf {
        public byte bytes;
        public String string;
        public RecursiveSelf next;

        public RecursiveSelf() {}

        public RecursiveSelf(RecursiveSelf next) {
            this.next = next;
        }
    }

    public static final class RecursiveRef {
        private String name;
        private int age;
        private String email;
        private RecursiveRef parent;

        public RecursiveRef() {
        }

        public RecursiveRef(String name,
                            int age,
                            String email,
                            RecursiveRef parent) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.parent = parent;
        }

        public String name() {
            return name;
        }

        public int age() {
            return age;
        }

        public String email() {
            return email;
        }

        public RecursiveRef parent() {
            return parent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RecursiveRef) obj;
            return Objects.equals(this.name, that.name) &&
                    this.age == that.age &&
                    Objects.equals(this.email, that.email) &&
                    Objects.equals(this.parent, that.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, email, parent);
        }

        @Override
        public String toString() {
            return "RecursiveRef[" +
                    "name=" + name + ", " +
                    "age=" + age + ", " +
                    "email=" + email + ", " +
                    "parent=" + parent + ']';
        }
    }

    public static final class RecursiveList {
        private String name;
        private int age;
        private String email;
        private List<RecursiveList> list;

        public RecursiveList() {
        }

        public RecursiveList(String name,
                             int age,
                             String email,
                             List<RecursiveList> list) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.list = list;
        }

        public String name() {
            return name;
        }

        public int age() {
            return age;
        }

        public String email() {
            return email;
        }

        public List<RecursiveList> list() {
            return list;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RecursiveList) obj;
            return Objects.equals(this.name, that.name) &&
                    this.age == that.age &&
                    Objects.equals(this.email, that.email) &&
                    Objects.equals(this.list, that.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, email, list);
        }

        @Override
        public String toString() {
            return "RecursiveList[" +
                    "name=" + name + ", " +
                    "age=" + age + ", " +
                    "email=" + email + ", " +
                    "list=" + list + ']';
        }
    }

    public static final class RecursiveArray {
        private String name;
        private int age;
        private String email;
        private RecursiveArray[] array;

        public RecursiveArray() {
        }

        public RecursiveArray(String name,
                              int age,
                              String email,
                              RecursiveArray[] array) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.array = array;
        }

        public String name() {
            return name;
        }

        public int age() {
            return age;
        }

        public String email() {
            return email;
        }

        public RecursiveArray[] array() {
            return array;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RecursiveArray) obj;
            return Objects.equals(this.name, that.name) &&
                    this.age == that.age &&
                    Objects.equals(this.email, that.email) &&
                    Objects.equals(this.array, that.array);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, email, array);
        }

        @Override
        public String toString() {
            return "RecursiveArray[" +
                    "name=" + name + ", " +
                    "age=" + age + ", " +
                    "email=" + email + ", " +
                    "array=" + array + ']';
        }
    }

    public static final class ListOfStrings {
        private List<String> strings;

        public ListOfStrings() {

        }

        public ListOfStrings(List<String> strings) {
            this.strings = strings;
        }

        public List<String> strings() {
            return strings;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ListOfStrings) obj;
            return Objects.equals(this.strings, that.strings);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strings);
        }

        @Override
        public String toString() {
            return "ListOfStrings[" +
                    "strings=" + strings + ']';
        }
    }

    public static final class ListOfIntegers {
        private Set<Integer> integers;

        public ListOfIntegers() {

        }

        public ListOfIntegers(Set<Integer> integers) {
            this.integers = integers;
        }

        public Set<Integer> integers() {
            return integers;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ListOfIntegers) obj;
            return Objects.equals(this.integers, that.integers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(integers);
        }

        @Override
        public String toString() {
            return "ListOfIntegers[" +
                    "integers=" + integers + ']';
        }

    }

    public static class Person {
        private int age;
        private String name;
        private double salary;
        private boolean deleted;

        public Person() {
        }

        public Person(int age, String name, double salary, boolean deleted) {
            this.age = age;
            this.name = name;
            this.salary = salary;
            this.deleted = deleted;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    ", salary=" + salary +
                    ", deleted=" + deleted +
                    '}';
        }
    }

    public static class PersonBoxed {
        private Integer age;
        private String name;
        private Double salary;
        private Boolean deleted;

        public PersonBoxed() {
        }

        public PersonBoxed(Integer age, String name, Double salary, Boolean deleted) {
            this.age = age;
            this.name = name;
            this.salary = salary;
            this.deleted = deleted;
        }

        @Override
        public String toString() {
            return "PersonBoxed{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    ", salary=" + salary +
                    ", deleted=" + deleted +
                    '}';
        }
    }

    public static class PersonWithParent {
        private int age;
        private String name;
        private double salary;
        private boolean deleted;
        private PersonWithParent parent;
        private Person another;

        public PersonWithParent() {
        }

        public PersonWithParent(int age, String name, double salary, boolean deleted) {
            this.age = age;
            this.name = name;
            this.salary = salary;
            this.deleted = deleted;
        }

        public void setParent(PersonWithParent parent) {
            this.parent = parent;
        }

        public void setAnother(Person another) {
            this.another = another;
        }

        @Override
        public String toString() {
            return "PersonWithParent{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    ", salary=" + salary +
                    ", deleted=" + deleted +
                    ", parent=" + parent +
                    ", another=" + another +
                    '}';
        }
    }

    // Complex object

    public static class Employee {
        private String firstName;
        private String lastName;
        private String position;
        private double salary;
        private Date createdAt;
        private Date updatedAt;

        public Employee() {

        }

        // Конструктор
        public Employee(String firstName,
                        String lastName,
                        String position,
                        double salary) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.position = position;
            this.salary = salary;
            this.createdAt = new Date(); // устанавливаем текущее время
            this.updatedAt = new Date();
        }

        // Геттеры и сеттеры
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
            this.updatedAt = new Date();
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
            this.updatedAt = new Date();
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
            this.updatedAt = new Date();
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
            this.updatedAt = new Date();
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", position='" + position + '\'' +
                    ", salary=" + salary +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    '}';
        }
    }

    // Класс Company
    public static class Company {
        private String name;
        private String address;
        private List<Employee> employees = new ArrayList<>();
        private Object object;

        public Company() {

        }

        public Company(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public List<Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employee> employees) {
            this.employees = employees;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        @Override
        public String toString() {
            Object o = this == object
                    ? "CIRCULAR REFERENCE"
                    : object;

            return "Company{" +
                    "name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    ", employees=" + employees +
                    ", object=" + o +
                    '}';
        }
    }
}
