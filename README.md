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

## Tech Details

Referring [INF file grammar](https://github.com/antlr/grammars-v4/blob/master/inf/inf.g4)
and [git configuration docs](https://git-scm.com/docs/git-config#_configuration_file), I
complete [`GitConfig.g4`](src/main/antlr4/org/example/GitConfig.g4).

Then I use ANTLR maven plugin to generate Java files, and
implement [`GitConfigListenerImpl`](src/main/java/org/example/GitConfigListenerImpl.java) to parse the gitconfig file.

Unlike INF files, I need to support the following gitconfig dialects:

- subsection header: `[remote "origin"]`
- deprecated subsection header: `[remote.origin]`
- colon `:` in the section value: `url = https://example.com/git` (Maybe INF file spec is really supported it, but I
  didn’t see it in the INF file grammar mentioned above)

Welcome to add more gitconfig syntax I was missing through Github issue, and I will update it ASAP.
