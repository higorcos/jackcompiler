package codegen;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

  public enum Kind {
    STATIC, FIELD, ARG, VAR
  }

  public record Symbol(String name, String type, Kind kind, int index) {
  }

  private final Map<String, Symbol> classScope = new HashMap<>();
  private final Map<String, Symbol> subroutineScope = new HashMap<>();
  private final Map<Kind, Integer> counts = new HashMap<>();

  public SymbolTable() {
    for (Kind kind : Kind.values()) {
      counts.put(kind, 0);
    }
  }

  public void startSubroutine() {
    subroutineScope.clear();
    counts.put(Kind.ARG, 0);
    counts.put(Kind.VAR, 0);
  }

  private Map<String, Symbol> scopeFor(Kind kind) {
    return switch (kind) {
      case STATIC, FIELD -> classScope;
      case ARG, VAR -> subroutineScope;
    };
  }

  public void define(String name, String type, Kind kind) {
    int index = varCount(kind);
    scopeFor(kind).put(name, new Symbol(name, type, kind, index));
    counts.put(kind, index + 1);
  }

  public int varCount(Kind kind) {
    return counts.get(kind);
  }

  public Symbol resolve(String name) {
    Symbol symbol = subroutineScope.get(name);
    return symbol != null ? symbol : classScope.get(name);
  }
}