package lexer;

import java.util.HashMap;
import java.util.Map;

class Lexicon {
  private static final Map<String, TokenType> keywords = new HashMap<>();
  private static final Map<Character, TokenType> symbols = new HashMap<>();

  static {
    keywords.put("class", TokenType.CLASS);
    keywords.put("constructor", TokenType.CONSTRUCTOR);
    keywords.put("function", TokenType.FUNCTION);
    keywords.put("method", TokenType.METHOD);
    keywords.put("field", TokenType.FIELD);
    keywords.put("static", TokenType.STATIC);
    keywords.put("var", TokenType.VAR);
    keywords.put("int", TokenType.INT);
    keywords.put("char", TokenType.CHAR);
    keywords.put("boolean", TokenType.BOOLEAN);
    keywords.put("void", TokenType.VOID);
    keywords.put("true", TokenType.TRUE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("null", TokenType.NULL);
    keywords.put("this", TokenType.THIS);
    keywords.put("let", TokenType.LET);
    keywords.put("do", TokenType.DO);
    keywords.put("if", TokenType.IF);
    keywords.put("else", TokenType.ELSE);
    keywords.put("while", TokenType.WHILE);
    keywords.put("return", TokenType.RETURN);

    symbols.put('+', TokenType.PLUS);
    symbols.put('-', TokenType.MINUS);
    symbols.put('*', TokenType.ASTERISK);
    symbols.put('/', TokenType.SLASH);
    symbols.put('.', TokenType.DOT);
    symbols.put(',', TokenType.COMMA);
    symbols.put(';', TokenType.SEMICOLON);
    symbols.put('&', TokenType.AND);
    symbols.put('|', TokenType.OR);
    symbols.put('~', TokenType.NOT);
    symbols.put('<', TokenType.LT);
    symbols.put('>', TokenType.GT);
    symbols.put('=', TokenType.EQ);
    symbols.put('(', TokenType.LPAREN);
    symbols.put(')', TokenType.RPAREN);
    symbols.put('{', TokenType.LBRACE);
    symbols.put('}', TokenType.RBRACE);
    symbols.put('[', TokenType.LBRACKET);
    symbols.put(']', TokenType.RBRACKET);
  }

  public static TokenType wordType(String word) {
    return keywords.getOrDefault(word, TokenType.IDENT);
  }

  public static TokenType symbolType(char ch) {
    return symbols.get(ch);
  }
}