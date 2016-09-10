package com.prueba.miguelforero.models;

/**
 * Created by Miguel Forero on 09/09/2016.
 */
public class AppModel {

        private String title;
        private String category;
        private int rating;
        private String image;
        private String story;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }


        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getStory() {
            return story;
        }

        public void setStory(String story) {
            this.story = story;
        }
    }
