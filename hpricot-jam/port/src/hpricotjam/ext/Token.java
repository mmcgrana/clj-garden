package hpricotjam.ext;

import java.util.HashMap;

public class Token {
  public TokenType sym;
  public String  tag;
  public HashMap attr;
  public String  raw;
  
  public Token(TokenType sym, String tag, HashMap attr, String raw) {
    this.sym =  sym;
    this.tag =  tag;
    this.attr = attr;
    this.raw =  raw;
  }
  
  public String toString() {
    return sym + " " + tag + " " + attr + " " + raw;
  }
}
