(ns be.core
(:require [compojure.core :refer :all]
           [compojure.route :as route]
           [ring.adapter.jetty :refer [run-jetty]]
           [ring.middleware.params :refer [wrap-params]]
           [hiccup.core :refer [html]]
           [cheshire.core :as json]
           [clojure.java.io :as io]
           [clojure.string :as str]))

(defn load-todos []
  (if (.exists (io/file "todos.json"))
    (json/parse-string (slurp "todos.json") true)
    []))

(def todos (atom (load-todos)))

(defn save-todos []
  (spit "todos.json" (json/generate-string @todos)))

(defn generate-id []
  (str (java.util.UUID/randomUUID)))

(defn filter-todos [filter-type]
  (case filter-type
    "completed" (filter :completed @todos)
    "active" (filter (complement :completed) @todos)
    @todos))

(defn todo-page [filter-type]
  (html
   [:html
    [:head
     [:title "To-Do List"]
     [:style "
        body { background-color: #121212; color: #FFFFFF; font-family: Arial, sans-serif; margin: 0; padding: 0; box-sizing: border-box; }
        .container { width: 90%; max-width: 800px; margin: auto; padding-top: 50px; }
        h1 { text-align: center; }
        form { text-align: center; margin-bottom: 20px; }
        input[type='text'] { padding: 10px; margin-right: 10px; border: none; border-radius: 5px; width: calc(100% - 120px); max-width: 500px; }
        input[type='submit'] { padding: 10px; background-color: #6200EE; color: white; border: none; border-radius: 5px; cursor: pointer; }
        ul { list-style-type: none; padding: 0; }
        li { background-color: #333333; margin: 5px 0; padding: 10px; border-radius: 5px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
        .completed { text-decoration: line-through; color: #888888; }
        button { margin-left: 10px; padding: 5px 10px; border: none; border-radius: 5px; cursor: pointer; }
        .complete-button { background-color: #008000; color: white; }
        .delete-button { background-color: #FF0000; color: white; }
        .edit-button { background-color: #FFA500; color: white; }
        .filter-buttons { text-align: center; margin-top: 20px; }
        .filter-buttons button { margin: 0 5px; }
        @media (max-width: 600px) {
          li { flex-direction: column; align-items: flex-start; }
          button { margin: 5px 0 0 0; }
        }"]]
    [:body
     [:div.container
      [:h1 "To-Do List"]
      [:form {:action "/add-todo" :method "post"}
       [:input {:type "text" :name "task" :placeholder "Enter a new task"}]
       [:input {:type "submit" :value "Add"}]]
      [:ul
       (for [{:keys [id task completed] :as todo} (filter-todos filter-type)]
         [:li {:class (when completed "completed")}
          task
          [:div
           [:form {:action "/toggle-complete-todo" :method "post" :style "display:inline;"}
            [:input {:type "hidden" :name "id" :value id}]
            [:button.complete-button {:type "submit"} (if completed "Undo" "Complete")]]
           [:form {:action "/edit-todo" :method "get" :style "display:inline;"}
            [:input {:type "hidden" :name "id" :value id}]
            [:input {:type "hidden" :name "task" :value task}]
            [:button.edit-button {:type "submit"} "Edit"]]
           [:form {:action "/delete-todo" :method "post" :style "display:inline;"}
            [:input {:type "hidden" :name "id" :value id}]
            [:button.delete-button {:type "submit"} "Delete"]]]])]
      [:div.filter-buttons
       [:form {:action "/" :method "get" :style "display:inline;"}
        [:input {:type "hidden" :name "filter" :value "all"}]
        [:button {:type "submit"} "All"]]
       [:form {:action "/" :method "get" :style "display:inline;"}
        [:input {:type "hidden" :name "filter" :value "active"}]
        [:button {:type "submit"} "Active"]]
       [:form {:action "/" :method "get" :style "display:inline;"}
        [:input {:type "hidden" :name "filter" :value "completed"}]
        [:button {:type "submit"} "Completed"]]]]]]))

(defn edit-page [params]
  (html
   [:html
    [:head
     [:title "Edit To-Do"]
     [:style "
        body { background-color: #121212; color: #FFFFFF; font-family: Arial, sans-serif; margin: 0; padding: 0; box-sizing: border-box; }
        .container { width: 90%; max-width: 800px; margin: auto; padding-top: 50px; }
        h1 { text-align: center; }
        form { text-align: center; margin-bottom: 20px; }
        input[type='text'] { padding: 10px; margin-right: 10px; border: none; border-radius: 5px; width: calc(100% - 120px); max-width: 500px; }
        input[type='submit'] { padding: 10px; background-color: #6200EE; color: white; border: none; border-radius: 5px; cursor: pointer; }"]]
    [:body
     [:div.container
      [:h1 "Edit To-Do"]
      [:form {:action "/update-todo" :method "post"}
       [:input {:type "hidden" :name "id" :value (params "id")}]
       [:input {:type "text" :name "task" :value (params "task")}]
       [:input {:type "submit" :value "Update"}]]]]]))

(defn handle-add-todo [params]
  (let [task (params "task")]
    (when (not (empty? task))
      (swap! todos conj {:id (generate-id) :task task :completed false})
      (save-todos)))
  {:status 302 :headers {"Location" "/"}})

(defn handle-toggle-complete-todo [params]
  (let [id (params "id")]
    (swap! todos (fn [ts] (map #(if (= (:id %) id) (update % :completed not) %) ts)))
    (save-todos))
  {:status 302 :headers {"Location" "/"}})

(defn handle-delete-todo [params]
  (let [id (params "id")]
    (swap! todos (fn [ts] (remove #(= (:id %) id) ts)))
    (save-todos))
  {:status 302 :headers {"Location" "/"}})

(defn handle-update-todo [params]
  (let [id (params "id")
        task (params "task")]
    (swap! todos (fn [ts] (map #(if (= (:id %) id) (assoc % :task task) %) ts)))
    (save-todos))
  {:status 302 :headers {"Location" "/"}})

(defroutes app-routes
  (GET "/" {params :params} (todo-page (get params "filter" "all")))
  (POST "/add-todo" {params :params} (handle-add-todo params))
  (POST "/toggle-complete-todo" {params :params} (handle-toggle-complete-todo params))
  (POST "/delete-todo" {params :params} (handle-delete-todo params))
  (GET "/edit-todo" {params :params} (edit-page params))
  (POST "/update-todo" {params :params} (handle-update-todo params))
  (route/not-found "Not Found"))

(def app
  (wrap-params app-routes))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "10000"))
        host "0.0.0.0"]
    (run-jetty app {:port port :host host :join? false})))