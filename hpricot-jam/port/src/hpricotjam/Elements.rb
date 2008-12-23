module Hpricot
  class Elements < Array
    # Searches this list for any elements (or children of these elements) matching
    # the CSS or XPath expression +expr+.  Root is assumed to be the element scanned.
    #
    # See Hpricot::Container::Trav.search for more.
    def search(*expr,&blk)
      Elements[*map { |x| x.search(*expr,&blk) }.flatten.uniq]
    end

    # Searches this list for the first element (or child of these elements) matching
    # the CSS or XPath expression +expr+.  Root is assumed to be the element scanned.
    #
    # See Hpricot::Container::Trav.at for more.
    def at(expr, &blk)
      search(expr, &blk).first
    end

    ATTR_RE = %r!\[ *(?:(@)([\w\(\)-]+)|([\w\(\)-]+\(\))) *([~\!\|\*$\^=]*) *'?"?([^'"]*)'?"? *\]!i
    BRACK_RE = %r!(\[) *([^\]]*) *\]+!i
    FUNC_RE = %r!(:)?([a-zA-Z0-9\*_-]*)\( *[\"']?([^ \)]*?)['\"]? *\)!
    CUST_RE = %r!(:)([a-zA-Z0-9\*_-]*)()!
    CATCH_RE = %r!([:\.#]*)([a-zA-Z0-9\*_-]+)!

    def self.filter(nodes, expr, truth = true)
      until expr.empty?
        _, *m = *expr.match(/^(?:#{ATTR_RE}|#{BRACK_RE}|#{FUNC_RE}|#{CUST_RE}|#{CATCH_RE})/)
        break unless _

        expr = $'
        m.compact!
        if m[0] == '@'
          m[0] = "@#{m.slice!(2,1)}"
        end

        if m[0] == '[' && m[1] =~ /^\d+$/
          m = [":", "nth", m[1].to_i-1]
        end

        if m[0] == ":" && m[1] == "not"
          nodes, = Elements.filter(nodes, m[2], false)
        elsif "#{m[0]}#{m[1]}" =~ /^(:even|:odd)$/
          new_nodes = []
          nodes.each_with_index {|n,i| new_nodes.push(n) if (i % 2 == (m[1] == "even" ? 0 : 1)) }
          nodes = new_nodes
        elsif "#{m[0]}#{m[1]}" =~ /^(:first|:last)$/
          nodes = [nodes.send(m[1])]
        else
          meth = "filter[#{m[0]}#{m[1]}]" unless m[0].empty?
          if meth and Traverse.method_defined? meth
            args = m[2..-1]
          else
            meth = "filter[#{m[0]}]"
            if Traverse.method_defined? meth
              args = m[1..-1]
            end
          end
          i = -1
          nodes = Elements[*nodes.find_all do |x|
                                i += 1
                                x.send(meth, *([*args] + [i])) ? truth : !truth
                            end]
        end
      end
      [nodes, expr]
    end

    def filter(expr)
      nodes, = Elements.filter(self, expr)
      nodes
    end

    def not(expr)
      if expr.is_a? Traverse
        nodes = self - [expr]
      else
        nodes, = Elements.filter(self, expr, false)
      end
      nodes
    end
  end

  module Traverse
    def self.filter(tok, &blk)
      define_method("filter[#{tok.is_a?(String) ? tok : tok.inspect}]", &blk)
    end

    filter '' do |name,i|
      name == '*' || (self.respond_to?(:name) && self.name.downcase == name.downcase)
    end

    filter '#' do |id,i|
      self.elem? and get_attribute('id').to_s == id
    end

    filter '.' do |name,i|
      self.elem? and classes.include? name
    end

    filter :lt do |num,i|
      self.position < num.to_i
    end

    filter :gt do |num,i|
      self.position > num.to_i
    end

    nth = proc { |num,i| self.position == num.to_i }
    nth_first = proc { |*a| self.position == 0 }
    nth_last = proc { |*a| self == parent.children_of_type(self.name).last }

    filter :nth, &nth
    filter :eq, &nth
    filter ":nth-of-type", &nth

    filter :first, &nth_first
    filter ":first-of-type", &nth_first

    filter :last, &nth_last
    filter ":last-of-type", &nth_last

    filter :even do |num,i|
      self.position % 2 == 0
    end

    filter :odd do |num,i|
      self.position % 2 == 1
    end

    filter ':first-child' do |i|
      self == parent.containers.first
    end

    filter ':nth-child' do |arg,i|
      case arg
      when 'even'; (parent.containers.index(self) + 1) % 2 == 0
      when 'odd';  (parent.containers.index(self) + 1) % 2 == 1
      else         self == (parent.containers[arg.to_i + 1])
      end
    end

    filter ":last-child" do |i|
      self == parent.containers.last
    end

    filter ":nth-last-child" do |arg,i|
      self == parent.containers[-1-arg.to_i]
    end

    filter ":nth-last-of-type" do |arg,i|
      self == parent.children_of_type(self.name)[-1-arg.to_i]
    end

    filter ":only-of-type" do |arg,i|
      parent.children_of_type(self.name).length == 1
    end

    filter ":only-child" do |arg,i|
      parent.containers.length == 1
    end

    filter :parent do
      containers.length > 0
    end

    filter :empty do
      containers.length == 0
    end

    filter :root do
      self.is_a? Hpricot::Doc
    end

    filter 'text' do
      self.text?
    end

    filter 'comment' do
      self.comment?
    end

    filter :contains do |arg, ignore|
      html.include? arg
    end

    pred_procs =
      {'text()' => proc { |ele, *_| ele.inner_text.strip },
       '@'      => proc { |ele, attr, *_| ele.get_attribute(attr).to_s if ele.elem? }}

    oper_procs =
      {'='      => proc { |a,b| a == b },
       '!='     => proc { |a,b| a != b },
       '~='     => proc { |a,b| a.split(/\s+/).include?(b) },
       '|='     => proc { |a,b| a =~ /^#{Regexp::quote b}(-|$)/ },
       '^='     => proc { |a,b| a.index(b) == 0 },
       '$='     => proc { |a,b| a =~ /#{Regexp::quote b}$/ },
       '*='     => proc { |a,b| idx = a.index(b) }}

    pred_procs.each do |pred_n, pred_f|
      oper_procs.each do |oper_n, oper_f|
        filter "#{pred_n}#{oper_n}" do |*a|
          qual = pred_f[self, *a]
          oper_f[qual, a[-2]] if qual
        end
      end
    end

    filter 'text()' do |val,i|
      !self.inner_text.strip.empty?
    end

    filter '@' do |attr,val,i|
      self.elem? and has_attribute? attr
    end

    filter '[' do |val,i|
      self.elem? and search(val).length > 0
    end
  end
end
