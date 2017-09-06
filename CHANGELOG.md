## 1.4.0
- Cache inserted rules
- Add experimental support for `:css` attribute

## 1.3.1
- Fix child elements rendering

## 1.3.0
- `defstyled` for Rum, Om and Reagent

## 1.2.0
- Enable debuggable styles in development (set via `goog.DEBUG`)
- Remove static styles extraction into a file

## 1.1.0
- Styled React components via `defstyled` macro

## 1.0.0-SNAPSHOT
- Dynamic styles via CSS Vars

## 0.1.0-SNAPSHOT
- Initial release
- Exclude mapping for selectors with pseudo classes and elements [19b55d7d](https://github.com/roman01la/cljss/commit/19b55d7dcd8053dbd35fdcc7f4ec3de0ab4396e0)
- Fix NullPointerException when cljs.env/*compiler* is nil [59bf4d33](https://github.com/roman01la/cljss/commit/59bf4d3346f6af55322511df8b0de8e9dc0640ed)
- Document alternative `:validate-config` Figwheel option [15a2dc8f](https://github.com/roman01la/cljss/commit/15a2dc8fda08b3fe9a58f794712add412ec8a676)
- Use resolved var as a unique id instead of passing it manually [a76f2aec](https://github.com/roman01la/cljss/commit/a76f2aeccc94e5c29b47511286cfadaab7ce6936)
- Note an issue about code reloading and React components with `shouldComponentUpdate` optimization enabled [0b0f7295](https://github.com/roman01la/cljss/commit/0b0f7295c876574107f1ca86191363b3e89b11e8)
