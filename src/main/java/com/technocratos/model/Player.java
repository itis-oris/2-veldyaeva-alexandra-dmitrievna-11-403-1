package com.technocratos.model;

/**
 * игрок имя и очки
 */
public class Player {
    public String username;
    public Integer score;
    private String type;

    public Player(String username, Integer score, String type) {
        this.username = username;
        this.score = score;
        this.type = type;
    }

    public Player() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Player{");
        sb.append("username='").append(username).append('\'');
        sb.append(", score=").append(score);
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
