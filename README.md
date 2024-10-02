# JSONParser

## Overview

`JSONParser` is a lightweight library for parsing JSON strings into Java objects. It supports various data types including `JSONObject`, `JSONArray`, and primitive types. The library provides a simple interface for validation and conversion, making it easy to work with JSON data in Java applications.

## Features

- **Flexible Parsing**: Supports parsing to various Java types including `JSONObject`, `JSONArray`, and `String`.
- **POJO Mapping**: Allows for easy mapping between `JSONObject` and Plain Old Java Objects (POJO), enabling seamless conversion between JSON data and Java objects.
- **Custom Validation**: Uses a bracket validator to ensure that the JSON structure is valid before parsing.
- **Extensible Converter**: Allows for custom conversion logic through the `Converter` interface.

## Installation

To use `JSONParser` in your project, add the following dependency to your `build.gradle` (for Gradle users):

```groovy
dependencies {
    implementation 'ru.introguzzle:jsonparser:<version>'
}

