package hpricotjam.StackElem;

public class StackElem {
  public stagname;
  public token;
  public eles;
  public excluded_tags;
  public included_tags;
  public uncontainable_tags;
  
  public StackElem(stagname, token, eles, excluded_tags, included_tags, uncontainable_tags) {
    this.stagname = stagname;
    this.token = token;
    this.eles = eles;
    this.excluded_tags = excluded_tags;
    this.included_tags = included_tags;
    this.uncontainable_tags = uncontainable_tags;
  }
}