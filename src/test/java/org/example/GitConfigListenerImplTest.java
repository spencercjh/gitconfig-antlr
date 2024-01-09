package org.example;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class GitConfigListenerImplTest {
    private static GitConfigListenerImpl readGitConfig(String filename) {
        final File file = new File(new File("src/test/resources").getAbsolutePath(), filename);
        String configContent;
        try {
            configContent = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            log.error("failed to read file: {}", file.getAbsoluteFile(), e);
            return null;
        }
        final GitConfigLexer lexer = new GitConfigLexer(CharStreams.fromString(configContent));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final GitConfigParser parser = new GitConfigParser(tokens);

        final ParseTree tree = parser.gitconfig();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final GitConfigListenerImpl listener = new GitConfigListenerImpl();
        walker.walk(listener, tree);
        return listener;
    }

    @Test
    void testParseConfigFileOne() {
        final GitConfigListenerImpl listener = readGitConfig("testParseConfigFile.1");
        if (listener == null) {
            return;
        }
        final Map<String, Map<String, Object>> allConfigs = listener.getAllConfigs();
        assertEquals(allConfigs.size(), 6);
        assertEquals(allConfigs.get("core").size(), 2);
        assertEquals(allConfigs.get("core").get("filemode"), "false");
        assertEquals(allConfigs.get("core").get("gitProxy"), "default-proxy");

        assertEquals(allConfigs.get("diff").size(), 2);
        assertEquals(allConfigs.get("diff").get("external"), "/usr/local/bin/diff-wrapper");
        assertEquals(allConfigs.get("diff").get("renames"), "true");

        assertEquals(allConfigs.get("branch").size(), 1);
        //noinspection unchecked
        final Map<String, Object> devel = (Map<String, Object>) allConfigs.get("branch").get("devel");
        assertEquals(devel.size(), 2);
        assertEquals(devel.get("remote"), "origin");
        assertEquals(devel.get("merge"), "refs/heads/devel");

        assertEquals(allConfigs.get("include").size(), 1);
        assertEquals(allConfigs.get("include").get("path"), "~/foo.inc");

        final Map<String, Object> includeIf = allConfigs.get("includeIf");
        assertEquals(includeIf.size(), 5);

        //noinspection unchecked
        Map<String, Object> includeIfTemp = (Map<String, Object>) includeIf.get("gitdir:/path/to/foo/.git");
        assertEquals(includeIfTemp.size(), 1);
        assertEquals(includeIfTemp.get("path"), "/path/to/foo.inc");
        //noinspection unchecked
        includeIfTemp = (Map<String, Object>) includeIf.get("gitdir:~/to/group/");
        assertEquals(includeIfTemp.size(), 1);
        assertEquals(includeIfTemp.get("path"), "/path/to/foo.inc");
        //noinspection unchecked
        includeIfTemp = (Map<String, Object>) includeIf.get("gitdir:/path/to/group/");
        assertEquals(includeIfTemp.size(), 1);
        assertEquals(includeIfTemp.get("path"), "foo.inc");
        //noinspection unchecked
        includeIfTemp = (Map<String, Object>) includeIf.get("onbranch:foo-branch");
        assertEquals(includeIfTemp.size(), 1);
        assertEquals(includeIfTemp.get("path"), "foo.inc");
        //noinspection unchecked
        includeIfTemp = (Map<String, Object>) includeIf.get("hasconfig:remote.*.url:https://example.com/**");
        assertEquals(includeIfTemp.size(), 1);
        assertEquals(includeIfTemp.get("path"), "foo.inc");

        assertEquals(allConfigs.get("remote").size(), 1);
        //noinspection unchecked
        final Map<String, Object> origin = (Map<String, Object>) allConfigs.get("remote").get("origin");
        assertEquals(origin.size(), 1);
        assertEquals(origin.get("url"), "https://example.com/git");
    }


    @Test
    void testParseConfigFileTwo() {
        final GitConfigListenerImpl listener = readGitConfig("testParseConfigFile.2");
        if (listener == null) {
            return;
        }
        final Map<String, Map<String, Object>> allConfigs = listener.getAllConfigs();
        assertEquals(allConfigs.size(), 4);
        final Map<String, Object> core = allConfigs.get("core");
        assertEquals(core.size(), 1);
        assertEquals(core.get("filemode"), "false");

        final Map<String, Object> test = allConfigs.get("test");
        assertEquals(test.size(), 1);
        assertEquals(test.get("foo"), "bar");

        final Map<String, Object> foo = allConfigs.get("foo");
        assertEquals(foo.size(), 1);
        //noinspection unchecked
        final Map<String, Object> bar = (Map<String, Object>) foo.get("bar");
        assertEquals(bar.size(), 1);
        assertEquals(bar.get("key"), "value");

        //noinspection unchecked
        final Map<String, Object> fooBar = (Map<String, Object>) allConfigs.get("baz").get("bar");
        assertEquals(fooBar.size(), 1);
        assertEquals(fooBar.get("key"), "value1");
    }

    @Test
    void testParseConfigFileThree() {
        final GitConfigListenerImpl listener = readGitConfig("testParseConfigFile.3");
        if (listener == null) {
            return;
        }
        final Map<String, Map<String, Object>> allConfigs = listener.getAllConfigs();
        assertTrue(allConfigs.isEmpty());
    }
}