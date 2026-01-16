package com.technocratos.net.client;

import com.technocratos.model.CircleInfo;
import com.technocratos.model.Player;
import com.technocratos.net.protocol.DrawableGameScreen;
import com.technocratos.net.protocol.DrawableGameScreenClient;
import com.technocratos.net.protocol.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class GameComponentClient extends JComponent {
    private String playerName;
    private String selected;
    private boolean resSet = true;
    long currentTime;
    private final long TIMEOUT = 2000;
    private Rectangle restartButton = new Rectangle(300, 500, 200, 60);
    private List<Player> topPlayers = new ArrayList<>();
    List<CircleInfo> circles = new ArrayList<>();
    int scoreClient = 0;
    int timeLeft = 60;
    Rectangle backButton = new Rectangle(300, 420, 200, 60);
    boolean gameActive = false;
    boolean gameStarted = false;
    boolean ready = false;
    boolean connectionAlive = true;
    private boolean showMissingClient;
    DataInputStream in;
    DataOutputStream out;
    Socket socket;
    MainFrame mainFrame;
    Rectangle playButton = new Rectangle(300, 350, 200, 60);
    private DrawableGameScreenClient drawableGameScreen = new DrawableGameScreenClient();
    private Timer returnToMenuTimer;
    private boolean running = true;

    public GameComponentClient(Socket socket) throws IOException {
        this.socket = socket;
        setPreferredSize(new Dimension(900, 650));
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!connectionAlive) return;

                if (backButton.contains(e.getPoint())) {
                    SwingUtilities.invokeLater(() -> {
                        mainFrame.returnToMenu();
                    });
                    return;
                }

                if (gameActive && gameStarted) {
                    try {
                        out.writeInt(0);
                        out.writeInt(e.getX());
                        out.writeInt(e.getY());
                        out.flush();
                    } catch (IOException ex) {
                        connectionLost();
                    }
                } else if (!gameStarted && playButton.contains(e.getPoint())) {
                    try {
                        out.writeInt(3);
                        out.writeUTF("READY");
                        out.flush();
                        ready = true;
                        repaint();
                    } catch (IOException ex) {
                        connectionLost();
                    }
                }
                if (!gameActive && gameStarted && restartButton.contains(e.getPoint())) {
                    try {
                        out.writeInt(7);
                        out.flush();
                    } catch (IOException ex) {
                        connectionLost();
                    }
                }
            }
        });

        returnToMenuTimer = new Timer(3000, e -> {
            if (mainFrame != null) {
                mainFrame.returnToMenu();
            }
        });
        returnToMenuTimer.setRepeats(false);

        new Thread(this::readLoop).start();
        requestLeaderboard();
    }

    private void requestLeaderboard() {
        try {
            out.writeInt(9);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (connectionAlive) {
                int message = in.readInt();

                switch (message) {
                    case 4:
                        gameStarted = true;
                        gameActive = true;
                        SwingUtilities.invokeLater(() -> repaint());
                        break;
                    case 2:
                        boolean newGameActive = in.readInt() == 1;
                        scoreClient = in.readInt();
                        timeLeft = in.readInt();
                        int count = in.readInt();
                        List<CircleInfo> newCircles = new ArrayList<>();

                        for (int i = 0; i < count; i++) {
                            newCircles.add(new CircleInfo(
                                    in.readInt(),
                                    in.readInt(),
                                    in.readInt(),
                                    in.readInt() == 1,
                                    in.readUTF()
                            ));
                        }

                        SwingUtilities.invokeLater(() -> {
                            circles.clear();
                            circles.addAll(newCircles);
                            gameActive = newGameActive;
                            repaint();
                        });
                        break;
                    case 1:
                        SwingUtilities.invokeLater(() -> {
                            gameActive = false;
                            gameStarted = true;

                            Player player = new Player(playerName, scoreClient, selected);
                            try {
                                out.writeInt(10);
                                out.writeUTF(player.getUsername());
                                out.writeInt(player.getScore());
                                out.writeUTF(player.getType());
                                out.flush();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            SwingUtilities.invokeLater(() -> repaint());
                            returnToMenuTimer.start();
                        });
                        break;
                    case 6:
                        showMissingClient = in.readBoolean();
                        currentTime = in.readLong();
                        SwingUtilities.invokeLater(() -> repaint());
                        break;
                    case 7:
                        SwingUtilities.invokeLater(() -> {
                            circles.clear();
                            scoreClient = 0;
                            gameActive = false;
                            gameStarted = false;
                            ready = false;
                            resSet = true;
                            showMissingClient = false;
                            repaint();
                        });
                        break;
                    case 8:
                        int playerCount = in.readInt();
                        topPlayers.clear();
                        for (int i = 0; i < playerCount; i++) {
                            topPlayers.add(new Player(
                                    in.readUTF(),
                                    in.readInt(),
                                    in.readUTF()
                            ));
                        }
                        SwingUtilities.invokeLater(() -> {
                            repaint();
                            if (mainFrame != null) {
                                mainFrame.updateLeaderboard(topPlayers);
                            }
                        });
                        break;
                    default:
                        System.out.println("Неизвестное сообщение: " + message);
                }
            }
        } catch (SocketException | EOFException e) {
            connectionLost();
        } catch (IOException e) {
            e.printStackTrace();
            connectionLost();
        } finally {
            closeConnection();
        }
    }

    private void connectionLost() {
        connectionAlive = false;
        SwingUtilities.invokeLater(() -> {
            gameActive = false;
            gameStarted = true;
            resSet = false;
            repaint();

            JOptionPane.showMessageDialog(this,
                    "Соединение с сервером потеряно",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!connectionAlive) {
            drawableGameScreen.drawConnectionLostScreen(g2, getWidth(), getHeight());
            return;
        }

        if (!gameStarted) {
            drawableGameScreen.drawStartScreen(g2, getWidth(), getHeight(), ready, topPlayers);
            drawableGameScreen.drawLeaderboardBeforeStart(g2, getWidth(), getHeight(), scoreClient, gameStarted, topPlayers);
            return;
        }

        drawableGameScreen.drawFon(g2, getWidth(), getHeight());
        drawableGameScreen.drawLanes(g2, getWidth());

        for (CircleInfo c : circles) {
            if (!c.isActive()) continue;
            String letterStr = String.valueOf(c.getElementName());
            drawableGameScreen.drawCircle(g2, c.getX(), c.getLaneY(), letterStr, Color.BLUE);
        }

        drawableGameScreen.drawScoreAndTime(g2, scoreClient, timeLeft);

        if (!gameActive && gameStarted) {
            drawGameResults(g2);
            drawRestartButton(g2);
        }

        if (showMissingClient && gameActive) {
            if ((System.currentTimeMillis() - currentTime) < TIMEOUT) {
                drawableGameScreen.drawErrorTable(g2, selected, getWidth());
            } else {
                showMissingClient = false;
            }
        }
    }

    private void drawGameResults(Graphics2D g2) {
        drawableGameScreen.drawLeaderboardBeforeStart(g2, getWidth(), getHeight(), scoreClient, gameStarted, topPlayers);
    }

    private void drawRestartButton(Graphics2D g2) {
        drawableGameScreen.drawButton(g2, "ПЕРЕЗАПУСТИТЬ", restartButton, Color.ORANGE);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}