(ns cljss.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljss.core-test]))

(doo-tests 'cljss.core-test)
