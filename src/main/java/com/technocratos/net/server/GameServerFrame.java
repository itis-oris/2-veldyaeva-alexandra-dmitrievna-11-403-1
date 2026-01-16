package com.technocratos.net.server;

import com.technocratos.net.protocol.NameDialog;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class GameServerFrame extends JFrame {

    private String serverName;
    private String selected;
    private String[] parameters = {
            "Основания",
            "Кислоты"
    };

    private String[] res;
    public GameServerFrame() throws Exception {
        setTitle("SERVER");
        setSize(800, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ServerSocket serverSocket = null;
        System.out.println("Waiting for client...");
        Socket client = null;
        System.out.println("Client connected");

        try {
            serverSocket = new ServerSocket(50000);
            client = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        GameComponentServer gcs = new GameComponentServer(client);

        res = NameDialog.showNameDialog(this, "Выберите режим", parameters);
        serverName = res[0];
        if (serverName == null) {
            System.out.println("exit");
            System.exit(0);
        }

        selected = res[1];

        setTitle(String.format("SERVER - %s - %s", serverName, selected));

        gcs.setPlayerName(serverName);
        gcs.setSelected(selected);

        add(gcs);
        setVisible(true);

    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameServerFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
//теперь надо создать протокол общения