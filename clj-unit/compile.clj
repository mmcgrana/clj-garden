(binding [*compile-path*      "classes"
          *warn-on-reflection* true]
  (compile 'clj-unit.core))