package com.technocratos.net.server;

import com.technocratos.PlayerDAO;
import com.technocratos.model.Circle;
import com.technocratos.model.Player;
import com.technocratos.net.protocol.DrawableGameScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameComponentServer extends JComponent {
    private Boolean running = true;
    private boolean showMissServer = false;
    private final Rectangle restartButton = new Rectangle(300, 500, 200, 60);
    private boolean showMissClient = false;
    private final DrawableGameScreen drawableGameScreen = new DrawableGameScreen();

    private static final Set<String> ACIDS = Set.of(
            "HCl", "H2SO4", "HNO3", "H3PO4", "H2S", "HF", "HBr", "HI", "H2CO3", "H2O2"
    );
    private static final Set<String> BASES = Set.of(
            "NaOH", "KOH", "Ca(OH)2", "Mg(OH)2", "NH3"
    );

    private static final long TIME = 2000;
    private long currentTime;
    private String playerName;
    private String selected;
    private PlayerDAO playerDAO = new PlayerDAO();
    static final int CIRCLE_RADIUS = 40;
    static final int GAME_TIME = 20;
    static final int LANE_STEP = 180;

    private final List<String> elements = readElements();
    private final Random random = new Random();
    private final List<Circle> circles = new ArrayList<>();
    private int serverScore = 0;
    private int clientScore = 0;
    private boolean gameActive = false;
    private boolean gameStarted = false;
    private boolean clientReady = false;
    private long startTime;
    private boolean resSet = true;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private boolean restarting = false;
    private Timer missMessageTimer;
    private Timer gameTimer;
    private Timer spawnTimer;

    public GameComponentServer(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        setPreferredSize(new Dimension(900, 650));
        setupMouseListeners();
        setupTimers();

        new Thread(this::messageProcessingLoop).start();
        sendLeaderboardData();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameActive) {
                    handleClick(e.getX(), e.getY(), true);
                } else if (!gameStarted && clientReady) {
                    startGame();
                } else if ((!gameActive && gameStarted && restartButton.contains(e.getPoint())) || restarting) {
                    restartGame();
                }
            }
        });
    }

    private void setupTimers() {
        spawnTimer = new Timer(800, e -> spawnNewCircle());
        spawnTimer.start();
        gameTimer = new Timer(30, e -> gameUpdate());
        gameTimer.start();
        missMessageTimer = new Timer(2000, e -> {
            showMissServer = false;
            showMissClient = false;
            repaint();
        });
        missMessageTimer.setRepeats(false);
    }

    private void messageProcessingLoop() {
        try {
            while (running && socket.isConnected() && !socket.isClosed()) {
                if (in.available() > 0) {
                    int messageType = in.readInt();
                    processClientMessage(messageType);
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка в потоке обработки сообщений сервера: " + e.getMessage());
        } finally {
            stopGame();
        }
    }

    private void processClientMessage(int messageType) throws IOException {
        switch (messageType) {
            case 0:
                int clientX = in.readInt();
                int clientY = in.readInt();
                SwingUtilities.invokeLater(() -> handleClick(clientX, clientY, false));
                break;
            case 3:
                in.readUTF();
                SwingUtilities.invokeLater(() -> {
                    clientReady = true;
                    repaint();
                });
                break;
            case 7:
                SwingUtilities.invokeLater(() -> {
                    restarting = true;
                    restartGame();
                });
                break;
            case 9:
                sendLeaderboardData();
                break;
            default:
                System.out.println("Неизвестный тип сообщения от клиента: " + messageType);
        }
    }

    private void sendLeaderboardData() {
        try {
            List<Player> players = playerDAO.getAllPlayers();
            out.writeInt(8);
            out.writeInt(players.size());
            for (Player player : players) {
                out.writeUTF(player.getUsername());
                out.writeInt(player.getScore());
                out.writeUTF(player.getType());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readElements() {
        List<String> elements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("elements.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    elements.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elements;
    }

    private void startGame() {
        gameStarted = true;
        gameActive = true;
        resSet = true;
        startTime = System.currentTimeMillis();
        spawnTimer.start();

        try {
            out.writeInt(4);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            stopGame();
        }
    }

    private void gameUpdate() {
        if (!gameActive) {
            repaint();
            return;
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsed >= GAME_TIME) {
            gameActive = false;
            sendGameOver();
            spawnTimer.stop();
        }

        for (Circle c : circles) {
            c.move();
        }
        circles.removeIf(c -> c.getX() > getWidth() + 700);
        sendState(GAME_TIME - (int) elapsed);
        repaint();
    }

    private void spawnNewCircle() {
        if (!gameActive || elements.isEmpty()) return;

        int lane = random.nextInt(3);
        int speed = 5;
        String element = elements.get(random.nextInt(elements.size()));

        int attempts = 0;
        while (attempts < 20) {
            int startX = -CIRCLE_RADIUS * 2 - 20 - random.nextInt(100);

            boolean positionFree = true;
            for (Circle c : circles) {
                if (c.getLane() == lane && Math.abs(c.getX() - startX) < 150) {
                    positionFree = false;
                    break;
                }
            }

            if (positionFree) {
                circles.add(new Circle(lane, startX, speed, element));
                return;
            }
            attempts++;
        }
        circles.add(new Circle(lane, -1000, speed, element));
    }

    private void handleClick(int x, int y, boolean server) {
        if (missMessageTimer != null && missMessageTimer.isRunning()) {
            missMessageTimer.stop();
        }

        for (int i = circles.size() - 1; i >= 0; i--) {
            Circle c = circles.get(i);
            if (c.contains(x, y)) {
                switch (selected) {
                    case "Основания":
                        if (isBase(c.getElement())) {
                            if (server) {
                                serverScore++;
                            } else {
                                clientScore++;
                            }
                        } else {
                            missClick(server);
                        }
                        break;
                    case "Кислоты":
                        if (isAcid(c.getElement())) {
                            if (server) {
                                serverScore++;
                            } else {
                                clientScore++;
                            }
                        } else {
                            missClick(server);
                        }
                        break;
                }
                circles.remove(i);
                break;
            }
        }
    }

    private void missClick(boolean server) {
        if (server) {
            serverScore = serverScore - 3;
            showMissServer = true;
            currentTime = System.currentTimeMillis();
        } else {
            clientScore = clientScore - 3;
            showMissClient = true;
            currentTime = System.currentTimeMillis();
            sendMissClick(currentTime);
        }
    }

    private void sendState(int timeLeft) {
        try {
            out.writeInt(2);
            out.writeInt(gameActive ? 1 : 0);
            out.writeInt(clientScore);
            out.writeInt(timeLeft);
            out.writeInt(circles.size());

            for (Circle c : circles) {
                out.writeInt(c.getLane());
                out.writeInt(c.getX());
                out.writeInt(c.getSpeed());
                out.writeInt(1);
                out.writeUTF(c.getElement());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            stopGame();
        }
    }

    private void sendGameOver() {
        try {
            out.writeInt(1);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMissClick(long currentTime) {
        try {
            out.writeInt(6);
            out.writeBoolean(showMissClient);
            out.writeLong(currentTime);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            stopGame();
        }
    }

    private void stopGame() {
        running = false;
        if (gameTimer != null) gameTimer.stop();
        if (spawnTimer != null) spawnTimer.stop();
        if (missMessageTimer != null) missMessageTimer.stop();
        gameActive = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameStarted) {
            drawableGameScreen.drawWaitScreen(g2, getWidth(), getHeight(), clientReady);
            drawableGameScreen.drawLeaderboardBeforeStart(g2, getWidth(), getHeight(), serverScore, gameStarted);
            return;
        }

        g2.setColor(new Color(240, 240, 240));
        g2.fillRect(0, 0, getWidth(), getHeight());
        drawableGameScreen.drawLanes(g2, getWidth());

        for (Circle c : circles) {
            drawableGameScreen.drawCircle(g2, c.getX(), c.getLaneY(), c.getElement(), new Color(255, 100, 100));
        }

        if (gameActive && startTime > 0) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            int timeLeft = GAME_TIME - (int) elapsed;
            drawableGameScreen.drawScoreAndTime(g2, serverScore, timeLeft);
        }

        if (!gameActive && gameStarted) {
            drawGameResults(g2);

            if (resSet) {
                playerDAO.addPlayer(new Player(playerName, serverScore, selected));
                resSet = false;
            }
            drawRestartButton(g2);
        }

        if (showMissServer && gameActive) {
            if (System.currentTimeMillis() - currentTime < TIME) {
                drawableGameScreen.drawErrorTable(g2, selected, getWidth());
            } else {
                showMissServer = false;
            }
        }
    }

    private void restartGame() {
        circles.clear();
        serverScore = 0;
        clientScore = 0;
        gameActive = false;
        gameStarted = false;
        resSet = true;
        showMissServer = false;
        showMissClient = false;
        restarting = false;
        spawnTimer.stop();

        try {
            out.writeInt(7);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            stopGame();
        }
        repaint();
    }

    private void drawGameResults(Graphics2D g2) {
        drawableGameScreen.drawLeaderboardBeforeStart(g2, getWidth(), getHeight(), serverScore, gameStarted);
    }

    private void drawRestartButton(Graphics2D g2) {
        drawableGameScreen.drawButton(g2, "ПЕРЕЗАПУСТИТЬ", restartButton, Color.ORANGE);
    }

    private boolean isBase(String formula) {
        return ACIDS.contains(formula);
    }

    private boolean isAcid(String formula) {
        return BASES.contains(formula);
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
}