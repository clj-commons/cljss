# Clojure Style Sheets

<img src="logo.png" width="155" height="68" alt="cljss logo" />

[CSS-in-JS](https://speakerdeck.com/vjeux/react-css-in-js) for ClojureScript

[![Clojars](https://img.shields.io/clojars/v/org.roman01la/cljss.svg)](https://clojars.org/org.roman01la/cljss)
[![CircleCI](https://circleci.com/gh/roman01la/cljss.svg?style=svg)](https://circleci.com/gh/roman01la/cljss)

<a href="https://www.patreon.com/bePatron?c=1239559">
  <img src="https://c5.patreon.com/external/logo/become_a_patron_button.png" height="40px" />
</a>

## Table of Contents
- [Why write CSS in ClojureScript?](#why-write-css-in-clojurescript)
- [Features](#features)
- [How it works](#how-it-works)
- [Installation](#installation)
- [Usage](#usage)
- [Production build](#production-build)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

## Why write CSS in ClojureScript?

Writing styles this way has the same benefits as writing components that keep together view logic and presentation. It all comes to developer efficiency and maintainability.

Thease are some resources that can give you more context:

- [“A Unified Styling Language”](https://medium.com/seek-blog/a-unified-styling-language-d0c208de2660) by Mark Dalgleish
- [“A Unified Styling Language”](https://www.youtube.com/watch?v=X_uTCnaRe94) (talk) by Mark Dalgleish
- [“The road to styled components: CSS in component-based systems”](https://www.youtube.com/watch?v=MT4D_DioYC8) by Glen Maddern

## Features
- Automatic scoped styles by generating unique names
- CSS pseudo-classes and pseudo-elements
- CSS animations via `@keyframes` at-rule
- Injects styles into `<style>` tag at run-time
- Debuggable styles in development (set via `goog.DEBUG`)
- Fast, 10000 insertions in ~200ms

## How it works

### `defstyles`

`defstyles` macro expands into a function which accepts arbitrary number of arguments and returns a string of auto-generated class names that references both static and dynamic styles.

```clojure
(defstyles button [bg]
  {:font-size "14px"
   :background-color bg})

(button "#000")
;; "-css-43696 -vars-43696"
```

Dynamic styles are updated via CSS Variables (see [browser support](http://caniuse.com/#feat=css-variables)).

### `defstyled`

`defstyled` macro accepts var name, HTML element tag name as a keyword and a hash of styles.

The macro expands into a function which accepts optional hash of attributes and child components, and returns React element. It is available for Om, Rum and Reagent libraries. Each of them are in corresponding namespaces: `cljss.om/defstyled`, `cljss.rum/defstyled` and `cljss.reagent/defstyled`.

A hash of attributes with dynamic CSS values as well as normal HTML attributes can be passed into underlying React element. Reading from attributes hash map can be done via anything that satisfies `cljs.core/ifn?` predicate (`Fn` and `IFn` protocols, and normal functions).

_NOTE: Dynamic props that used only to compute styles are also passed onto React element and thus result in adding an unknown attribute on a DOM node. To prevent this it is recommended to use keyword or a function marked with `with-meta` so the library can remove those props (see example below)._

- keyword value — reads the value from props map and removes matching attribute
- `with-meta` with a single keyword — passes a value of a specified attribute from props map into a function and removes matching attribute
- `with-meta` with a collection of keywords — passes values of specified attributes from props map into a function in the order of these attributes and removes matching attributes

```clojure
(defstyled h1 :h1
  {:font-family "sans-serif"
   :font-size :size ;; reads the value and removes custom `:size` attribute
   :color (with-meta #(get {:light "#fff" :dark "#000"} %) :color)} ;; gets `:color` value and removes this attribute
   :padding (with-meta #(str %1 " " %2) [:padding-v :padding-h])) ;; gets values of specified attrs as arguments and remove those attrs

(h1 {:size "32px" ;; custom attr
     :color :dark ;; custom attr
     :padding-v "8px" ;; custom attr
     :padding-h "4px" ;; custom attr
     :margin "8px 16px"} ;; normal CSS rule
    "Hello, world!")
;; (js/React.createElement "h1" #js {:className "css-43697 vars-43697"} "Hello, world!")
```

#### predicate attributes in `defstyled`

Sometimes you want toggle between two values. In this example a menu item can switch between active and non-active styles using `:active?` attribute.
```clojure
(defstyled MenuItem :li
  {:color (with-meta #(if % "black" "grey") :active?)})

(MenuItem {:active? true})
```

Because this pattern is so common there's a special treatment for predicate attributes (keywords ending with `?`) in styles definition.
```clojure
(defstyled MenuItem :li
  {:color "grey"
   :active? {:color "black"}})

(MenuItem {:active? true})
```

### `:css` attribute

`:css` attribute allows to define styles inline and still benefit from CSS-in-JS approach.

_NOTE: This feature is supported only for Rum/Sablono elements_

```clojure
(def color "#000")

[:button {:css {:color color}} "Button"]
;; (js/React.createElement "button" #js {:className "css-43697 vars-43697"} "Button")
```

### `defkeyframes`

`defkeyframes` macro expands into a function which accepts arbitrary number of arguments, injects @keyframes declaration and returns a string that is an animation name.

```clojure
(defkeyframes spin [from to]
  {:from {:transform (str "rotate(" from "deg)")
   :to   {:transform (str "rotate(" to "deg)")}})

[:div {:style {:animation (str (spin 0 180) "500ms ease infinite")}}]
;; (js/React.createElement "div" #js {:style #js {:animation "animation-43697 500ms ease infinite"}})
```

## Installation

Add to project.clj: `[org.roman01la/cljss "1.5.6"]`

## Usage

`(defstyles name [args] styles)`

- `name` name of a var
- `[args]` arguments
- `styles` a hash map of styles definition

```clojure
(ns example.core
  (:require [cljss.core :refer [defstyles]]))

(defstyles button [bg]
  {:font-size "14px"
   :background-color bg})

[:div {:class (button "#fafafa")}]
```

`(defstyled name tag-name styles)`

- `name` name of a var
- `tag-name` HTML tag name as a keyword
- `styles` a hash map of styles definition

Using [Sablono](https://github.com/r0man/sablono) templating for [React](https://facebook.github.io/react/)
```clojure
(ns example.core
  (:require [sablono.core :refer [html]]
            [cljss.rum :refer [defstyled]]))

(defstyled Button :button
  {:padding "16px"
   :margin-top :v-margin
   :margin-bottom :v-margin})

(html
  (Button {:v-margin "8px"
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

## Roadmap
- Media Queries syntax
- Server-side rendering

## Contributing
- Pick an issue with `help wanted` label (make sure no one is working on it)
- Stick to project's code style as much as possible
- Make small commits with descriptive commit messages
- Submit a PR with detailed description of what was done


## Development

A repl for the example project is provided via [lein-figwheel](https://github.com/bhauman/lein-figwheel).

```
$ cd example
$ lein figwheel
```

If using emacs [cider](https://github.com/clojure-emacs/cider) - you can also launch the repl using `M-x cider-jack-in-clojurescript`.

## License

Copyright © 2017 Roman Liutikov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
