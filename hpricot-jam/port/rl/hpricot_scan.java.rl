package hpricotjam.ext;

import java.io.IOException;
import java.util.HashMap;

public class Scanner {
  private void yield_tokens(TokenType sym, String tag, HashMap attr, String raw) {
    if (sym == TokenType.SYM_TEXT) {
      raw = tag;
    }
    System.out.println(new Token(sym, tag, attr, raw));
  }
  
  public void ELE(TokenType N) {
    if (te > ts || text) {
      String raw_string = null;
      ele_open = false; text = false;
      if (ts != -1 && N != cdata && N != sym_text && N != procins && N != comment) { 
        raw_string = new String(buf, ts, te - ts);
      }
      yield_tokens(N, tag[0], attr, raw_string);
    }
  }
  
  public void SET(String[] N, int E) {
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
  
  public void CAT(String[] N, int E) {
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
  
  public void EBLK(TokenType N, int T) {
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
  String[] tag, akey, aval;
  int mark_tag, mark_akey, mark_aval;
  boolean done = false, ele_open = false;
  int buffer_size = 0;
   
  TokenType xmldecl =  TokenType.XMLDECL,
            doctype =  TokenType.DOCTYPE,
            procins =  TokenType.PROCINS,
            stag =     TokenType.STAG, 
            etag =     TokenType.ETAG,
            emptytag = TokenType.EMPTYTAG,
            comment =  TokenType.COMMENT,
            cdata =    TokenType.CDATA,
            sym_text = TokenType.SYM_TEXT;
  
  public Object scan(String port) throws ParseException {
    attr = null;
    tag  = new String[]{null};
    akey = new String[]{null};
    aval = new String[]{null};
  
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
  
      int back = nread + space;
      if (port.length() < back) {
        str = port.substring(nread);
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
      
      if (cs == hpricot_scan_error) {
        if(tag[0] == null) {
          throw new ParseException("parse error on line " + curline);
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
    return null;
  }
}
