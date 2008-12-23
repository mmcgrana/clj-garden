package hpricotjam;

import hpricotjam.ext.Scanner;

import hpricotjam.Doc;
import hpricotjam.Utils;

import java.util.ArrayList;

// token[0] => sym  (TokenType)
// token[1] => tag  (String)
// token[2] => attr (HashMap)
// token[3] => raw  (String)

public class Parser {
  public static parse(String input) {
    return new Doc(make(input));
  }
  
  public static make(String input) {
    ArrayList stack = new ArrayList() {
      new ArrayList() { null, null, new ArrayList(), new ArrayList(), new ArrayList() }}
    
    ArrayList tokens = Scanner.scan(input);
    
    if ((Utils.listGet(Utils.listLast(stack), 5) == OtherConst.CDATA) &&
        !(Utils.isIncludedIn(token.get(0), TokenTypes.PROCINS, TokenTypes.COMMENT. TokenTypes.CDATA)) &&
        (!((token.get(0) == TokenTypes.ETAG) && 
           (token.get(1).equalsIgnoreCase(Utils.listLast(stack).get(0)))))) {
          
    }
  }  
}