import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.toedter.calendar.JDateChooser;

class Expense {
    private int id;
    private String name;
    private double amount;
    private Date date;

    public Expense(int id, String name, double amount, Date date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }
}

public class ExpenseTrackerGUI extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Expenses";
    private static final String DB_USER = "abdulbasit";
    private static final String DB_PASSWORD = "test123";

    private ArrayList<Expense> expenses = new ArrayList<>();
    private JList<String> expenseList;
    private DefaultListModel<String> listModel;

    public ExpenseTrackerGUI() {
        super("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        listModel = new DefaultListModel<>();
        expenseList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(expenseList);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loadExpenses();
    }

    private void loadExpenses() {
        expenses.clear();
        listModel.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, name, amount, date FROM expenses";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    Date date = rs.getDate("date");
                    Expense expense = new Expense(id, name, amount, date);
                    expenses.add(expense);
                    listModel.addElement(name + " - $" + amount + " (" + date + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading expenses from the database.");
        }
    }

    private void addExpense() {
        String name = JOptionPane.showInputDialog(this, "Enter expense name:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        String amountStr = JOptionPane.showInputDialog(this, "Enter expense amount:");
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.");
            return;
        }

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        int option = JOptionPane.showOptionDialog(
                this,
                new Object[] { "Enter expense date:", dateChooser },
                "Expense Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        );

        if (option == JOptionPane.OK_OPTION) {
            Date date = dateChooser.getDate();

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO expenses (name, amount, date) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, amount);
                    stmt.setDate(3, new java.sql.Date(date.getTime()));
                    stmt.executeUpdate();

                    Expense expense = new Expense(-1, name, amount, date);
                    expenses.add(expense);
                    listModel.addElement(name + " - $" + amount + " (" + date + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding expense to the database.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ExpenseTrackerGUI();
            }
        });
    }
}
