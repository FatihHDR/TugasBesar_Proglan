import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.main.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Properties;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * FinancialApp adalah aplikasi GUI untuk mengelola transaksi keuangan.
 * Aplikasi ini memungkinkan pengguna untuk menambah, menghapus, dan mengekspor transaksi ke berkas PDF.
 */
public class FinancialApp {
    JFrame frame;
    JTable table;
    private DefaultTableModel tableModel;
    public ArrayList<Map<String, String>> transactions;
    private JLabel incomeLabel, expenseLabel, balanceLabel;
    public JTextField usernameField;
    public JPasswordField passwordField;
    public JButton signInButton;

    /**
     * Membangun instance FinancialApp dan menginisialisasi komponen GUI.
     */
    public FinancialApp() {
        transactions = new ArrayList<>();
        frame = new JFrame("Pencatatan Keuangan UMKM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        frame.getContentPane().setBackground(new Color(255, 255, 255));

        tableModel = new DefaultTableModel(new String[]{"Pilih", "Tanggal", "Deskripsi", "Kategori", "Jumlah"}, 0);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 220, 220));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Cari");
        JButton addButton = new JButton("Tambah Transaksi");
        JButton deleteButton = new JButton("Hapus Transaksi");
        JButton exportButton = new JButton("Ekspor ke PDF");
        JButton logoutButton = new JButton("Keluar");

        topPanel.add(new JLabel("Cari Transaksi:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(addButton);
        topPanel.add(deleteButton);
        topPanel.add(exportButton);
        topPanel.add(logoutButton);

        frame.add(topPanel, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setBackground(new Color(220, 220, 220));
        incomeLabel = new JLabel("Pemasukan: 0");
        expenseLabel = new JLabel("Pengeluaran: 0");
        balanceLabel = new JLabel("Saldo: 0");
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);
        frame.add(summaryPanel, BorderLayout.SOUTH);

        loadTransactions();
        updateSummary();

        searchButton.addActionListener(e -> filterTransactions(searchField.getText()));
        addButton.addActionListener(e -> openAddTransactionDialog());
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        exportButton.addActionListener(e -> exportToPDF());
        logoutButton.addActionListener(e -> {
            frame.setVisible(false);
            openSignInDialog();
        });

        frame.setVisible(true);
        frame.setEnabled(false);
        openSignInDialog();
    }


    /**
     * Membuka dialog masuk untuk autentikasi pengguna.
     */
    void openSignInDialog() {
        frame.setVisible(false);

        JDialog dialog = new JDialog(frame, "Sign In", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(new Color(255, 255, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton signInButton = new JButton("Sign In");

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        dialog.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel(" Password:"), gbc);

        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel(), gbc);
        gbc.gridx = 1;
        dialog.add(signInButton, gbc);

        signInButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if ("admin".equals(username) && "password".equals(password)) {
                dialog.dispose();
                frame.setVisible(true);
                frame.setEnabled(true);
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username atau password salah!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        dialog.setVisible(true);
    }


    /**
     * Membuka dialog untuk menambahkan transaksi baru.
     */
    private void openAddTransactionDialog() {
        JDialog dialog = new JDialog(frame, "Tambah Transaksi", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(5, 2));

        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField descField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        JTextField amountField = new JTextField();
        JButton saveButton = new JButton("Simpan");

        dialog.add(new JLabel("Tanggal (YYYY-MM-DD):"));
        dialog.add(dateField);
        dialog.add(new JLabel("Deskripsi:"));
        dialog.add(descField);
        dialog.add(new JLabel("Kategori:"));
        dialog.add(categoryBox);
        dialog.add(new JLabel("Jumlah:"));
        dialog.add(amountField);
        dialog.add(new JLabel());
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            try {
                String date = dateField.getText();
                String desc = descField.getText();
                String category = categoryBox.getSelectedItem().toString();
                double amount = Double.parseDouble(amountField.getText());

                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(dialog, "Format tanggal tidak valid! Gunakan YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (date.isEmpty() || desc.isEmpty() || amount <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Semua field harus diisi dengan benar!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    Map<String, String> transaction = new HashMap<>();
                    transaction.put("date", date);
                    transaction.put("desc", desc);
                    transaction.put("category", category);
                    transaction.put("amount", String.valueOf(amount));
                    transactions.add(transaction);
                    saveTransactions();
                    refreshTable();
                    updateSummary();
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Jumlah harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Refresh tabel transaksi untuk menampilkan transaksi saat ini.
     */
    void refreshTable() {
        tableModel.setRowCount(0);
        for (Map<String, String> transaction : transactions) {
            tableModel.addRow(new Object[]{
                    false,
                    transaction.get("date"),
                    transaction.get("desc"),
                    transaction.get("category"),
                    "Rp" + formatCurrency(transaction.get("amount"))
            });
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(220, 220, 220));
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setReorderingAllowed(false);

        table.setRowHeight(30);
    }

    /**
     * Memperbarui label ringkasan untuk pendapatan, pengeluaran, dan saldo.
     */
    public void updateSummary() {
        double totalIncome = 0, totalExpense = 0;
        for (Map<String, String> transaction : transactions) {
            double amount = Double.parseDouble(transaction.get("amount"));
            if (transaction.get("category").equals("Pemasukan")) {
                totalIncome += amount;
            } else {
                totalExpense += amount;
            }
        }
        incomeLabel.setText("Pemasukan: " + "Rp" + formatCurrency(String.valueOf(totalIncome)));
        expenseLabel.setText("Pengeluaran: " + "Rp" + formatCurrency(String.valueOf(totalExpense)));
        balanceLabel.setText("Saldo: " + "Rp" + formatCurrency(String.valueOf(totalIncome - totalExpense)));
    }

    /**
     * Memfilter transaksi yang ditampilkan dalam tabel berdasarkan kata kunci yang diberikan.
     *
     * @param keyword kata kunci untuk memfilter transaksi berdasarkan deskripsi
     */
    public void filterTransactions(String keyword) {
        tableModel.setRowCount(0);
        for (Map<String, String> transaction : transactions) {
            if (transaction.get("desc").toLowerCase().contains(keyword.toLowerCase())) {
                tableModel.addRow(new Object[]{
                        false,
                        transaction.get("date"),
                        transaction.get("desc"),
                        transaction.get("category"),
                        "Rp" + formatCurrency(transaction.get("amount"))
                });
            }
        }
    }

    /**
     * Menyimpan transaksi terkini ke mysql.
     */
    private void saveTransactions() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO transactions (date, description, category, amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Map<String, String> transaction : transactions) {
                    statement.setString(1, transaction.get("date"));
                    statement.setString(2, transaction.get("desc"));
                    statement.setString(3, transaction.get("category"));
                    statement.setDouble(4, Double.parseDouble(transaction.get("amount")));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Memuat transaksi dari file JSON ke dalam aplikasi.
     */
    private void loadTransactions() {
        transactions.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT date, description, category, amount FROM transactions";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> transaction = new HashMap<>();
                    transaction.put("date", resultSet.getString("date"));
                    transaction.put("desc", resultSet.getString("description"));
                    transaction.put("category", resultSet.getString("category"));
                    transaction.put("amount", resultSet.getString("amount"));
                    transactions.add(transaction);
                }
                refreshTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menghapus transaksi yang dipilih dari tabel dan memperbarui data.
     */
    public void deleteSelectedTransactions() {
        List<Integer> rowsToDelete = new ArrayList<>();

        for (int row = 0; row < table.getRowCount(); row++) {
            if (table.getValueAt(row, 0) instanceof Boolean && (Boolean) table.getValueAt(row, 0)) {
                rowsToDelete.add(row);
            }
        }

        for (int i = rowsToDelete.size() - 1; i >= 0; i--) {
            int row = rowsToDelete.get(i);
            transactions.remove(row);
        }

        saveTransactions();

        refreshTable();
        updateSummary();
    }

    /**
     * Memformat jumlah yang diberikan sebagai string mata uang.
     *
     * @param amount jumlah yang akan diformat
     * @return string mata uang yang diformat
     */
    public String formatCurrency(String amount) {
        double number = Double.parseDouble(amount);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        return formatter.format(number);
    }

    /**
     * Mengekspor transaksi terkini ke berkas PDF.
     */
    private void exportToPDF() {
        try {
            PdfWriter writer = new PdfWriter("transactions.pdf");
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Laporan Transaksi Keuangan").setBold().setFontSize(18));
            document.add(new Paragraph(" "));

            Table table = new Table(new float[]{1, 2, 2, 2});

            table.addHeaderCell("Tanggal");
            table.addHeaderCell("Deskripsi");
            table.addHeaderCell("Kategori");
            table.addHeaderCell("Jumlah");

            for (Map<String, String> transaction : transactions) {
                table.addCell(transaction.get("date"));
                table.addCell(transaction.get("desc"));
                table.addCell(transaction.get("category"));
                table.addCell("Rp" + formatCurrency(transaction.get("amount")));
            }

            document.add(table);

            document.close();

            JOptionPane.showMessageDialog(frame, "Data berhasil diekspor ke PDF!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

            String email = JOptionPane.showInputDialog(frame, "Masukkan alamat email untuk mengirim PDF:");
            if (email != null && !email.isEmpty()) {
                sendEmail(email, "Laporan Transaksi Keuangan", "Berikut adalah laporan transaksi keuangan Anda.", "transactions.pdf");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Gagal mengekspor data ke PDF!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sends an email with the specified subject and body, and attaches the specified file.
     *
     * @param to      the recipient's email address
     * @param subject the subject of the email
     * @param body    the body of the email
     * @param filePath the path to the file to attach
     */
    private void sendEmail(String to, String subject, String body, String filePath) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("f34995975@gmail.com", "hcll mvld icgh xazg");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("f34995975@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName("transactions.pdf");
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            JOptionPane.showMessageDialog(frame, "Email berhasil dikirim!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Gagal mengirim email!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getIncomeLabelText() {
        return incomeLabel.getText();
    }

    public String getExpenseLabelText() {
        return expenseLabel.getText();
    }

    public String getBalanceLabelText() {
        return balanceLabel.getText();
    }


    /**
     * Metode utama untuk meluncurkan FinancialApp.
     *
     * @param args argumen baris perintah
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FinancialApp app = new FinancialApp();
        });
    }
}