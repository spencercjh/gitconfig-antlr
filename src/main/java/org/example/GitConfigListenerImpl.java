package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GitConfigListenerImpl extends GitConfigBaseListener {
    private static final Pattern DEPRECATED_SECTION_HEADER = Pattern.compile("(\\w+)\\.(\\w+)");
    /**
     * key is section name, value is a map of key-value pairs
     */
    @Getter
    private Map<String, Map<String, Object>> allConfigs;
    private Map<String, Object> currentSection;

    private static String removeQuotes(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    @Override
    public void enterGitconfig(GitConfigParser.GitconfigContext ctx) {
        allConfigs = new HashMap<>();
    }

    @Override
    public void enterSectionHeader(GitConfigParser.SectionHeaderContext ctx) {
        log.debug("enterSectionHeader: {}", ctx.getText());
        if (ctx.string().size() == 1) {
            final String sectionName = ctx.string().getFirst().getText();
            // There is also a deprecated [section.subsection] syntax.
            final Matcher matcher = DEPRECATED_SECTION_HEADER.matcher(sectionName);
            if (matcher.matches()) {
                log.warn("deprecated section header: {}", sectionName);
                assignSubSection(matcher.group(1), matcher.group(2));
                return;
            }
            currentSection = allConfigs.computeIfAbsent(removeQuotes(sectionName), s -> new HashMap<>());
        } else if (ctx.string().size() == 2) {
            final String sectionName = ctx.string().getFirst().getText();
            final String subSectionName = ctx.string().getLast().getText();
            assignSubSection(sectionName, subSectionName);
        } else {
            throw new RuntimeException("section header is not valid: %s".formatted(ctx.getText()));
        }
    }

    private void assignSubSection(String sectionName, String subSectionName) {
        final Map<String, Object> subSection = allConfigs.computeIfAbsent(removeQuotes(sectionName), s -> new HashMap<>());
        //noinspection unchecked
        currentSection = (Map<String, Object>) subSection.computeIfAbsent(removeQuotes(subSectionName), s -> new HashMap<>());
    }

    @Override
    public void enterLine(GitConfigParser.LineContext ctx) {
        log.debug("enterLine: {}", ctx.getText());
        if (ctx.stringList().size() != 2) {
            log.warn("key-value is not valid: {}", ctx.getText());
            return;
        }
        if (currentSection != null) {
            final String key = ctx.stringList().getFirst().getText();
            final String value = ctx.stringList().getLast().getText();
            if (key.isBlank() || value.isBlank()) {
                log.warn("key or value is blank: {}", ctx.getText());
                return;
            }
            currentSection.put(key, value);
        }
    }

    @Override
    public void exitSection(GitConfigParser.SectionContext ctx) {
        log.debug("exitSection: {}", ctx.getText());
        currentSection = null;
    }

    @Override
    public void exitLine(GitConfigParser.LineContext ctx) {
        log.debug("exitLine: {}", ctx.getText());
    }
}
