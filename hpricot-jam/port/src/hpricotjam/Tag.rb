module Hpricot
  # :stopdoc:

  class Doc
  end

  class BaseEle
    def pathname; self.name end
  end

  class Elem
    def initialize tag, attrs = nil, children = nil, etag = nil
      self.name, self.raw_attributes, self.children, self.etag =
        tag, attrs, children, etag
    end
    def empty?; children.nil? or children.empty? end
    def attributes
      if raw_attributes
        raw_attributes.inject({}) do |hsh, (k, v)|
          hsh[k] = Hpricot.uxs(v)
          hsh
        end
      end
    end
    def pathname; self.name end
  end

  class ETag
    def initialize name; self.name = name end
  end

  class BogusETag
  end

  class Text
    def initialize content; self.content = content end
    def pathname; "text()" end
    def to_s
      Hpricot.uxs(content)
    end
    alias_method :inner_text, :to_s
    alias_method :to_plain_text, :to_s
    def << str; self.content << str end
  end

  class CData
    def initialize content; self.content = content end
    alias_method :to_s, :content
    alias_method :to_plain_text, :content
  end

  class XMLDecl
    def pathname; "xmldecl()" end
  end

  class DocType
    def initialize target, pub, sys
      self.target, self.public_id, self.system_id = target, pub, sys
    end
    def pathname; "doctype()" end
  end

  class ProcIns
    def pathname; "procins()" end
  end

  class Comment
    def pathname; "comment()" end
  end
end
