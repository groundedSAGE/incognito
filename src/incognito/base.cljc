(ns incognito.base
  #?(:clj (:refer-clojure :exclude [read-string]))
  (:require #?(:clj [clojure.edn :refer [read-string]]
               :cljs [cljs.reader :refer [read-string
                                          *tag-table* *default-data-reader-fn*]])))

(defrecord IncognitoTaggedLiteral [tag value])

(defn pr-str->pure-read-string [r]
  (let [r-str (pr-str r)]
    #?(:cljs (binding [*tag-table* (atom (select-keys @cljs.reader/*tag-table*
                                                      ["inst" "uuid" "queue" "js"]))
                       *default-data-reader-fn* (atom (fn [t v] [t v]))]
               (read-string r-str))
       :clj (read-string {:readers {} :default (fn [t v] [t v])} r-str))))

(defn incognito-reader [read-handlers m]
  (if (read-handlers (:tag m))
    ((read-handlers (:tag m)) (:value m))
    (map->IncognitoTaggedLiteral m)))

(defn incognito-writer [write-handlers r]
  (let [t (type r)
        [tag v] (if (write-handlers t)
                  ((write-handlers t) r)
                  (pr-str->pure-read-string r))]
    {:tag tag
     :value v}))

(comment
  (defrecord Foos [a])

  (cljs-type (map->Foos {:a 4}))

  (incognito-writer {incognito.base.Foos (fn [r] ['incognito.base.Foos (assoc r :c "bananas")])}
                    (map->Foos {:a [1 2 3] :b {:c "Fooos"}}))

  (incognito-writer {} (map->Foos {:a [1 2 3] :b {:c "Fooos"}}))

  )
