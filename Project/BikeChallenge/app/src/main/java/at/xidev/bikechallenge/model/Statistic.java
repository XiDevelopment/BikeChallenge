package at.xidev.bikechallenge.model;

import java.io.Serializable;
import java.util.List;

public class Statistic implements Serializable {
    private float totalDistance;
    private float longestDistance;
    private float avgDistance;
    private List<Float> last7Days;
    private float totalTime;
    private float avgTime;
    private float emission;
    private float fuel;

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public float getLongestDistance() {
        return longestDistance;
    }

    public void setLongestDistance(float longestDistance) {
        this.longestDistance = longestDistance;
    }

    public float getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(float avgDistance) {
        this.avgDistance = avgDistance;
    }

    public List<Float> getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(List<Float> last7Days) {
        this.last7Days = last7Days;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }

    public float getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(float avgTime) {
        this.avgTime = avgTime;
    }

    public float getEmission() {
        return emission;
    }

    public void setEmission(float emission) {
        this.emission = emission;
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }
}
