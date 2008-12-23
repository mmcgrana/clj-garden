package hpricotjam;

import hpricotjam.ext.Scanner;
import hpricotjam.Utils;
import java.util.ArrayList;

// TODO: Doc class?

public class Parser {
  public static parse(String input) {
    return new Doc(make(input));
  }
  
  public static make(String input) {
    ArrayList stack = new ArrayList() {
      new ArrayList() { null, null, new ArrayList(), new ArrayList(), new ArrayList() }}
    
    ArrayList tokens = Scanner.scan(input);
    
    if ((Utils.listGet(Utils.listLast(stack), 5) == OtherConst.CDATA) &&
        !(Utils.isIncludedIn(token.get(0), TagTypes.PROCINS, TagTypes.COMMENT. TagTypes.CDATA)) &&
        (!((token.get(0) == TagTypes.ETAG) && 
           (token.get(1).equalsIgnoreCase(Utils.listLast(stack).get(0)))))) {
          
    }
  }  
}