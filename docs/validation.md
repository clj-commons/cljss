# CSS Validation

`(ns cljss.validation.css)`

## Misspelled CSS properties

Misspelling is detected using Sørensen–Dice coefficient function which is being run againts a list of all CSS properties. A warning proposes possible fixes: the best match and three more results.

    WARNING - Misspelled CSS property
    Looks like you've misspelled ":posiion" CSS property. Should it be ":position" or one of these: (:ruby-position :mask-position :fill-position)?

## CSS values DSL and validation

DSL for CSS values is parsed, validated and compiled with `clojure.spec`

**Example**
```clojure
(defstyles header []
  {:background-color [255 255 256]})
```

    WARNING - Invalid CSS syntax
    Invalid color channel value 256 found in ":background-color [255 255 256]". Make sure the value is within range 0-255.
    
### Grammar

#### background-color

- HEX RGB `"#fff"`
- HEX RGBA `"#ffff"`
- HEX RRGGBB `"#ffffff"`
- HEX RRGGBBAA `"ffffffff"`
- RGB `[255 255 255]`
- RGBA `[255 255 255 1]`

#### margin
- single `[8 :px]`
- vertical-horizontal `[8 :px 16 :px]`
- top-horizontal-bottom `[8 :px 16 :px 4 :px]`
- top-right-bottom-let `[8 :px 16 :px 4 :px 8 :px]`
