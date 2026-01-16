package com.technocratos.model;

public class Circle {
    private int lane;
    private int x;
    private int speed;
    private String element;
    private static final int CIRCLE_RADIUS = 40;
    private static final int LANE_STEP = 180;


    public Circle(int lane, int x, int speed, String element) {
        this.lane = lane;
        this.x = x;
        this.speed = speed;
        this.element = element;
    }

    public void move() {
        x += speed; // просто двигаем
    }

    public boolean contains(int cx, int cy) {
        int y = getLaneY();
        return Math.hypot(cx - x, cy - y) < CIRCLE_RADIUS; //попали по кружку
    }

    public int getLaneY() {
        return 150 + lane * LANE_STEP;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }
}
