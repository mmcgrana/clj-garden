// NamedCharacters - none
// ElementContent :EMPTY and :CDATA
// ElementInclusions and ElementExclusions - none

package hpricotjam;

import java.util.HashSet;

public class HtmlInfo {
  public static HashSet EMPTY_TAG_NAMES = new HashSet() {
    "area", "hr", "base", "link", "br", "meta", "isindex", "col",
    "basefont", "img", "param", "input"};
  
  public static boolean isCDataTagName(String tag_name) {
    return (tag_name.equals("script") || tag_name.equals("style"));
  }
  
  public static boolean isEmptyTagName(String tag_name) {
    return EMPTY_TAG_NAMES.containsKey(string_name);
  }
}