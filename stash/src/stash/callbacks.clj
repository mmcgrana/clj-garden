(ns stash.callbacks)

(defn run-callbacks
  [callback-name instance]
  (let [callbacks (callback-name (:callbacks (:model (meta instance))))]
    (loop [instance instance callbacks callbacks]
      (if-let [callback (first callbacks)]
        (let [[c-instance success] (callback instance)]
          (if success
            (recur c-instance (rest callbacks))
            c-instance))
        instance))))


; before-validation-on-create
;   validate-on-create
; after-validation-on-create
; before-create
;   create
; after-create
;
; before-validation-on-update
;   validate-on-update
; after-validation-on-update
; before-update
;   update
; after-update
;
; before-destroy
;   destroy
; after-destroy

; (call-if something conditions)
; (fn [instance] (if (condition instance) (something instance) instance))
; 
; (defn email-admin [instance]
;   (send-email "hai" (:email instance)))
; 
; (defn admin-writer? [instance]
;   (user/admin? (writer instance)))
; 
; (defmacro conditionally [f condition pred]
;   (case
;     condition
;     :if
;     `(fn [instance] (if (~pred instance) (f instance) instance))))
; [save-draft (conditionally email-admin :if admin-writer?)]
; [save-draft #(if (admin-writer? %) (email-admin %))]
; before- and after- save are pure sugar
; before- and after- validation are pure sugar

