package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Scans Lox source code and produces a list of tokens.
 * Takes in raw source code as a string and breaks it down
 * into tokens
 */
class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;
	private static final Map<String, TokenType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("and",    TokenType.AND);
		keywords.put("class",  TokenType.CLASS);
		keywords.put("else",   TokenType.ELSE);
		keywords.put("false",  TokenType.FALSE);
		keywords.put("for",    TokenType.FOR);
		keywords.put("fun",    TokenType.FUN);
		keywords.put("if",     TokenType.IF);
		keywords.put("nil",    TokenType.NIL);
		keywords.put("or",     TokenType.OR);
		keywords.put("print",  TokenType.PRINT);
		keywords.put("return", TokenType.RETURN);
		keywords.put("super",  TokenType.SUPER);
		keywords.put("this",   TokenType.THIS);
		keywords.put("true",   TokenType.TRUE);
		keywords.put("var",    TokenType.VAR);
		keywords.put("while",  TokenType.WHILE);
	}
	
	Scanner(String source) {
		this.source = source;
	}
	
	/**
	 * Scans through the source code and produces a list of tokens.
	 * 
	 * @return	a list of tokens produced from the source code
	 */
	List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}
	
		tokens.add(new Token(TokenType.EOF, "", null, line));
		return tokens;
	}
	
	/**
	 * Scans a single token from the source code.
	 */
	private void scanToken() {
	    char c = advance();
	    switch (c) {
	      case '(': addToken(TokenType.LEFT_PAREN); break;
	      case ')': addToken(TokenType.RIGHT_PAREN); break;
	      case '{': addToken(TokenType.LEFT_BRACE); break;
	      case '}': addToken(TokenType.RIGHT_BRACE); break;
	      case ',': addToken(TokenType.COMMA); break;
	      case '.': addToken(TokenType.DOT); break;
	      case '-': addToken(TokenType.MINUS); break;
	      case '+': addToken(TokenType.PLUS); break;
	      case ';': addToken(TokenType.SEMICOLON); break;
	      case '*': addToken(TokenType.STAR); break;
	      
	      case '!':
	          addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
	          break;
	        case '=':
	          addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
	          break;
	        case '<':
	          addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
	          break;
	        case '>':
	          addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
	          break;     
	      case '/':
	        if (match('/') ) {
	        	// a comment goes until the end of the line
	        	while (peek() != '\n' && !isAtEnd()) advance();
	        } else {
	        	addToken(TokenType.SLASH);
	        }
	        break;
	        
	      case ' ':
	      case '\r':
	      case '\t':
	    	  // ignore whitespace
	    	  break;
	    	  
	      case '\n':
	    	  line++;
	    	  break;
	    	  
	      case '"': string(); break;
	        
	      default:
	    	  if (isDigit(c)) {
	    		  number();
	    	  } else if (isAlpha(c)) {
	    		  identifier();
	    	  } else {
	    		  Lox.error(line, "Unexpected character.");
	    	  }
    	  break;
	    }
	}
	
	/**
	 * Scans an identifier or reserved word.
	 */
	private void identifier() {
		while (isAlphaNumeric(peek())) advance();
		
		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null) type = TokenType.IDENTIFIER;
		addToken(type);
	}
	
	/**
	 * Scans a number literal.
	 */
	private void number() {
		while (isDigit(peek())) advance();
		
		// Look for a fractional part 
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();
			
			while (isDigit(peek())) advance();
		}
		
		addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
	}
	
	/**
	 * Scans a string literal.
	 */
	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++; // supports multi-line strings
			advance();
		}
		
		if (isAtEnd()) {
			Lox.error(line,  "Unterminated string.");
			return;
		}
		
		// the closing "
		advance();
		
		// Trim the surrounding quotes
		String value = source.substring(start + 1, current - 1);
		addToken(TokenType.STRING, value);
	}
	
	/**
	 * Consumes the next character if it matches the expected one.
	 * Used to scan double-character lexemes.
	 * 
	 * @param expected	the character to check for
	 * @return			true if current character matches expected
	 */
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}
	
	/**
	 * Returns the next character in the source code without consuming it.
	 * Performs one character lookahead.
	 * 
	 * @return	character at current position, or the null 
	 * 			character if end of source file has been reached
	 */
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}
	
	/**
	 * Returns the character one position past the current one in the source
	 * code without consuming it. Performs two characters lookahead.
	 * 
	 * @return	the character after the current position, or the
	 * 			null character if it would be past the end of the source
	 */
	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}
	
	/**
	 * Checks whether the given character is a letter or underscore.
	 *
	 * @param c	the character to check
	 * @return	true if the character is a letter from a to z
	 * 			(either case) or an underscore, false otherwise
	 */
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
		       (c >= 'A' && c <= 'Z') ||
		       c == '_';
	}
	
	/**
	 * Checks whether the given character is a letter, underscore,
	 * or digit.
	 *
	 * @param c	the character to check
	 * @return	true if the character is alphanumeric or an
	 * 			underscore, false otherwise
	 */
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	/**
	 * Checks whether the character given is a digit.
	 * 
	 * @param c		the character to check
	 * @return		true if character is digit from 0 to 9,
	 * 				false otherwise
	 */
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	/**
	 * Checks whether all characters in the source code 
	 * have been consumed.
	 *
	 * @return	true if the scanner has reached the end of
	 * 		   	the source code, false otherwise
	 */
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	/**
	 * Consumes the next character in the source file and
	 * returns it.
	 * 
	 * @return	the character at the current position before advancing
	 */
	private char advance() {
		return source.charAt(current++);
	}

	/**
	 * Adds a token with no literal value to the token list.
	 * 
	 * @param type	the type of token to add
	 */
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	/**
	 * Adds a token to the token list.
	 * 
	 * @param type		the type of token to add
	 * @param literal	the literal value of the token
	 */
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
