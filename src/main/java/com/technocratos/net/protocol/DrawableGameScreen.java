package com.technocratos.net.protocol;

import com.technocratos.DBManager;
import com.technocratos.PlayerDAO;
import com.technocratos.model.Player;

import java.awt.*;
import java.util.List;

public class DrawableGameScreen {
    protected static final int LANE_HEIGHT = 160;
    protected static final int CIRCLE_RADIUS = 40;
    protected static final int LANE_STEP = 180;
    private PlayerDAO playerDAO = new PlayerDAO();

    public void drawStartScreen(Graphics2D g2, int width, int height, boolean ready) {
        g2.setColor(new Color(35, 35, 45));
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "Сетевая игра";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, width / 2 - tw / 2, 120);

        g2.setFont(new Font("Arial", Font.PLAIN, 22));

        if (!ready) {
            String text = "Нажмите кнопку для игры";
            int w = g2.getFontMetrics().stringWidth(text);
            g2.setColor(new Color(220, 220, 255));
            g2.drawString(text, width / 2 - w / 2, 180);

            Rectangle playButton = new Rectangle(300, 350, 200, 60);
            drawButton(g2, "ИГРАТЬ", playButton, new Color(70, 130, 200));

            drawLeaderboardTable(g2, width, 450, "Лучшие результаты", false);
        } else {
            String text = "Ожидание второго игрока...";
            int w = g2.getFontMetrics().stringWidth(text);
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(text, width / 2 - w / 2, 220);

            drawLeaderboardTable(g2, width, 300, "Лучшие результаты", false);
        }
    }

    public void drawLeaderboardBeforeStart(Graphics2D g2, int width, int height, int score, boolean gameStsrt) {
        if (gameStsrt) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, width, height);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 38));
            String title = "ИГРА ОКОНЧЕНА";
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, width / 2 - titleWidth / 2, 80);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            String scoreText = "Ваш счет: " + score;
            int scoreWidth = g2.getFontMetrics().stringWidth(scoreText);
            g2.drawString(scoreText, width / 2 - scoreWidth / 2, 140);

            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.setColor(new Color(180, 220, 255));


            drawLeaderboardTable(g2, width, 200, "Топ игроков", true);
        }
    }

    private void drawLeaderboardTable(Graphics2D g2, int width, int startY, String title, boolean isResults) {
        List<Player> topPlayers = playerDAO.getAllPlayers();

        if (!topPlayers.isEmpty()) {
            int tableWidth = 500;
            int tableHeight = 70 + Math.min(topPlayers.size(), 5) * 35;
            int xPos = width / 2 - tableWidth / 2;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(xPos, startY, tableWidth, tableHeight, 10, 10);

            g2.setColor(new Color(100, 100, 150));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(xPos, startY, tableWidth, tableHeight, 10, 10);

            g2.setColor(new Color(50, 50, 120));
            g2.setFont(new Font("Arial", Font.BOLD, isResults ? 22 : 20));
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, width / 2 - titleWidth / 2, startY + 30);

            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            g2.setColor(Color.BLACK);

            int yPos = startY + 60;
            int maxPlayers = Math.min(topPlayers.size(), 5);

            for (int i = 0; i < maxPlayers; i++) {
                Player player = topPlayers.get(i);

                if (i % 2 == 0) {
                    g2.setColor(new Color(240, 245, 255));
                } else {
                    g2.setColor(new Color(220, 230, 245));
                }
                g2.fillRect(xPos + 10, yPos - 20, tableWidth - 20, 25);

                g2.setColor(Color.BLACK);
                String playerText;
                if (isResults) {
                    playerText = String.format("%d. %s - %d (%s)",
                            i + 1, player.getUsername(), player.getScore(), player.getType());
                } else {
                    playerText = String.format("%d. %s - %d",
                            i + 1, player.getUsername(), player.getScore());
                }
                g2.drawString(playerText, xPos + 20, yPos);

                yPos += 35;
            }
        }
    }

    public void drawButton(Graphics2D g2, String text, Rectangle r, Color color) {
        g2.setColor(color);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 15, 15);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text,
                r.x + r.width / 2 - fm.stringWidth(text) / 2,
                r.y + r.height / 2 + fm.getAscent() / 2 - 4);
    }

    public void drawCircle(Graphics2D g2, int x, int y, String text, Color circleColor) {
        g2.setColor(circleColor);
        g2.fillOval(x - CIRCLE_RADIUS, y - CIRCLE_RADIUS,
                CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(x - CIRCLE_RADIUS, y - CIRCLE_RADIUS,
                CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x - width/2, y + 8);
    }

    public void drawLanes(Graphics2D g2, int width) {
        for (int lane = 0; lane < 3; lane++) {
            int y = 150 + lane * LANE_STEP;
            g2.setColor(new Color(230, 235, 240));
            g2.fillRect(0, y - LANE_HEIGHT / 2, width, LANE_HEIGHT);
            g2.setColor(new Color(180, 190, 200));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(0, y - LANE_HEIGHT / 2, width, y - LANE_HEIGHT / 2);
            g2.drawLine(0, y + LANE_HEIGHT / 2, width, y + LANE_HEIGHT / 2);
        }
    }

    public void drawConnectionLostScreen(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(60, 30, 30));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(255, 100, 100));
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "СОЕДИНЕНИЕ ПОТЕРЯНО";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, width/2 - tw/2, 200);

        g2.setColor(new Color(255, 200, 200));
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String text = "Связь с сервером разорвана";
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, width/2 - w/2, 250);
    }

    public void drawFon(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(240, 245, 250));
        g2.fillRect(0, 0, width, height);
    }

    public void drawScoreAndTime(Graphics2D g2, int scoreClient, int timeLeft) {
        g2.setColor(new Color(50, 60, 80, 220));
        g2.fillRoundRect(15, 15, 200, 70, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Счёт: " + scoreClient, 30, 45);
        g2.drawString("Время: " + timeLeft, 30, 70);
    }

    public void drawErrorTable(Graphics2D g2, String selected, int width) {
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        g2.setColor(Color.RED);
        String text = String.format("Не попал, ты ловишь %s!", selected);
        int w = g2.getFontMetrics().stringWidth(text);

        g2.drawString(
                text,
                width / 2 - w / 2,
                120
        );
    }

    public void drawWaitScreen(Graphics2D g2, int width, int height, boolean clientReady) {
        g2.setColor(new Color(35, 35, 45));
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "Сетевая игра";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, width / 2 - tw / 2, 120);

        g2.setFont(new Font("Arial", Font.PLAIN, 22));

        if (clientReady) {
            String text = "Нажмите в любом месте для начала игры";
            int w = g2.getFontMetrics().stringWidth(text);
            g2.setColor(new Color(220, 220, 255));
            g2.drawString(text, width / 2 - w / 2, 200);

            drawLeaderboardTable(g2, width, 300, "Лучшие результаты", false);
        } else {
            String text = "Ожидание подключения игрока...";
            int w = g2.getFontMetrics().stringWidth(text);
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(text, width / 2 - w / 2, 200);

            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            g2.setColor(new Color(180, 220, 255));

            drawLeaderboardTable(g2, width, 300, "Лучшие результаты", false);
        }
    }
}