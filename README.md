patterned
=========

Provides `defpatterned` to define Clojure functions in a
somewhat Haskell-like way. 

Here is a simple example showing an implementation of that
has a single-argument main interface and a two-step
recursive function that uses a collecting parameter.

```clojure
(defpatterned factorial
  [n] (factorial 1 n)
  [so-far 0] so-far
  [so-far n] (factorial (* n so-far) (dec n)))
```

Matches are made recursively when the pattern contains subvectors:

```clojure
(defpatterned deep
  [:a [1]]          "a simple match"
  [s [1 [1 [n]]]]   (str "a match of " s " and " n))

user=> (deep :a [1])
"a simple match"
user=> (deep :b [1 [1 [3]]])
"a match of :b and 3"
```

Rest arguments are supported. Here's a typical recursive implementation of a sequence function:

```clojure
(defpatterned count-sequence
  [[]] 0
  [[head & tail]] (inc (count-sequence tail)))

user=> (count-sequence [10 9 8 7 6 5 4 3 2 1])
10
```

A list containing `:when` may be used to match any argument that passes
a predicate. An optional `:bind` argument names a symbol that will be
bound on the right-hand side:

```clojure
(defpatterned classify-number
  [(:when odd? :bind n)] (str "odd number " n)
  [(:when even?)] (str "some even number"))

user=> (classify-number 5)
"odd number 5"
user=> (classify-number 6)
"some even number"
```

A list containing `:in` will match any one of a number of literals. `:bind` can be used to find out which value matched. Here is a factorial that uses both `:in` and `:when`:

```clojure
(defpatterned factorial
  [(:when (partial > 0) :bind n)] (oops! "Bad number" :n n)
  [(:in [0 1])] 1
  [n] (* n (factorial (dec n))))
```

---------------------

The `patterned` macro is the pattern-matching equivalent of
`fn`:

```clojure
( (patterned
    [1] "one"
    [n] "other")
  8888)
```

The `letpatterned` macro is the pattern-matching equivalent of `letfn`:

```clojure
(letpatterned
   [(f1 [0] 0 [n] (f2 (dec n)))
    (f2 [0] 1 [n] (f1 (dec n)))]
 (f2 88))
```

---------------------

This library is in support of *[Functional Programming for
the Object-Oriented
Programmer](https://leanpub.com/fp-oo)*. Feel free to use it
for other things. Please note:

* This is not based on `clojure.core.match` because I had
  trouble getting that working.

* Speed was not a concern.

* Right now, all you can do is write functions. There's no
  support for matching expressions inside a function.
