package com.medusa.lib;

import java.util.Stack;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
/**
 * A fluent API for formatting Strings. Canonical usage:
 * <pre>
 *  CharSequence chars = ColorPhrase.from("I'm<Chinese>,I love <China>").
 *                                                              withSeparator("<>").
 *                                                              innerColor(0xFFE6454A).
 *                                                              outerColor(0xFF666666).
 *                                                              format();
 * </pre>
 * <ul>
 * <li>Surround keys with curly braces; use two {{ to escape.</li>
 * <li>Fails fast on any mismatched keys.</li>
 * </ul>
 * The constructor parses the original pattern into a doubly-linked list of {@link Token}s.
 * These tokens do not modify the original pattern, thus preserving any spans.
 * <p/>
 * The {@link #format()} method iterates over the tokens, replacing and coloring the text as it iterates. The
 * doubly-linked list allows each token to ask its predecessor for the expanded length.
 */
public class ColorPhrase {
	/** The unmodified original pattern. */
	private final CharSequence pattern;
	/** Cached result after replacing all keys with corresponding values. */
	private CharSequence formatted;
	/**
	 * The constructor parses the original pattern into this doubly-linked list
	 * of tokens.
	 */
	private Token head;

	/** When parsing, this is the current character. */
	private char curChar;
	private String separator;// default "{}"
	private int curCharIndex;
	private int outerColor;// color that outside the separators,default 0xFF666666
	private int innerColor;// color that between the separators,default 0xFFA6454A
	/** Indicates parsing is complete. */
	private static final int EOF = 0;

	/**
	 * Entry point into this API.
	 * 
	 * @throws IllegalArgumentException
	 *             if pattern contains any syntax errors.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static ColorPhrase from(Fragment f, int patternResourceId) {
		return from(f.getResources(), patternResourceId);
	}

	/**
	 * Entry point into this API.
	 * 
	 * @throws IllegalArgumentException
	 *             if pattern contains any syntax errors.
	 */
	public static ColorPhrase from(View v, int patternResourceId) {
		return from(v.getResources(), patternResourceId);
	}

	/**
	 * Entry point into this API.
	 * 
	 * @throws IllegalArgumentException
	 *             if pattern contains any syntax errors.
	 */
	public static ColorPhrase from(Context c, int patternResourceId) {
		return from(c.getResources(), patternResourceId);
	}

	/**
	 * Entry point into this API.
	 * 
	 * @throws IllegalArgumentException
	 *             if pattern contains any syntax errors.
	 */
	public static ColorPhrase from(Resources r, int patternResourceId) {
		return from(r.getText(patternResourceId));
	}

	/**
	 * Entry point into this API; pattern must be non-null.
	 * 
	 * @throws IllegalArgumentException
	 *             if pattern contains any syntax errors.
	 */
	public static ColorPhrase from(CharSequence pattern) {
		return new ColorPhrase(pattern);
	}

	private ColorPhrase(CharSequence pattern) {
		curChar = (pattern.length() > 0) ? pattern.charAt(0) : EOF;

		this.pattern = pattern;
		// Invalidate the cached formatted text.
		formatted = null;
		separator = "{}";// initialize the default separator
		outerColor = 0xFF666666;//initialize the default value
		innerColor =0xFFE6454A;//initialize the default value
	}

	/**
	 * set the separator of the target，called after from() method.
	 * 
	 * @param _separator
	 * @return
	 */
	public ColorPhrase withSeparator(String _separator) {
		if (TextUtils.isEmpty(_separator)) {
			throw new IllegalArgumentException("separator must not be empty!");
		}
		if (_separator.length() > 2) {
			throw new IllegalArgumentException("separator‘s length must not be more than 3 charactors!");
		}
		this.separator = _separator;
		return this;
	}

	/**
	 * init the outerColor
	 * 
	 * @param _outerColor
	 * @return
	 */
	public ColorPhrase outerColor(int _outerColor) {
		this.outerColor = _outerColor;
		return this;
	}

	/**
	 * init the innerColor
	 * 
	 * @param _innerColor
	 * @return
	 */
	public ColorPhrase innerColor(int _innerColor) {
		this.innerColor = _innerColor;
		return this;
	}

	/**
	 * cut the pattern with the separators and linked them with double link
	 * structure;
	 */
	private void createDoubleLinkWithToken() {
		// A hand-coded lexer based on the idioms in
		// "Building Recognizers By Hand".
		// http://www.antlr2.org/book/byhand.pdf.
		Token prev = null;
		Token next;
		while ((next = token(prev)) != null) {
			// Creates a doubly-linked list of tokens starting with head.
			if (head == null)
				head = next;
			prev = next;
		}
	}

	/**
	 * Returns the next token from the input pattern, or null when finished
	 * parsing.
	 */
	private Token token(Token prev) {
		if (curChar == EOF) {
			return null;
		}
		if (curChar == getLeftSeparator()) {
			char nextChar = lookahead();
			if (nextChar == getLeftSeparator()) {
				return leftSeparator(prev);
			} else {
				return inner(prev);
			}
		}
		return outer(prev);
	}

	private char getLeftSeparator() {
		return separator.charAt(0);
	}

	private char getRightSeparator() {
		if (separator.length() == 2) {
			return separator.charAt(1);
		}
		return separator.charAt(0);
	}

	/**
	 * Returns the text after replacing all keys with values.
	 * 
	 * @throws IllegalArgumentException
	 *             if any keys are not replaced.
	 */
	public CharSequence format() {
		if (formatted == null) {
			if (!checkPattern()) {
				throw new IllegalStateException("the separators don't match in the pattern!");
			}
			createDoubleLinkWithToken();
			// Copy the original pattern to preserve all spans, such as bold,
			// italic, etc.
			SpannableStringBuilder sb = new SpannableStringBuilder(pattern);
			for (Token t = head; t != null; t = t.next) {
				t.expand(sb);
			}

			formatted = sb;
		}
		return formatted;
	}

	/**
	 * check if the pattern has legal separators
	 * 
	 * @return
	 */
	private boolean checkPattern() {
		if (pattern == null) {
			return false;
		}
		char leftSeparator = getLeftSeparator();
		char rightSeparator = getRightSeparator();
		Stack<Character> separatorStack = new Stack<Character>();
		for (int i = 0; i < pattern.length(); i++) {
			char cur = pattern.charAt(i);
			if (cur == leftSeparator) {
				separatorStack.push(cur);
			} else if (cur == rightSeparator) {
				if (!separatorStack.isEmpty() && (separatorStack.pop() == leftSeparator)) {
					continue;
				} else {
					return false;
				}
			}
		}
		return separatorStack.isEmpty();
	}

	private InnerToken inner(Token prev) {

		// Store keys as normal Strings; we don't want keys to contain spans.
		StringBuilder sb = new StringBuilder();

		// Consume the left separator.
		consume();
		char rightSeparator = getRightSeparator();
		while (curChar != rightSeparator && curChar != EOF) {
			sb.append(curChar);
			consume();
		}

		if (curChar == EOF) {
			throw new IllegalArgumentException("Missing closing separator");
		}
        //consume the right separator.
		consume();

		if (sb.length() == 0) {
			throw new IllegalStateException("Disallow empty content between separators,for example {}");
		}

		String key = sb.toString();
		return new InnerToken(prev, key, innerColor);
	}

	/** Consumes and returns a token for a sequence of text. */
	private OuterToken outer(Token prev) {
		int startIndex = curCharIndex;

		while (curChar != getLeftSeparator() && curChar != EOF) {
			consume();
		}
		return new OuterToken(prev, curCharIndex - startIndex, outerColor);
	}

	/**
	 * Consumes and returns a token representing two consecutive curly brackets.
	 */
	private LeftSeparatorToken leftSeparator(Token prev) {
		consume();
		consume();
		return new LeftSeparatorToken(prev, getLeftSeparator());
	}

	/** Returns the next character in the input pattern without advancing. */
	private char lookahead() {
		return curCharIndex < pattern.length() - 1 ? pattern.charAt(curCharIndex + 1) : EOF;
	}

	/**
	 * Advances the current character position without any error checking.
	 * Consuming beyond the end of the string can only happen if this parser
	 * contains a bug.
	 */
	private void consume() {
		curCharIndex++;
		curChar = (curCharIndex == pattern.length()) ? EOF : pattern.charAt(curCharIndex);
	}

	/**
	 * Returns the raw pattern without expanding keys; only useful for
	 * debugging. Does not pass through to {@link #format()} because doing so
	 * would drop all spans.
	 */
	@Override
	public String toString() {
		return pattern.toString();
	}

	private abstract static class Token {
		private final Token prev;
		private Token next;

		protected Token(Token prev) {
			this.prev = prev;
			if (prev != null)
				prev.next = this;
		}

		/** Replace text in {@code target} with this token's associated value. */
		abstract void expand(SpannableStringBuilder target);

		/** Returns the number of characters after expansion. */
		abstract int getFormattedLength();

		/** Returns the character index after expansion. */
		final int getFormattedStart() {
			if (prev == null) {
				// The first token.
				return 0;
			} else {
				// Recursively ask the predecessor node for the starting index.
				return prev.getFormattedStart() + prev.getFormattedLength();
			}
		}
	}

	/** Ordinary text between tokens. */
	private static class OuterToken extends Token {
		private final int textLength;
		private int color;

		OuterToken(Token prev, int textLength, int _color) {
			super(prev);
			this.textLength = textLength;
			this.color = _color;
		}

		@Override
		void expand(SpannableStringBuilder target) {

			int startPoint = getFormattedStart();
			int endPoint = startPoint + textLength;
			target.setSpan(new ForegroundColorSpan(color), startPoint, endPoint, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		@Override
		int getFormattedLength() {
			return textLength;
		}
	}

	/** A sequence of two curly brackets. */
	private static class LeftSeparatorToken extends Token {
		private char leftSeparetor;

		LeftSeparatorToken(Token prev, char _leftSeparator) {
			super(prev);
			leftSeparetor = _leftSeparator;
		}

		@Override
		void expand(SpannableStringBuilder target) {
			int start = getFormattedStart();
			target.replace(start, start + 2, String.valueOf(leftSeparetor));
		}

		@Override
		int getFormattedLength() {
			// for example,,Replace "{{" with "{".
			return 1;
		}
	}

	private static class InnerToken extends Token {
		/** The InnerText without separators,like '{' and '}'. */
		private final String innerText;

		private int color;

		InnerToken(Token prev, String _inner, int _color) {
			super(prev);
			this.innerText = _inner;
			color = _color;
		}

		@Override
		void expand(SpannableStringBuilder target) {

			int replaceFrom = getFormattedStart();
			// Add 2 to account for the separators.
			int replaceTo = replaceFrom + innerText.length() + 2;
			target.replace(replaceFrom, replaceTo, innerText);
			target.setSpan(new ForegroundColorSpan(color), replaceFrom, replaceTo - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		@Override
		int getFormattedLength() {
			// Note that value is only present after expand. Don't error check
			// because this is all
			// private code.
			return innerText.length();
		}
	}
}
