# Clojure Style Sheets

<img src="logo.png" width="155" height="68" alt="cljss logo" />

[CSS-in-JS](https://speakerdeck.com/vjeux/react-css-in-js) for ClojureScript

_v1 is inspired by [threepointone/glam](https://github.com/threepointone/glam) and [tkh44/emotion](https://github.com/tkh44/emotion)_

[![Clojars](https://img.shields.io/clojars/v/org.roman01la/cljss.svg)](https://clojars.org/org.roman01la/cljss)

## Table of Contents
- [Why write CSS in ClojureScript?](#why-write-css-in-clojurescript)
- [Features](#features)
- [How it works](#how-it-works)
- [Installation](#installation)
- [Usage](#usage)
- [Production build](#production-build)
- [Issues](#issues)
- [License](#license)

## Why write CSS in ClojureScript?

Writing styles this way has the same benefits as writing components that keep together view logic and presentation. It all comes to developer efficiency and maintainability.

Thease are some resources that can give you more context:

- [“A Unified Styling Language”](https://medium.com/seek-blog/a-unified-styling-language-d0c208de2660) by Mark Dalgleish
- [“A Unified Styling Language”](https://www.youtube.com/watch?v=X_uTCnaRe94) (talk) by Mark Dalgleish
- [“The road to styled components: CSS in component-based systems”](https://www.youtube.com/watch?v=MT4D_DioYC8) by Glen Maddern

## Features
- Automatic scoped styles by generating unique names
- Supports CSS pseudo-classes and pseudo-elements
- Injects dynamic styles into `<style>` tag at run-time
- Outputs static styles into a single file at compile-time

## How it works

### `defstyles`

`defstyles` macro expands into a function which accepts arbitrary number of arguments and returns a string of auto-generated class names that references both static and dynamic styles.

```clojure
(defstyles button [bg]
  {:font-size "14px"
   :background-color bg})

(button "#000")
;; "css-43696 vars-43696"
```

Dynamic styles are updated via CSS Variables (see [browser support](http://caniuse.com/#feat=css-variables)).

### `defstyled`

`defstyled` macro accepts var name, HTML element tag name as a keyword and a hash of styles.

The macro expands into a function which accepts optional hash of attributes and child components, and returns plain React component definition. Static part of the styles are written into a file at compile-time.

A hash of attributes can be used to pass dynamic CSS values as well as normal attributes onto HTML tag React component. Reading from attributes hash map can be done via anything that satisfies `cljs.core/ifn?` predicate (`Fn` and `IFn` protocols, and normal functions). It is recommended to use keywords.

```clojure
(defstyled h1 :h1
  {:font-family "sans-serif"
   :font-size :size
   :color #(-> % :color {:light "#fff" :dark "#000"})})

(h1 {:size "32px" :color :dark} "Hello, world!")
;; (js/React.createElement "h1" #js {:className "css-43697 vars-43697"} "Hello, world!")
```

NOTE: _Child components are rendered using Sablono, which means they are expected to be Hiccup-style components._

## Installation

Add to project.clj: `[org.roman01la/cljss "1.1.0"]`

## Usage

`(defstyles name [args] styles)`

- `name` name of a var
- `[args]` arguments
- `styles` a hash map of styles definition

`(defstyled name tag-name styles)`

- `name` name of a var
- `tag-name` HTML tag name as a keyword
- `styles` a hash map of styles definition

Using [Sablono](https://github.com/r0man/sablono) templating for [React](https://facebook.github.io/react/)
```clojure
(ns example.core
  (:require [sablono.core :refer-macros [html]]
            [cljss.core :refer-macros [defstyles]]))

(defstyles button [bg]
  {:font-size "14px"
   :background-color bg})

;; expands into =>
;; (defn button [bg]
;;   (cljss.core/css "43696" [[:background-color bg]]))

(defstyled wrapper :div
  {:padding "16px"
   :background :bg})

 ;; expands into =>
 ;; (def wrapper
 ;;   (cljss.core/styled "div" "43697" [[:background :bg]] [:bg]))

(html
  (wrapper {:bg "#fafafa"}
   [:button {:class (button "green")} "hit me"]))
```

Output in CSS file (pretty):
```css
.css-43696 {
  font-size: 14px;
  background-color: var(--css-43696-0);
}
.css-43697 {
  padding: 16px;
  background: var(--css-43697-0);
}
```

Dynamically injected:
```css
.vars-43696 {
  --css-43696-0: green;
}
.vars-43697 {
  --css-43697-0: #fafafa;
}
```

## Production build

Compiler options

```clojure
{:compiler
 {:css-output-to "resources/public/css/styles.css"}}
```

## Issues
- If you are using [Figwheel](https://github.com/bhauman/lein-figwheel) with build config validation enabled, you'll see an error `The key :css-output-to is unrecognized` in REPL when starting a project.
Set `:validate-config :ignore-unknown-keys` in Figwheel config to only validate options it recognizes.

## License

Copyright © 2017 Roman Liutikov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
