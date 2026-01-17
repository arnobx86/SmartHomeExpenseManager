# Smart Home Expense Manager

A user-friendly desktop application built with **JavaFX** and **MySQL** to help families and individuals track their finances efficiently.

## Features
*   **Expense Tracking:** Record daily expenses with categories and dates.
*   **Dashboard:** View total expenses, monthly income, and savings at a glance.
*   **Visual Analysis:** Interactive pie charts and bar charts for spending habits.
*   **Category Management:** Add or delete custom expense categories.
*   **Secure Login:** Built-in authentication system.
*   **Reports:** Export expense data to CSV.

## Prerequisites
*   **Java JDK 21** or higher.
*   **MySQL Server** (running on localhost:3306).
*   **Maven** (for dependency management).

## Setup & Installation

1.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd SmartHomeExpenseManager
    ```

2.  **Configure Database:**
    *   Open `src/main/java/com/arnobx86/smarthomeexpensemanager/DatabaseConnection.java`
    *   Open `src/main/java/com/arnobx86/smarthomeexpensemanager/DatabaseInitializer.java`
    *   Update the `USER` and `PASSWORD` fields with your local MySQL credentials.
    *   *Default is set to `root` / `Cicada3301`.*

3.  **Run the Application:**
    *   The application will automatically create the database (`smart_home_expense_db`) and tables on the first run.
    *   Run via IDE or Maven:
        ```bash
        mvn clean javafx:run
        ```

## Usage

### Login
*   **Username:** `arnob`
*   **Password:** `arnobx86`

### Dashboard
*   **Add Expense:** Enter amount, select category, pick a date, and click "Add Expense".
*   **Set Income:** Enter your monthly income to calculate savings.
*   **Manage Categories:** Click the "Manage" button next to the category dropdown.
*   **Delete Expense:** Right-click on any row in the expense table and select "Delete".
*   **Generate Report:** Click "Generate Report" to save `expense_report.csv` to the project folder.

## Troubleshooting
*   **"No suitable driver found":** Ensure Maven has downloaded dependencies. Try `mvn clean install`.
*   **"Database Error":** Verify MySQL is running and credentials in the code match your server.

## License
This project is open-source and available for personal and educational use.
