package com.arnobx86.smarthomeexpensemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label totalExpenseLabel;
    @FXML private Label monthlyIncomeLabel;
    @FXML private Label savingsLabel;
    
    @FXML private TextField expenseAmountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker expenseDatePicker;
    @FXML private TextField incomeField;
    
    @FXML private DatePicker filterDatePicker;
    
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, Integer> colId;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, Double> colAmount;
    @FXML private TableColumn<Expense, LocalDate> colDate;

    @FXML private PieChart expensePieChart;
    @FXML private BarChart<String, Number> expenseBarChart;
    @FXML private BarChart<String, Number> monthlyExpenseBarChart;

    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private ObservableList<String> categories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCategories();
        categoryComboBox.setItems(categories);
        expenseDatePicker.setValue(LocalDate.now());
        
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        expenseTable.setItems(expenseList);
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Expense selectedExpense = expenseTable.getSelectionModel().getSelectedItem();
            if (selectedExpense != null) {
                deleteExpense(selectedExpense);
            }
        });
        contextMenu.getItems().add(deleteItem);
        expenseTable.setContextMenu(contextMenu);
        
        loadExpenses();
        updateDashboard();
        updateMonthlyComparisonChart();
    }

    private void loadCategories() {
        categories.clear();
        String query = "SELECT name FROM categories";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onManageCategoriesClick() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Add", "Add", "Delete");
        dialog.setTitle("Manage Categories");
        dialog.setHeaderText("Choose an action");
        dialog.setContentText("Action:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(action -> {
            if (action.equals("Add")) {
                TextInputDialog addDialog = new TextInputDialog();
                addDialog.setTitle("Add Category");
                addDialog.setHeaderText("Add a new expense category");
                addDialog.setContentText("Category Name:");
                addDialog.showAndWait().ifPresent(name -> {
                    if (!name.trim().isEmpty()) addCategory(name.trim());
                });
            } else if (action.equals("Delete")) {
                ChoiceDialog<String> deleteDialog = new ChoiceDialog<>(categories.isEmpty() ? "" : categories.get(0), categories);
                deleteDialog.setTitle("Delete Category");
                deleteDialog.setHeaderText("Select a category to delete");
                deleteDialog.setContentText("Category:");
                deleteDialog.showAndWait().ifPresent(this::deleteCategory);
            }
        });
    }

    private void addCategory(String name) {
        String query = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            loadCategories();
        } catch (SQLException e) {
            showAlert("Error", "Could not add category. It might already exist.");
        }
    }
    
    private void deleteCategory(String name) {
        String query = "DELETE FROM categories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            loadCategories();
        } catch (SQLException e) {
            showAlert("Error", "Could not delete category.");
        }
    }

    @FXML
    protected void onAddExpenseClick() {
        try {
            String amountText = expenseAmountField.getText();
            if (amountText.isEmpty()) {
                showAlert("Input Error", "Please enter an amount.");
                return;
            }
            double amount = Double.parseDouble(amountText);
            String category = categoryComboBox.getValue();
            LocalDate date = expenseDatePicker.getValue();
            
            if (category == null || date == null) {
                showAlert("Input Error", "Please select category and date.");
                return;
            }

            String query = "INSERT INTO expenses (category, amount, date) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, category);
                pstmt.setDouble(2, amount);
                pstmt.setDate(3, Date.valueOf(date));
                pstmt.executeUpdate();
                
                loadExpenses();
                updateDashboard();
                updateMonthlyComparisonChart();
                clearFields();
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid amount.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not add expense.");
        }
    }
    
    private void deleteExpense(Expense expense) {
        String query = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, expense.getId());
            pstmt.executeUpdate();
            loadExpenses();
            updateDashboard();
            updateMonthlyComparisonChart();
        } catch (SQLException e) {
            showAlert("Error", "Could not delete expense.");
        }
    }
    
    @FXML
    protected void onSetIncomeClick() {
        try {
            String incomeText = incomeField.getText();
            if (incomeText.isEmpty()) return;
            double income = Double.parseDouble(incomeText);
            monthlyIncomeLabel.setText(String.format("%.2f", income));
            updateSavings();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid income amount.");
        }
    }

    @FXML
    protected void onGenerateReportClick() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("expense_report.csv"))) {
            writer.write("ID,Category,Amount,Date\n");
            for (Expense expense : expenseList) {
                writer.write(String.format("%d,%s,%.2f,%s\n", 
                        expense.getId(), expense.getCategory(), expense.getAmount(), expense.getDate()));
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Generated");
            alert.setHeaderText(null);
            alert.setContentText("Expense report saved to expense_report.csv");
            alert.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Could not generate report.");
        }
    }
    
    @FXML
    protected void onFilterDateChange() {
        loadExpenses();
        updateDashboard();
    }
    
    @FXML
    protected void onClearFilterClick() {
        filterDatePicker.setValue(null);
        loadExpenses();
        updateDashboard();
    }

    private void loadExpenses() {
        expenseList.clear();
        String query = "SELECT * FROM expenses";
        LocalDate filterDate = filterDatePicker.getValue();
        
        if (filterDate != null) {
            query += " WHERE MONTH(date) = ? AND YEAR(date) = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if (filterDate != null) {
                pstmt.setInt(1, filterDate.getMonthValue());
                pstmt.setInt(2, filterDate.getYear());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expenseList.add(new Expense(
                            rs.getInt("id"),
                            rs.getString("category"),
                            rs.getDouble("amount"),
                            rs.getDate("date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboard() {
        double totalExpense = expenseList.stream().mapToDouble(Expense::getAmount).sum();
        totalExpenseLabel.setText(String.format("%.2f", totalExpense));
        updateSavings();
        updateCharts();
    }
    
    private void updateSavings() {
        try {
            double income = Double.parseDouble(monthlyIncomeLabel.getText());
            double expense = Double.parseDouble(totalExpenseLabel.getText());
            savingsLabel.setText(String.format("%.2f", income - expense));
        } catch (NumberFormatException e) {
            savingsLabel.setText("0.00");
        }
    }

    private void updateCharts() {
        Map<String, Double> categoryTotals = expenseList.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        expensePieChart.setData(pieChartData);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses by Category");
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        expenseBarChart.getData().clear();
        expenseBarChart.getData().add(series);
    }
    
    private void updateMonthlyComparisonChart() {
        String query = "SELECT MONTH(date) as month, SUM(amount) as total FROM expenses WHERE YEAR(date) = ? GROUP BY MONTH(date)";
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Expenses");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, LocalDate.now().getYear());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int monthVal = rs.getInt("month");
                    double total = rs.getDouble("total");
                    String monthName = Month.of(monthVal).name();
                    series.getData().add(new XYChart.Data<>(monthName, total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        monthlyExpenseBarChart.getData().clear();
        monthlyExpenseBarChart.getData().add(series);
    }

    private void clearFields() {
        expenseAmountField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        expenseDatePicker.setValue(LocalDate.now());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
