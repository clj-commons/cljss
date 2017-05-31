# Clojure Style Sheets

<img src="logo.png" width="155" height="68" alt="cljss logo" />

[CSS-in-JS](https://speakerdeck.com/vjeux/react-css-in-js) for ClojureScript

_v1 is inspired by [threepointone/glam](https://github.com/threepointone/glam)_

[![Clojars](https://img.shields.io/clojars/v/org.roman01la/cljss.svg)](https://clojars.org/org.roman01la/cljss)

## Table of Contents
- [Features](#features)
- [How it works](#how-it-works)
- [Installation](#installation)
- [Usage](#usage)
- [Production build](#production-build)
- [Issues](#issues)
- [License](#license)

## Features
- Isolated styles by generating unique names
- Supports CSS pseudo-classes and pseudo-elements
- Injects dynamic styles into `<style>` tag
- Outputs static styles into a single file

## How it works
`defstyles` macro expands into a function which accepts arbitrary number of arguments and returns a string of auto-generated class names that references both static and dynamic styles.

Dynamic styles are updated via CSS Variables (see [browser support](http://caniuse.com/#feat=css-variables)).

## Installation

Add to project.clj: `[org.roman01la/cljss "1.0.0-SNAPSHOT"]`

## Usage

`(defstyles name [args] styles)`

- `name` name of a var
- `[args]` arguments
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

(html
  [:div
   [:button {:class (button "green")} "hit me"]])

;; (button "green") returns =>
;; "css-43696 vars-43696"
```

Output in CSS file (pretty):
```css
.css-43696 {
  font-size: 14px;
  background-color: var(--css-43696-0);
}
```

Dynamically injected:
```css
.vars-43696 {
  --css-43696-0: green;
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
