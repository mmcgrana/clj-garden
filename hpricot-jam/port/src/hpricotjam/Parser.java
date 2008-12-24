// Done first pass, seems very doable, just have to get typing right,
// espeically structure[] stuff towards the end.

package hpricotjam;

import hpricotjam.ext.Scanner;
import hpricotjam.ext.Token;

import hpricotjam.Doc;
import hpricotjam.Utils;
import hpricotjam.HtmlInfo;

import java.util.ArrayList;

// token[0] => sym  (TokenType)
// token[1] => tag  (String)
// token[2] => attr (HashMap)
// token[3] => raw  (String)


public stagname;
public token;
public eles;
public excluded_tags;
public included_tags;
public uncontainable_tags;

// stack_elem[0] => stagname           ()
// stack_elem[1] => token              ()
// stack_elem[2] => eles               (Stack<Token>)
// stack_elem[3] => excluded_tags      ()
// stack_elem[4] => included_tags      ()
// stack_elem[5] => uncontainable_tags ()

// normalize attrs

public class Parser {
  public static parse(String input) {
    return new Doc(make(input));
  }
  
  public static make(String input) {
    Stack stack = new Stack();
    stack.push(new StackElem(nil, nil, [], [], []));
        
    ArrayList tokens = Scanner.scan(input);
    
    for (Token token : tokens) {
      // TODO: may want _tags.first() or something here.
      if (HtmlInfo.isCDataTagName(stack.peek().uncontainable_tags)) &&
          !(Utils.isIncludedIn(token.sym, TokenType.PROCINS, TokenType.COMMENT, TokenType.CDATA)) &&
          !((token.sym == TokenType.ETAG) && (token.tag.equalsIgnoreCase(stack.peek().stagname)))) {
        token.sym = TokenType.SYM_TEXT;
        if (token.raw != null) {
          token.tag = token.raw;
        }
      }
      
      if (token.sym == TokenType.EMPTYTAG) {
        token.tag = token.tag.toLowerCase();
        if (HtmlInfo.isEmptyTagName.(token.tag)) {
          token.sym = TokenType.STAG;
        }
      }
      
      if (token.attr instanceof HashMap) {
        token.normalizeAttrs()
      }
      
      if (token.sym == TokenType.STAG) {
        // map u(str) ?
        
        String stagname = token.tag.toLowerCase();
        // token.sym = stagname; ??
        token.tag = stagname;
        if (HtmlInfo.isEmptyTagName(stagname)) {
          token.sym = TokenType.EMPTYTAG;
          stack.peek.eles.push(token);
        } else {
          if (HtmlInfo.isCDataTagName(stagname) {
            // TODO: need better marker for this
            uncontainable_tags = ":CDATA";
          }
          // TODO: excluded tags etc.
          stack.push(new StackElem(stagname, token, [], excluded_tags, included_tags, uncontainable_tags));
        }
      } else if (token.sym == TokenType.ETAG) {
        String etagname = token.tag.toLowerCase();
        // token.sym = etagname?
        token.tag = etagname;
        matched_elem = null;
        // TODO: downto loop
        if (matched_elem != null) {
          ele = stack.pop();
          stack.peek().eles.push(ele);
        } else {
          stack.peek().eles.push([:bogus_etag, token.first, token.last])
        }
      } else if (token.sym == TokenType.TEXT_SYM) {
        l = stack.peek().eles.peek();
        if ((l != null) && (l[0] == TokenType.TEXT_SYM)) {
          l[1] += token[1];
        } else {
          stack.peek.eles.push(token);
        }
      } else {
        stack.peek().eles.push(token);
      }
    }
    
    while (stack.empty()) {
      StackElem ele = stack.pop;
      stack.peek().eles.push(ele); 
    }
    
    
    ArrayList built = new ArrayList(stack.peek().eles.size());
    for (SomeClass structure : stack.peek().eles) {
      built.add(buildNode(structure));
    }
  }
  
  public static buildNode(structure) {
    SomeClass label = structure[0];
    if (label instanceof String) {
      // TODO
    } else if (label == TokenType.TEXT_SYM) {
      String raw_string = structure[1];
      return new Text(raw_string);
    } else if (label == TokenType.EMPTYTAG) {
      String qname = structure[1];
      HashMap attrs = structure[2];
      String raw_string = structure[3];
      return new Elem(new STag(qname, attrs, raw_string));
    } else if (label == TokenType.BOGUSETAG) {
      String qname = structure[1];
      String raw_string = structure[2];
      return new BogusETag(qname, raw_string);
    } else if (label == TokenType.XMLDECL) {
      HashMap attrs = structure[2];
      String raw_string = structure[3];
      if (attrs == null) {
        attrs = new HashMap();
      }
      String version = attrs.get("version");
      String encoding = attrs.get("encoding");
      String standalone_val = attrs.get("standalone");
      boolean standalone;
      if (stanalone_val.equals("yes")) {
        standalone = true;
      } else if (stanalone_val.equals("no")) {
        standalone = false;
      } else {
        standalone = null;
      }
      return new XMLDecl(version, encoding, standalone, raw_string);
    } else if (label == TokenType.DOCTYPE) {
      String root_elem_name = structure[1].toLowerCase;
      HashMap attrs = structure[2];
      String raw_string = structure[3];
      return new DocType(root_elem_name, null, null, raw_string);
    } else if (label == TokenType.PROCINS) {
      String raw_string = structure[1];
      Matcher matcher = PROCINS_RE.matcher(raw_string);
      matcher.find();
      String target = matcher.group(1);
      String content = matcher.group(2);
      return new ProcIns(target, content);
    } else if (label == TokenType.COMMENT) {
      String content = structure[1];
      return new Comment(content);
    } else if (label == TokenType.CDATA_CONTENT) {
      String raw_string = structure[1];
      return new CData(raw_string);
    } else if (label == TokenType.CDATA) {
      String content = structure[1];
      return new CData(content)
    } else {
      throw new ParseException("[bug] unknown structure: " + structure.toString());
    }
  } 
}