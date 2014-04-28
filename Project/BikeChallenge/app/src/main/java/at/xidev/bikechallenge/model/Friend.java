package at.xidev.bikechallenge.model;

import android.graphics.drawable.Drawable;

/**
 * Created by int3r on 14.04.2014.
 */
public class Friend {
    private int id;
    private String name;
    private int points;
    private Drawable image;

    public Friend(int id, String name, int points) {
        this.id = id;
        this.name = name;
        this.points = points;
       // this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
