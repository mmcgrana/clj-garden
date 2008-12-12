import java.io.IOException;
import java.util.HashMap;
import hpricotjam.ParseException;

public class Scan {
  public void ELE(Object N) {
    if (te > ts || text) {
      String raw_string = null;
      ele_open = false; text = false;
      if (ts != -1 && N != cdata && N != sym_text && N != procins && N != comment) { 
        raw_string = new String(buf, ts, te - ts);
      }
      // PORT: do we need something to replace rb_yield_tokens here? 
    }
  }
  
  public void SET(Object[] N, int E) {
    int mark = 0;
    if(N == tag) { 
      if(mark_tag == -1 || E == mark_tag) {
        tag[0] = "";
      } else if(E > mark_tag) {
        tag[0] = new String(buf,mark_tag, E - mark_tag);
      }
    } else if(N == akey) {
      if(mark_akey == -1 || E == mark_akey) {
        akey[0] = "";
      } else if(E > mark_akey) {
        akey[0] = new String(buf, mark_akey, E - mark_akey);
      }
    } else if(N == aval) {
      if(mark_aval == -1 || E == mark_aval) {
        aval[0] = "";
      } else if(E > mark_aval) {
        aval[0] = new String(buf, mark_aval, E - mark_aval);
      }
    }
  }
  
  public void CAT(Object[] N, int E) {
    if(null == N[0]) {
      SET(N, E);
    } else {
      int mark = 0;
      if(N == tag) {
        mark = mark_tag;
      } else if(N == akey) {
        mark = mark_akey;
      } else if(N == aval) {
        mark = mark_aval;
      }
      // PORT: less than ideal...
      N[0] = new StringBuilder(N[0]).append(new String(buf, mark, E - mark)).toString();
    }
  }

  public void SLIDE(Object N) {
      int mark = 0;
      if(N == tag) {
        mark = mark_tag;
      } else if(N == akey) {
        mark = mark_akey;
      } else if(N == aval) {
        mark = mark_aval;
      }
      if(mark > ts) {
        if(N == tag) {
          mark_tag  -= ts;
        } else if(N == akey) {
          mark_akey -= ts;
        } else if(N == aval) {
          mark_aval -= ts;
        }
      }
  }

  public void ATTR(String K, String V) {
    if(!(K == null)) {
      if(attr == null) {
        attr = new HashMap();
      }
      attr.put(K,V);
    }
  }

  public void ATTR(String[] K, String V) {
    ATTR(K[0], V);
  }
  
  public void ATTR(String K, String[] V) {
    ATTR(K, V[0]);
  }
  
  public void ATTR(String[] K, String[] V) {
    ATTR(K[0], V[0]);
  }

  public void TEXT_PASS() {
    if(!text) { 
      if(ele_open) { 
        ele_open = false; 
        if(ts > -1) { 
          mark_tag = ts; 
        } 
      } else {
        mark_tag = p; 
      } 
      attr = null; 
      tag[0] = null; 
      text = true; 
    }
  }
  
  public void EBLK(Object N, int T) {
    CAT(tag, p - T + 1);
    ELE(N);
  }

%%{

  machine hpricot_scan;

  action newEle {
    if (text) {
      CAT(tag, p);
      ELE(sym_text);
      text = false;
    }
    attr = null;
    tag[0] = null;
    mark_tag = -1;
    ele_open = true;
  }

  action _tag  { mark_tag = p;    }
  action _aval { mark_aval = p;   }
  action _akey { mark_akey = p;   }
  action tag   { SET(tag, p);     }
  action tagc  { SET(tag, p - 1); }
  action aval  { SET(aval, p);    }
  action aunq { 
    if (buf[p-1] == '"' || buf[p-1] == '\'') { SET(aval, p-1); }
    else { SET(aval, p); }
  }
  action akey   { SET(akey, p); }
  action xmlver { SET(aval, p); ATTR("version",    aval); }
  action xmlenc { SET(aval, p); ATTR("encoding",   aval); }
  action xmlsd  { SET(aval, p); ATTR("standalone", aval); }
  action pubid  { SET(aval, p); ATTR("public_id",  aval); }
  action sysid  { SET(aval, p); ATTR("system_id",  aval); }

  action new_attr { 
    akey[0] = null;
    aval[0] = null;
    mark_akey = -1;
    mark_aval = -1;
  }

  action save_attr { 
    ATTR(akey, aval);
  }

  include hpricot_common "hpricot_common.rl";

}%%

  %% write data nofinal;
  
  public final static int BUFSIZE = 16384;
  
  int cs, act, have = 0, nread = 0, curline = 1, p = -1;
  boolean text = false;
  int ts = -1, te;
  int eof = -1;
  char[] buf;
  HashMap attr;
  Object[] tag, akey, aval;
  int mark_tag, mark_akey, mark_aval;
  boolean done = false, ele_open = false;
  int buffer_size = 0;
   
  String xmldecl =  "xmldecl", 
         doctype =  "doctype", 
         procins =  "procins", 
         stag =     "stag", 
         etag =     "etag", 
         emptytag = "emptytag", 
         comment =  "comment"
         cdata =    "cdata",
         sym_text = "text";
  
  public Object scan(String port) {
    attr = null;
    tag  = new Object[]{null};
    akey = new Object[]{null};
    aval = new Object[]{null};
  
    buf = new char[BUFSIZE];
  
    %% write init;
  
    while(!done) {
      String str;
      p = have;
      int pe;
      int len, space = buffer_size - have;
  
      if (space == 0) {
        /* We've used up the entire buffer storing an already-parsed token
         * prefix that must be preserved.  Likely caused by super-long attributes.
         * See ticket #13. */
         buffer_size += BUFSIZE;
         char[] new_buf = new char[buffer_size];
         System.arraycopy(buf, 0, new_buf, 0, buf.length);
         buf = new_buf;
         space = buffer_size - have;
      }
  
      // PORT: probably can do less over the next 10 lines.
      int back = nread + space;
      if (port.length() < back) {
        str = post.substring(nread);
      } else {
        str = port.substring(nread, nread + space);
      }
  
      char[] chars = str.toCharArray();
      System.arraycopy(chars, 0, buf, p, chars.length);
  
      len = str.length();
      nread += len;
  
      if (len < space) {
        len++;
        done = true;
      }
  
      pe = p + len;
      char[] data = buf;
  
      %% write exec;
      
      // PORT: where does this symbol come from?
      if (cs == hpricot_scan_error) {
        if(tag[0] == null) {
          throws new ParseException("parse error on line " + curline);
        } else {
          throw new ParseException("parse error on element <" + tag.toString() + ">, starting on line " + curline); 
        }
      }
      
      if (done && ele_open) {
        ele_open = false;
        if(ts > -1) {
          mark_tag = ts;
          ts = -1;
          text = true;
        }
      }
  
      if(ts == -1) {
        have = 0;
        /* text nodes have no ts because each byte is parsed alone */
        if(mark_tag != -1 && text) {
          if (done) {
            if(mark_tag < p - 1) {
              CAT(tag, p - 1);
              ELE(sym_text);
            }
          } else {
            CAT(tag, p);
          }
        }
        mark_tag = 0;
      } else {
        have = pe - ts;
        System.arraycopy(buf, ts, buf, 0, have);
        SLIDE(tag);
        SLIDE(akey);
        SLIDE(aval);
        te = (te - ts);
        ts = 0;
      }
    }
    // PORT: return value??.
    return null;
  }
}
