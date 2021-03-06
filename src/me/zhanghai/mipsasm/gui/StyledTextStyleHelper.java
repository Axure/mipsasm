/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.mipsasm.gui;

import me.zhanghai.mipsasm.assembler.DirectiveInformation;
import me.zhanghai.mipsasm.assembler.OperationInformation;
import me.zhanghai.mipsasm.assembler.Register;
import me.zhanghai.mipsasm.parser.Tokens;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyledTextStyleHelper {

    private static final StyleRangeTemplate TRAILING_WHITESPACE_STYLE = new StyleRangeTemplate(
            new StyleRangeTemplate.UnderlineStyle(new Color(Display.getCurrent(), 0xF9, 0x26, 0x72),
                    SWT.UNDERLINE_SQUIGGLE));

    private static final StyleRangeTemplate COMMENT_STYLE = new StyleRangeTemplate(
            new Color(Display.getCurrent(), 0x75, 0x71, 0x51));

    private static final StyleRangeTemplate STATEMENT_SEPARATOR_STYLE = new StyleRangeTemplate(
            new Color(Display.getCurrent(), 0xF9, 0x26, 0x72));

    private static final String[] PUNCTUATION_REGEXS = new String [] {
            ":",
            ",",
            "\\(",
            "\\)"
    };

    private static final StyleRangeTemplate PUNCTUATION_STYLE = STATEMENT_SEPARATOR_STYLE;

    private static final String TOKEN_SEPARATOR_REGEX;
    static {
        StringBuilder builder = new StringBuilder();
        for (String punctuationRegex : PUNCTUATION_REGEXS) {
            builder.append("(\\s*")
                    .append(punctuationRegex)
                    .append("\\s*)|");
        }
        builder.append("(\\s+)");
        TOKEN_SEPARATOR_REGEX = builder.toString();
    }

    private static final List<String> KEYWORD;
    static {
        KEYWORD = new ArrayList<>();
        for (OperationInformation operationInformation : OperationInformation.values()) {
            KEYWORD.add(operationInformation.name());
        }
        for (DirectiveInformation directiveInformation : DirectiveInformation.values()) {
            KEYWORD.add("." + directiveInformation.name());
        }
    }
    private static final StyleRangeTemplate KEYWORD_STYLE = new StyleRangeTemplate(
            new Color(Display.getCurrent(), 0xA6, 0xE2, 0x2E), SWT.BOLD);

    private static final List<String> REGISTERS;
    static {
        REGISTERS = new ArrayList<>();
        for (Register register : Register.values()) {
            REGISTERS.add("$" + register.ordinal());
            REGISTERS.add("$" + register.name());
        }
    }

    private static final StyleRangeTemplate REGISTER_STYLE = new StyleRangeTemplate(
            new Color(Display.getCurrent(), 0xFD, 0x97, 0x1F));

    private static final StyleRangeTemplate IMMEDIATE_STYLE = new StyleRangeTemplate(
            new Color(Display.getCurrent(), 0xAE, 0x81, 0xFF));

    public static void setup(final StyledText styledText) {
        styledText.addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(LineStyleEvent event) {
                setStyleRangeListForEvent(event, styledText);
            }
        });
    }

    private static void setStyleRangeListForEvent(LineStyleEvent event, StyledText styledText) {

        List<StyleRange> styleRangeList = new ArrayList<>();

        // Workaround case for String.indexOf().
        String line = event.lineText.toUpperCase();

        Matcher trailingSpaceMatcher = Pattern.compile("\\s+$").matcher(line);
        if (trailingSpaceMatcher.find()) {
            styleRangeList.add(TRAILING_WHITESPACE_STYLE.forRange(
                    event.lineOffset + trailingSpaceMatcher.start(),
                    trailingSpaceMatcher.end() - trailingSpaceMatcher.start()));
            line = line.substring(0, trailingSpaceMatcher.start());
        }

        Matcher commentMatcher = Pattern.compile(Tokens.COMMENT_REGEX).matcher(line);
        if (commentMatcher.find()) {
            styleRangeList.add(COMMENT_STYLE.forRange(event.lineOffset + commentMatcher.start(),
                    commentMatcher.end() - commentMatcher.start()));
            line = line.substring(0, commentMatcher.start());
        }

        Matcher statementSeparatorMatcher = Pattern.compile(Tokens.STATEMENT_SEPARATOR_REGEX).matcher(line);
        int statementStart = 0;
        while (true) {
            boolean statementSeparatorFound = statementSeparatorMatcher.find();
            String statement;
            if (statementSeparatorFound) {
                statement = line.substring(statementStart, statementSeparatorMatcher.start());
            } else {
                statement = line.substring(statementStart, line.length());
            }
            addStyleForStatement(statement, event.lineOffset + statementStart, styleRangeList);
            if (statementSeparatorFound) {
                styleRangeList.add(STATEMENT_SEPARATOR_STYLE.forRange(
                        event.lineOffset + statementSeparatorMatcher.start(),
                        statementSeparatorMatcher.end() - statementSeparatorMatcher.start()));
                statementStart = statementSeparatorMatcher.end();
            } else {
                break;
            }
        }

        // StyleRange[] must be sorted, according to documentation, or behavior will be weird.
        sortStyleRangeList(styleRangeList);

        event.styles = new StyleRange[styleRangeList.size()];
        styleRangeList.toArray(event.styles);
    }

    private static void addStyleForStatement(String statement, int offset, List<StyleRange> styleRangeList) {
        Matcher tokenSeparatorMatcher = Pattern.compile(TOKEN_SEPARATOR_REGEX).matcher(statement);
        int tokenStart = 0;
        while (true) {
            boolean tokenSeparatorFound = tokenSeparatorMatcher.find();
            String token;
            if (tokenSeparatorFound) {
                token = statement.substring(tokenStart, tokenSeparatorMatcher.start());
            } else {
                token = statement.substring(tokenStart, statement.length());
            }
            addStyleForToken(token, offset + tokenStart, styleRangeList);
            if (tokenSeparatorFound) {
                addStyleForPunctuation(tokenSeparatorMatcher.group(), offset + tokenSeparatorMatcher.start(),
                        styleRangeList);
                tokenStart = tokenSeparatorMatcher.end();
            } else {
                break;
            }
        }
    }

    private static void addStyleForToken(String token, int offset, List<StyleRange> styleRangeList) {

        for (String keyword : KEYWORD) {
            if (token.equals(keyword)) {
                styleRangeList.add(KEYWORD_STYLE.forRange(offset, token.length()));
            }
        }

        for (String register : REGISTERS) {
            if (token.equals(register)) {
                styleRangeList.add(REGISTER_STYLE.forRange(offset, token.length()));
                return;
            }
        }

        Matcher numberLiteralMatcher = Pattern.compile("(0X[0-9A-F]{1,8})|(0[0-7]{1,11})|(0B[01]{1,32})|(\\d{1,10})")
                .matcher(token);
        if (numberLiteralMatcher.matches()) {
            styleRangeList.add(IMMEDIATE_STYLE.forRange(offset, token.length()));
        }
    }

    private static void addStyleForPunctuation(String tokenSeparator, int offset, List<StyleRange> styleRangeList) {
        for (String punctuationRegex : PUNCTUATION_REGEXS) {
            Matcher punctuationMatcher = Pattern.compile(punctuationRegex).matcher(tokenSeparator);
            while (punctuationMatcher.find()) {
                styleRangeList.add(PUNCTUATION_STYLE.forRange(offset + punctuationMatcher.start(),
                        punctuationMatcher.end() - punctuationMatcher.start()));
            }
        }
    }

    private static void sortStyleRangeList(List<StyleRange> styleRangeList) {
        Collections.sort(styleRangeList, new Comparator<StyleRange>() {
            @Override
            public int compare(StyleRange left, StyleRange right) {
                int result = left.start - right.start;
                if (result == 0) {
                    result = left.length - right.length;
                }
                return result;
            }
        });
    }

    private static class StyleRangeTemplate {

        private Color foreground;
        private Color background;
        private int fontStyle;
        private UnderlineStyle underlineStyle;

        public StyleRangeTemplate(Color foreground, Color background, int fontStyle, UnderlineStyle underlineStyle) {
            this.foreground = foreground;
            this.background = background;
            this.fontStyle = fontStyle;
            this.underlineStyle = underlineStyle;
        }

        public StyleRangeTemplate(UnderlineStyle underlineStyle) {
            this(null, null, SWT.NORMAL, underlineStyle);
        }

        public StyleRangeTemplate(Color foreground, int fontStyle) {
            this(foreground, null, fontStyle, new UnderlineStyle());
        }

        public StyleRangeTemplate(Color foreground) {
            this(foreground, SWT.NORMAL);
        }

        public StyleRange forRange(int start, int length) {
            StyleRange styleRange = new StyleRange(start, length, foreground, background, fontStyle);
            underlineStyle.applyTo(styleRange);
            return styleRange;
        }

        private static class UnderlineStyle {
            private boolean underline;
            private Color color;
            private int style;

            private UnderlineStyle(boolean underline, Color color, int style) {
                this.underline = underline;
                this.color = color;
                this.style = style;
            }

            public UnderlineStyle() {
                this(false, null, SWT.SINGLE);
            }

            public UnderlineStyle(Color color, int style) {
                this(true, color, style);
            }

            public UnderlineStyle(Color color) {
                this(color, SWT.SINGLE);
            }

            public void applyTo(StyleRange styleRange) {
                styleRange.underline = underline;
                styleRange.underlineColor = color;
                styleRange.underlineStyle = style;
            }
        }
    }
}
