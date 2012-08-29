patterned
=========

Provides `defpatterned` to define Clojure functions in a
somewhat Haskell-like way. Examples:

```clojure
(defpatterned factorial
  [so-far 1] so-far
  [so-far n] (factorial (* n so-far) (dec n))
  [n] (factorial 1 n))

(defpatterned count-sequence
  [[]] 0
  [[head & tail]] (inc (count-sequence tail)))

(fact
  (count-sequence []) => 0
  (count-sequence [1 2 3]) => 3)
```

This library is in support of *[Functional Programming for
the Object-Oriented
Programmer](https://leanpub.com/fp-oo)*. Feel free to use it
for other things. Please note:

* This is not based on `clojure.core.match` because I had
  trouble getting that working.

* Speed was not a concern.

* Right now, all you can do is write functions. There's no
  support for matching expressions inside a function.
