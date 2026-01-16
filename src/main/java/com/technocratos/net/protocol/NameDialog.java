package com.technocratos.net.protocol;

import javax.swing.*;
import java.awt.*;

public class NameDialog extends JDialog {
    private String playerName;
    private JTextField nameField;
    private Boolean flag;
    private JComboBox<String> comboBox;
    private String selected;


    public NameDialog(JFrame parent, String title, String[] parameters) {
        super(parent, title, true);

        flag = false;

        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        //mainPanel.setLayout(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Ваше имя:");
        nameField = new JTextField(15);
        mainPanel.add(label);
        mainPanel.add(nameField);

        JLabel panel = new JLabel(
                "Выберете параметр:"
        );
        comboBox = new JComboBox<>(parameters);
        selected = parameters[0];

        comboBox.addActionListener(e -> {
            selected = (String) comboBox.getSelectedItem();
        });

        mainPanel.add(panel);
        mainPanel.add(comboBox);

        add(mainPanel, BorderLayout.CENTER);

        JPanel button = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        10,
                        10
                )
        );

        JButton okButton = new JButton("Готово!");
        JButton cancelButton = new JButton("Отмена");

        okButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            selected = comboBox.getSelectedItem().toString();
            if (!playerName.isEmpty()) {
                flag = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Введите имя!",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        cancelButton.addActionListener(e -> {
            flag = false;
            dispose();
        });

        button.add(okButton);
        button.add(cancelButton);

        //add(panel, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);


        getRootPane().setDefaultButton(okButton);
        selected = (String) comboBox.getSelectedItem();

    }

    public static String[] showNameDialog(JFrame parent, String title, String[] parameters) {
        NameDialog dialog = new NameDialog(parent, title, parameters);
        dialog.setVisible(true);
        if (dialog.getFlag()) {
            String[] res = new String[2];
            res[0] = dialog.playerName;
            res[1] = dialog.selected;
            return res;
        }
        return null;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public void setNameField(JTextField nameField) {
        this.nameField = nameField;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
