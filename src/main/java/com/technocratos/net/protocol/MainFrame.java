package com.technocratos.net.protocol;

import com.technocratos.model.Player;
import com.technocratos.net.client.GameComponentClient;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final CardLayout layout = new CardLayout();
    private final JPanel contentPanel = new JPanel(layout);
    private final ScoreTableModel scoreTableModel = new ScoreTableModel();
    private final List<Player> players = new ArrayList<>();

    private Socket socket;
    private String playerName;
    private String selectedMode;

    public MainFrame(Socket socket, String playerName, String selectedMode) throws Exception {
        this.socket = socket;
        this.playerName = playerName;
        this.selectedMode = selectedMode;

        setTitle(String.format("Клиент - %s - %s", playerName, selectedMode));
        initUI();
    }

    private void initUI() {
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        contentPanel.add(createMenuPanel(), "МЕНЮ");
        JPanel gamePlaceholder = new JPanel();
        gamePlaceholder.setBackground(Color.LIGHT_GRAY);
        contentPanel.add(gamePlaceholder, "ИГРА");

        add(contentPanel);
        showMenu();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Таблица рекордов", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(scoreTableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 18));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        scoreTableModel.setPlayers(players);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton playButton = new JButton("Играть");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(200, 50));
        playButton.addActionListener(e -> startGame());
        buttonPanel.add(playButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startGame() {
        try {
            GameComponentClient game = new GameComponentClient(socket);
            game.setPlayerName(playerName);
            game.setSelected(selectedMode);
            game.setMainFrame(this);

            Component[] components = contentPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof GameComponentClient) {
                    contentPanel.remove(comp);
                    break;
                }
            }

            contentPanel.add(game, "ИГРА");
            showGame();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка запуска игры: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showMenu() {
        layout.show(contentPanel, "МЕНЮ");
    }

    public void showGame() {
        layout.show(contentPanel, "ИГРА");
    }

    public void returnToMenu() {
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof GameComponentClient) {
                contentPanel.remove(comp);
                break;
            }
        }
        showMenu();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void updateLeaderboard(List<Player> newPlayers) {
        this.players.clear();
        this.players.addAll(newPlayers);
        scoreTableModel.setPlayers(players);
        repaint();
    }
}