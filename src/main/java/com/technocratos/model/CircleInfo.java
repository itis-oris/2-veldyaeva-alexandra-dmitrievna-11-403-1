package com.technocratos.model;

public class CircleInfo {
    private int lane;
    private int x;
    private int speed;
    private boolean active;
    private String elementName;
    private static final int LANE_STEP = 180;

    public CircleInfo(int lane, int x, int speed, boolean active, String elementName) {
        this.lane = lane;
        this.x = x;
        this.speed = speed;
        this.active = active;
        this.elementName = elementName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
