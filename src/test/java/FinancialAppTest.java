import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class FinancialAppTest {
    private FinancialApp app;

    @BeforeEach
    public void setUp() {
        // Initialize the FinancialApp instance on the EDT
        try {
            SwingUtilities.invokeAndWait(() -> {
                app = new FinancialApp();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginSuccess() {
        // Simulate user input for login
        SwingUtilities.invokeLater(() -> {
            app.openSignInDialog(); // Open the login dialog
            // Simulate entering username and password
            app.usernameField.setText("admin");
            app.passwordField.setText("password");
            app.signInButton.doClick(); // Simulate button click
        });

        // Wait for the dialog to close
        try {
            Thread.sleep(100); // Wait a bit for the dialog to process
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the main frame is visible after successful login
        assertTrue(app.frame.isVisible(), "Main frame should be visible after successful login.");
    }

    @Test
    public void testFormatCurrency() {
        String formatted = app.formatCurrency("1234567.89");
        assertEquals("1,234,568", formatted);
    }

    @Test
    public void testUpdateSummary() {
        // Add some transactions
        SwingUtilities.invokeLater(() -> {
            app.transactions.add(createTransaction("2023-10-01", "Salary", "Pemasukan", "5000"));
            app.transactions.add(createTransaction("2023-10-02", "Groceries", "Pengeluaran", "1500"));
            app.updateSummary();
        });

        // Wait for the summary to update
        try {
            Thread.sleep(100); // Wait a bit for the summary to process
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Pemasukan: Rp5,000", app.getIncomeLabelText());
        assertEquals("Pengeluaran: Rp1,500", app.getExpenseLabelText());
        assertEquals("Saldo: Rp3,500", app.getBalanceLabelText());
    }

    @Test
    public void testFilterTransactions() {
        SwingUtilities.invokeLater(() -> {
            app.transactions.add(createTransaction("2023-10-01", "Salary", "Pemasukan", "5000"));
            app.transactions.add(createTransaction("2023-10-02", "Groceries", "Pengeluaran", "1500"));
            app.refreshTable();
            app.filterTransactions("Salary");
        });

        try {
            Thread.sleep(100); // Wait a bit for the filter to process
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = (DefaultTableModel) app.table.getModel();
        assertEquals(1, model.getRowCount());
        assertEquals("Salary", model.getValueAt(0, 2)); // Check description
    }

    @Test
    public void testDeleteSelectedTransactions() {
        // Add some transactions
        SwingUtilities.invokeLater(() -> {
            app.transactions.add(createTransaction("2023-10-01", "Salary", "Pemasukan", "5000"));
            app.transactions.add(createTransaction("2023-10-02", "Groceries", "Pengeluaran", "1500"));
            app.refreshTable();

            app.table.setValueAt(true, 0, 0);
            app.deleteSelectedTransactions();
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = (DefaultTableModel) app.table.getModel();
        assertEquals(1, model.getRowCount());
        assertEquals("Groceries", model.getValueAt(0, 2)); // Ensure the remaining transaction is the correct one
    }

    private Map<String, String> createTransaction(String date, String description , String category, String amount) {
        Map<String, String> transaction = new HashMap<>();
        transaction.put("date", date);
        transaction.put("description", description);
        transaction.put("category", category);
        transaction.put("amount", amount);
        return transaction;
    }
}