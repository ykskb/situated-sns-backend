(ns situated-sns.signal
  (:require [situated-sns.db :as d]
            [situated-sns.middleware :as mid]))

;;; Signals

(defn- pre-create-user [args ctx]
  (let [payload (get-in ctx [:req :auth-payload])
        user (get-in ctx [:req :auth-user])]
    (if user
      nil
      (-> args
          (assoc :auth_id (:uid payload))
          (assoc :email (:email payload))))))

(defn- post-update-user [args ctx]
  (let [variables (get-in ctx [:req :body-params :variables])
        user (mid/user-or-throw (:req ctx))]
    (when (and (:username variables) (:slug variables))
      (d/update! (:db ctx) :enduser {:id (:id user)} {:is_valid true}))
    args))

(defn- update-count-column! [increment? table-key col-key pk-path-map res ctx]
  (let [variables (get-in ctx [:req :body-params :variables])
        pk-map (reduce-kv (fn [m k v]
                            (assoc m k (get-in variables v)))
                          {} pk-path-map)
        params {col-key [:raw [(name col-key) (if increment? " + 1" " - 1")]]}]
    (d/update! (:db ctx) table-key pk-map params)
    res))

(defn- update-created-by [args ctx]
  (let [user (mid/user-or-throw (:req ctx))]
    (assoc args :created_by (:id user))))

(defn- update-pk-col-auth [col-key args ctx]
  (let [user (mid/user-or-throw (:req ctx))]
    (assoc-in args [:pk_columns col-key] (:id user))))

(defn- dissoc-arg-keys [keys-to-dissoc args _ctx]
  (reduce #(dissoc %1 %2) args keys-to-dissoc))

(defn- update-where-auth-or [sql-params ctx]
  (let [user (mid/user-or-throw (:req ctx))
        user-id (:id user)]
    (update sql-params :where conj
            [:or [:= :created_by user-id] [:= :enduser_id user-id]])))

(defn- validate-msg-creation [args ctx]
  (let [user (mid/user-or-throw (:req ctx))
        user-id (:id user)
        chat (first (d/list-up (:db ctx) :enduser_chat
                               {:where [:= :id (:chat_id args)]}))]
    (let [created-by (:created_by chat)
          enduser-id (:enduser_id chat)]
      (if (or (= created-by user-id) (= enduser-id user-id))
        (-> args
            (assoc :enduser_id (if (= created-by user-id) enduser-id created-by))
            (assoc :created_by user-id))
        (throw (ex-info "Access not allowed to this chat." {}))))))

;; (defn- update-where-created-by [sql-params ctx]
;;   (let [user (mid/user-or-throw (:req ctx))]
;;     (update sql-params :where conj [:= :created_by (:id user)])))

(def signals
  {:enduser {:create {:pre pre-create-user}
             :update {:pre [(partial update-pk-col-auth :id)
                            (partial dissoc-arg-keys
                                     [:auth_id :email :profile_image_url])]
                      :post post-update-user}}
   :enduser_follow {:create {:pre update-created-by}
                    :delete {:pre (partial update-pk-col-auth :created_by)}}
   :post {:create {:pre update-created-by}}
   :post_comment {:create {:pre update-created-by
                           :post (partial update-count-column! true :post
                                          :comment_count {:id [:postId]})}}
   :post_comment_reply {:create {:pre update-created-by}}
   :post_like
   {:create {:pre [update-created-by]
             :post (partial update-count-column! true
                            :post :like_count {:id [:postId]})}
    :delete {:pre [(partial update-pk-col-auth :created_by)]
             :post (partial update-count-column! false
                            :post :like_count {:id [:pk_columns :post_id]})}}
   :post_comment_like
   {:create {:pre [update-created-by]
             :post (partial update-count-column! true :post_comment :like_count
                            {:id [:postCommentId]})}
    :delete {:pre (partial update-pk-col-auth :created-by)
             :post (partial update-count-column! false :post_comment :like_count
                            {:id [:pk_columns :post_comment_id]})}}
   :post_comment_reply_like
   {:create {:pre update-created-by
             :post (partial update-count-column! true :post_comment_reply
                            :like_count {:id [:postCommentReplyId]})}
    :delete {:pre (partial update-pk-col-auth :created-by)
             :post (partial update-count-column! false :post_comment_reply
                            :like_count {:id [:pk_columns
                                              :post_comment_reply_id]})}}
   :enduser_chat {:query {:pre update-where-auth-or}
                  :create {:pre update-created-by}}
   :enduser_message {:query {:pre update-where-auth-or}
                     :create {:pre validate-msg-creation}}
   :post_dislike {:create {:pre update-created-by}}
   :tag_follow {:create {:pre update-created-by}}})

