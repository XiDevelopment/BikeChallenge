package at.xidev.bikechallenge.model;

import java.util.List;

public class Statistic {
    private int score;
    private double emissions;
    private double fuel;
    private double avgDistance;
    private double longestDistance;
    private double totalDistance;
    private long avgTime;
    private long longestTime;
    private long totalTime;
    private List<Double> last7DaysDistances;
    private List<Long> last7DaysTimes;
    private double globalEmissions;
    private double globalFuel;
    private long globalTime;
    private double globalDistance;

    public Statistic(int score, double emissions, double fuel, double avgDistance, double longestDistance, double totalDistance, long avgTime, long longestTime, long totalTime, List<Double> last7DaysDistances, List<Long> last7DaysTimes, double globalEmissions, double globalFuel, long globalTime, double globalDistance) {
        this.score = score;
        this.emissions = emissions;
        this.fuel = fuel;
        this.avgDistance = avgDistance;
        this.longestDistance = longestDistance;
        this.totalDistance = totalDistance;
        this.avgTime = avgTime;
        this.longestTime = longestTime;
        this.totalTime = totalTime;
        this.last7DaysDistances = last7DaysDistances;
        this.last7DaysTimes = last7DaysTimes;
        this.globalEmissions = globalEmissions;
        this.globalFuel = globalFuel;
        this.globalTime = globalTime;
        this.globalDistance = globalDistance;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getEmissions() {
        return emissions;
    }

    public void setEmissions(double emissions) {
        this.emissions = emissions;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public double getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(double avgDistance) {
        this.avgDistance = avgDistance;
    }

    public double getLongestDistance() {
        return longestDistance;
    }

    public void setLongestDistance(double longestDistance) {
        this.longestDistance = longestDistance;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(long avgTime) {
        this.avgTime = avgTime;
    }

    public long getLongestTime() {
        return longestTime;
    }

    public void setLongestTime(long longestTime) {
        this.longestTime = longestTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public List<Double> getLast7DaysDistances() {
        return last7DaysDistances;
    }

    public void setLast7DaysDistances(List<Double> last7DaysDistances) {
        this.last7DaysDistances = last7DaysDistances;
    }

    public List<Long> getLast7DaysTimes() {
        return last7DaysTimes;
    }

    public void setLast7DaysTimes(List<Long> last7DaysTimes) {
        this.last7DaysTimes = last7DaysTimes;
    }

    public double getGlobalEmissions() {
        return globalEmissions;
    }

    public void setGlobalEmissions(double globalEmissions) {
        this.globalEmissions = globalEmissions;
    }

    public double getGlobalFuel() {
        return globalFuel;
    }

    public void setGlobalFuel(double globalFuel) {
        this.globalFuel = globalFuel;
    }

    public long getGlobalTime() {
        return globalTime;
    }

    public void setGlobalTime(long globalTime) {
        this.globalTime = globalTime;
    }

    public double getGlobalDistance() {
        return globalDistance;
    }

    public void setGlobalDistance(double globalDistance) {
        this.globalDistance = globalDistance;
    }
}
