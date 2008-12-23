// Easy, just class hierarchy.

package hpricotjam.core;

public class Name extends Hpricot {}

public class Context extends Hpricot {}


public abstract class Tag extends Hpricot { }

public class ETag extends Tag { }


public class Node extends Hpricot { }

public class Container extends Node { }

public class Doc extends Container { }

public class Elem extends Container { }

public abstract class Leaf extends Node { }

public class Text extends Leaf { }

public class XMLDecl extends Leaf { }

public class DocType extends Leaf { }

public class ProcIins extends Leaf { }

public class Comment extends Leaf { }

public class BogsETag extends Leaf { }


public class Traverse { }

public class ContainerTrav extends Traverse { }

public class LeafTrav extends Traverse { }

Doc, Elem => include ContainerTrav

CData, Text, XMLDecl, DocType, ProcIns, Comment, BogusETag => include LeafTrav 







