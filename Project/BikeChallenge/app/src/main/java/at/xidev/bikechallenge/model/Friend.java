package at.xidev.bikechallenge.model;

import android.graphics.drawable.Drawable;

/**
 * Created by int3r on 14.04.2014.
 */
public class Friend {
    private Integer id;
    private String name;
    private Integer score;
    private Drawable image;

    public Friend(int id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
        // this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
