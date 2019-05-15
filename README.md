# Poulet
Implementing a dependently typed language. Here is an example of some basic encodings in our language.
```
// define natural number types
nat : Type1 := {X : Type1} {f : {_ : X} X} {_ : X} X
n0 : nat := \X : Type1 -> \f : {_ : X} X -> \a : X -> a

// define operations on natural numbers
succ : {_ : nat} nat := \n : nat -> \X : Type1 -> \f : {_ : X} X -> \a : X -> (f) (((n) X) f) a
pred : {_ : nat} nat := \n : nat -> \T : Type1 -> \f : {_ : T} T -> \x : T -> ((((n) {_ : {_ : T} T} T) \g : {_ : {_ : T} T} T -> \h : {_ : T} T -> (h) (g) f) \u : {_ : T} T -> x) \u : T -> u
plus : {_ : nat} {_ : nat} nat := \n : nat -> \m : nat -> \X : Type1 -> \f : {_ : X} X -> \a : X -> (((m) X) f) (((n) X) f) a
minus : {_ : nat} {_ : nat} nat := \n : nat -> \m : nat -> (((m) nat) pred) n
mult : {_ : nat} {_ : nat} nat := \n : nat -> \m : nat -> \X : Type1 -> \f : {_ : X} X -> \a : X -> (((m) X) ((n) X) f) a
exp : {_ : nat} {_ : nat} nat := \n : nat -> \m : nat -> (((m) nat) (mult) n) (succ) n0

// define bools
bool : Type1 := {X : Type1} {_ : X} {_ : X} X
true : bool := \X : Type1 -> \x : X -> \y : X -> x
false : bool := \X : Type1 -> \x : X -> \y : X -> y
ifthenelse : {X : Type1} {_ : bool} {_ : X} {_ : X} X := \X : Type1 -> \p : bool -> \a : X -> \b : X -> (((p) X) a) b
and : {_ : bool} {_ : bool} bool := \a : bool -> \b : bool -> ((((ifthenelse) bool) a) b) false
or : {_ : bool} {_ : bool} bool := \a : bool -> \b : bool -> ((((ifthenelse) bool) a) true) b
not : {_ : bool} bool := \a : bool -> ((((ifthenelse) bool) a) false) true
xor : {_ : bool} {_ : bool} bool := \a : bool -> \b : bool -> ((and) ((or) a) b) (not) ((and) a) b

// predicates on natural numbers
iszero : {_ : nat} bool := \n : nat -> (((n) bool) \x : bool -> false) true
geq : {_ : nat} {_ : nat} bool := \n : nat -> \m : nat -> (iszero) ((minus) m) n
leq : {_ : nat} {_ : nat} bool := \n : nat -> \m : nat -> (iszero) ((minus) n) m
eq : {_ : nat} {_ : nat} bool := \n : nat -> \m : nat -> ((and) ((geq) n) m) ((geq) m) n
gtr : {_ : nat} {_ : nat} bool := \n : nat -> \m : nat -> ((geq) (pred) n) m
less : {_ : nat} {_ : nat} bool := \n : nat -> \m : nat -> ((leq) (succ) n) m

// integer division
//      dividing by 0 gives dividend
divide : {_ : nat} {_ : nat} nat := \n : nat -> \m : nat -> (((n) nat) \z : nat -> ((((ifthenelse) nat) ((leq) ((mult) m) (succ) z) n) (succ) z) z) n0

// define pairs
pair : Type1 := {X : Type1} {_ : {_ : X} {_ : X} X} X
construct_pair : {X : Type1} {_ : X} {_ : X} (pair) X := \X : Type1 -> \a : X -> \b : X -> \f : {_ : X} {_ : X} X -> ((f) a) b
first : {X : Type1} {_ : X} {_ : X} X := \X : Type1 -> (true) X
second : {X : Type1} {_ : X} {_ : X} X := \X : Type1 -> (false) X
```
