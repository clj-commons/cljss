# Clojure Style Sheets

<img src="logo.png" width="155" height="68" alt="cljss logo" />

[CSS-in-JS](https://speakerdeck.com/vjeux/react-css-in-js) for ClojureScript

[![Clojars](https://img.shields.io/clojars/v/org.roman01la/cljss.svg)](https://clojars.org/org.roman01la/cljss)

## Table of Contents
- [Why write CSS in ClojureScript?](#why-write-css-in-clojurescript)
- [Features](#features)
- [How it works](#how-it-works)
- [Installation](#installation)
- [Usage](#usage)
- [Production build](#production-build)
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
- Injects styles into `<style>` tag at run-time
- Debuggable styles in development (set via `goog.DEBUG`)

## How it works

### `defstyled`

`defstyled` macro accepts var name, HTML element tag name as a keyword and a hash of styles.

The macro expands into a function which accepts optional hash of attributes and child components, and returns Hiccup.

A hash of attributes with dynamic CSS values as well as normal HTML attributes can be passed into underlying React component. Reading from attributes hash map can be done via anything that satisfies `cljs.core/ifn?` predicate (`Fn` and `IFn` protocols, and normal functions). It is recommended to use keywords.

```clojure
(defstyled h1 :h1
  {:font-family "sans-serif"
   :font-size :size
   :color #(-> % :color {:light "#fff" :dark "#000"})})

(h1 {:size "32px" :color :dark} "Hello, world!")
;; [:h1 {:className "css-43697 vars-43697"} "Hello, world!"]
```

## Installation

Add to project.clj: `[org.roman01la/cljss "1.2.0"]`

## Usage

`(defstyled name tag-name styles)`

- `name` name of a var
- `tag-name` HTML tag name as a keyword
- `styles` a hash map of styles definition

Using [Sablono](https://github.com/r0man/sablono) templating for [React](https://facebook.github.io/react/)
```clojure
(ns example.core
  (:require [sablono.core :refer [html]]
            [cljss.core :refer [defstyled]]))

(defstyled Button :button
  {:padding "16px"
   :margin-top :v-margin
   :margin-bottom :v-margin})

(html
  (Button {:v-marging "8px"
           :on-click #(console.log "Click!")}))
```

Dynamically injected CSS:
```css
.css-43697 {
  padding: 16px;
  margin-top: var(--css-43697-0);
  margin-bottom: var(--css-43697-1);
}
.vars-43697 {
  --css-43697-0: 8px;
  --css-43697-1: 8px;
}
```

## Production build

Set `goog.DEBUG` to `false` to enable fast path styles injection.

```clojure
{:compiler
 {:closure-defines {"goog.DEBUG" false}}}
```

## License

Copyright © 2017 Roman Liutikov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
