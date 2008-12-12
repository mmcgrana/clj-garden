// Doesn't seem terrible, thoughsome knarly string logic and interface issues.

package hpricotjam.core;

public class Doc {
  public SC output(SC out) {
    if (children != null) {
      (for child: this.children) {
        child.output(out);
      }
    }
    return out;
  }
  
  // make is dest. so not needed?
}

public class BaseEle {
  // PORT: name, raw_attributes, children, etag?
  
  public String htmlQuote(String str) {
    return "\"" + gsub(str, "\"", "\\"" ) + "\"";
  }
  
  // if_output no :preserve option so redundant
  
  public String pathname {
    return this.name;
  }
}

public class Elem {
  public Elem(String name, HashMap attrs, SC[] children, SC etag) {
    this.name = name;
    this.raw_attributes = attrs;
    this.children = children;
    this.etag = etag;
  }
  
  public boolean isEmpty {
    return (this.children == null) or (this.children.length == 0);
  }
  
  public HashMap attributes() {
    if (this.raw_attributes != null) {
      // PORT: mash with .uxs on keys
    }
  }
  
  // to_plain_text
  
  public String pathname {
    return this.name;
  }
  
  // output
  
  // attributes_as_html
}

public class ETag {
  public Etag(String name) {
    this.name = name;
  }
  
  // output
}

// more...