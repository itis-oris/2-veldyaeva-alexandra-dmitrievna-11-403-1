package com.technocratos.net.client;

import com.technocratos.net.protocol.MainFrame;
import com.technocratos.net.protocol.NameDialog;
import com.technocratos.net.server.GameComponentServer;
import com.technocratos.net.server.GameServerFrame;

import javax.swing.*;
import java.net.Socket;

public class GameClientFrame extends JFrame {

    private String clientName;
    private String[] parameters = {
            "Основания",
            "Кислоты"
    };
    private String[] result;
    private String selectedParameter;

    public GameClientFrame() throws Exception {
        setTitle("CLIENT");
        setSize(800, 700);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Socket socket = new Socket("localhost", 50000);


        GameComponentClient gcc = new GameComponentClient(socket);


        result = NameDialog.showNameDialog(this, "Выберите режим", parameters);
        clientName = result[0];

        if (clientName == null) {
            System.exit(0);
        }
        selectedParameter = result[1];

        MainFrame mainFrame = new MainFrame(socket, clientName, selectedParameter);
        gcc.setMainFrame(mainFrame);

        setTitle(String.format("CLIENT - %s - %s", clientName, selectedParameter));
        gcc.setPlayerName(clientName);
        gcc.setSelected(selectedParameter);

        System.out.println(gcc.getName());


        add(gcc);

        setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameClientFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
//составить обработку выйгрыша и
//отделить кружочки
