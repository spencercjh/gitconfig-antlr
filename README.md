# gitconfig-antlr

A demo to show how to use ANTLR to parse gitconfig file.

## Usage

Generate ANTLR Java files,

```bash
./mvnw clean package
```

then run the tests to verify the result.

```bash
./mvnw test
```

Please pay attention to the `src/test/java/org/example/GitConfigListenerImplTest.java` for assert details.