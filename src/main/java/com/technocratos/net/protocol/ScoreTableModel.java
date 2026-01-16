package com.technocratos.net.protocol;

import com.technocratos.model.Player;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ScoreTableModel extends AbstractTableModel {
    private final String[] columnNames = { "Имя", "Счет", "Режим"};
    private List<Player> players;

    public void setPlayers(List<Player> players) {
        this.players = players;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return players.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Player player = players.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> player.getUsername();
            case 1 -> player.getScore();
            case 2 -> player.getType();
            default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
        };
    }
}
