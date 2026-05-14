package com.codelab.app.ui.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CodeInsightEngine {

    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?:public|private|protected|internal|static|final|suspend|async|fun|void|int|long|double|float|boolean|char|String|List<.*?>|Map<.*?>|[A-Z][A-Za-z0-9_<>]*)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

    private CodeInsightEngine() {}

    public static String normalizeScannedCode(String raw) {
        if (raw == null) return "";
        String text = raw.replace("\r", "");
        text = text.replace('“', '"').replace('”', '"');
        text = text.replace('‘', '\'').replace('’', '\'');
        text = text.replace('—', '-').replace('–', '-');
        text = text.replace('\t', ' ');

        String[] lines = text.split("\n");
        StringBuilder cleaned = new StringBuilder();
        boolean previousBlank = false;
        for (String line : lines) {
            String trimmedRight = rtrim(line);
            String normalized = trimmedRight
                    .replace("{|", "{")
                    .replace("|}", "}")
                    .replace("();", "();")
                    .replace(" .", ".")
                    .replace("( ", "(")
                    .replace(" )", ")");
            boolean blank = normalized.trim().isEmpty();
            if (blank && previousBlank) {
                continue;
            }
            cleaned.append(normalized.trim().isEmpty() ? "" : normalized).append('\n');
            previousBlank = blank;
        }
        return cleaned.toString().trim();
    }

    public static String explain(String code) {
        String normalized = normalizeScannedCode(code);
        if (normalized.isEmpty()) {
            return "No code was provided. Paste or scan a function/class first.";
        }

        String lower = normalized.toLowerCase(Locale.US);
        List<String> bullets = new ArrayList<>();

        String className = findFirst(CLASS_PATTERN, normalized);
        String methodName = findFirst(METHOD_PATTERN, normalized);

        if (className != null) {
            bullets.add("This defines a class named " + className + ". It groups related state and behavior into one reusable unit.");
        }
        if (methodName != null) {
            bullets.add("The main behavior appears to be in the function/method " + methodName + "(). That is likely the entry point of the pasted snippet.");
        }
        if (lower.contains("for (") || lower.contains("for(")) {
            bullets.add("It uses a for-loop, so part of the logic repeats over a list, range, or collection of values.");
        }
        if (lower.contains("while (") || lower.contains("while(")) {
            bullets.add("It uses a while-loop, meaning the code keeps running until a condition becomes false.");
        }
        if (lower.contains("if (") || lower.contains("if(")) {
            bullets.add("It contains conditional logic with if-statements, so the output changes depending on runtime conditions.");
        }
        if (lower.contains("return ")) {
            bullets.add("It returns a value, so this snippet is designed to produce a result for other code to use.");
        }
        if (lower.contains("system.out.print") || lower.contains("println") || lower.contains("console.log") || lower.contains("print(")) {
            bullets.add("It prints output, which suggests the code is showing a result, logging progress, or helping with debugging.");
        }
        if (lower.contains("try {") || lower.contains("catch (") || lower.contains("catch(")) {
            bullets.add("It includes exception/error handling, which helps prevent the app or program from crashing when something unexpected happens.");
        }
        if (lower.contains("new ")) {
            bullets.add("It creates at least one new object, so the snippet is instantiating dependencies or building data structures at runtime.");
        }
        if (lower.contains("list<") || lower.contains("arraylist") || lower.contains("map<") || lower.contains("hashmap") || lower.contains("[]")) {
            bullets.add("It works with a collection or array, so it is likely storing, transforming, or scanning multiple values.");
        }

        if (bullets.isEmpty()) {
            bullets.add("This snippet is valid-looking code, but its purpose is general. It likely defines structure or simple behavior without strong clues such as loops, output, or branching.");
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Summary\n");
        if (className != null && methodName != null) {
            explanation.append("This snippet defines class ").append(className)
                    .append(" and includes behavior centered around ").append(methodName).append("().\n\n");
        } else if (className != null) {
            explanation.append("This snippet defines class ").append(className).append(".\n\n");
        } else if (methodName != null) {
            explanation.append("This snippet mainly contains method ").append(methodName).append("().\n\n");
        } else {
            explanation.append("This snippet appears to be a code fragment or utility block.\n\n");
        }

        explanation.append("What it does\n");
        for (int i = 0; i < bullets.size(); i++) {
            explanation.append("• ").append(bullets.get(i)).append('\n');
        }

        explanation.append("\nLearning note\n");
        explanation.append("Check the control flow first (if/for/while), then identify inputs, side effects, and return values. That is usually the fastest way to understand unfamiliar code.");
        return explanation.toString().trim();
    }

    private static String findFirst(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String rtrim(String value) {
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end);
    }
}
