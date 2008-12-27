* * any element
* E an element of type E
* E:nth-child(n) an E element, the n-th child of its parent
* E:nth-last-child(n) an E element, the n-th child of its parent, * counting from the last one
* E:nth-of-type(n) an E element, the n-th sibling of its type
* E:nth-last-of-type(n) an E element, the n-th sibling of its type, counting from the last one
* E:first-child an E element, first child of its parent
* E:last-child an E element, last child of its parent
* E:first-of-type an E element, first sibling of its type
* E:last-of-type an E element, last sibling of its type
* E:only-child an E element, only child of its parent
* E:only-of-type an E element, only sibling of its type
* E:empty an E element that has no children (including text nodes)
* E:enabled
* E:disabled a user interface element E which is enabled or disabled
* E:checked a user interface element E which is checked (for instance a radio-button or checkbox)
* E.warning an E element whose class is "warning"
* E#myid an E element with ID equal to "myid".
* E:not(s) an E element that does not match simple selector s
* E F an F element descendant of an E element
* E > F an F element child of an E element
* E + F an F element immediately preceded by an E element
* E ~ F an F element preceded by an E element 

* E[@foo] an E element with a "foo" attribute
* E[@foo="bar"] an E element whose "foo" attribute value is exactly equal to "bar"
* E[@foo~="bar"] an E element whose "foo" attribute value is a list of space-separated values, one of which is exactly equal to "bar"
* E[@foo^="bar"] an E element whose "foo" attribute value begins exactly with the string "bar"
* E[@foo$="bar"] an E element whose "foo" attribute value ends exactly with the string "bar"
* E[@foo*="bar"] an E element whose "foo" attribute value contains the substring "bar"
* E[@hreflang|="en"] an E element whose "hreflang" attribute has a hyphen-separated list of values beginning (from the left) with "en"