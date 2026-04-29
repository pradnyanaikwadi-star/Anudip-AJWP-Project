import java.sql.*;
import java.util.*;
import java.util.function.Predicate;

// Account class (for Collection + Stream)
class Account {
    int accNo;
    String name;
    double balance;

    public Account(int accNo, String name, double balance) {
        this.accNo = accNo;
        this.name = name;
        this.balance = balance;
    }
}

public class BankAccountSystem {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/bankdb";
        String user = "root";
        String pass = "pradnya1903";

        Scanner sc = new Scanner(System.in);

        try {
            // Load driver (optional in new versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to Bank Database Successfully!");

            while (true) {
                System.out.println("\n=== BANK ACCOUNT MANAGEMENT SYSTEM ===");
                System.out.println("1. Open New Account");
                System.out.println("2. View All Accounts");
                System.out.println("3. Deposit Money");
                System.out.println("4. Withdraw Money");
                System.out.println("5. Close Account");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int ch;
                try {
                    ch = sc.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println(" Invalid input! Enter number only.");
                    sc.next();
                    continue;
                }

                switch (ch) {

                    case 1:
                        try {
                            System.out.print("Enter Account Number: ");
                            int accNo = sc.nextInt();
                            sc.nextLine();

                            System.out.print("Enter Account Holder Name: ");
                            String name = sc.nextLine();

                            System.out.print("Enter Initial Balance: ");
                            double balance = sc.nextDouble();

                            // Lambda validation
                            Predicate<Double> validBalance = b -> b >= 0;
                            if (!validBalance.test(balance)) {
                                System.out.println(" Balance cannot be negative!");
                                break;
                            }

                            String insertQuery = "INSERT INTO accounts VALUES (?, ?, ?)";
                            PreparedStatement psInsert = con.prepareStatement(insertQuery);
                            psInsert.setInt(1, accNo);
                            psInsert.setString(2, name);
                            psInsert.setDouble(3, balance);
                            psInsert.executeUpdate();

                            System.out.println("✅ Account created successfully!");
                            psInsert.close();

                        } catch (InputMismatchException e) {
                            System.out.println(" Invalid input! Please enter correct data.");
                            sc.next();
                        }
                        break;

                    case 2:
                        try {
                            String selectQuery = "SELECT * FROM accounts";
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(selectQuery);

                            List<Account> accountList = new ArrayList<>();

                            while (rs.next()) {
                                accountList.add(new Account(
                                        rs.getInt("acc_no"),
                                        rs.getString("name"),
                                        rs.getDouble("balance")
                                ));
                            }

                            System.out.println("\nAcc No\tName\t\tBalance");
                            System.out.println("------------------------------");

                            // Stream + Lambda
                            accountList.stream().forEach(acc ->
                                    System.out.printf("%d\t%-10s\t₹%.2f\n",
                                            acc.accNo, acc.name, acc.balance)
                            );

                            rs.close();
                            stmt.close();

                        } catch (SQLException e) {
                            System.out.println(" Error fetching data!");
                        }
                        break;

                    case 3:
                        try {
                            System.out.print("Enter Account Number: ");
                            int depNo = sc.nextInt();

                            System.out.print("Enter Deposit Amount: ");
                            double depAmt = sc.nextDouble();

                            String depQuery = "UPDATE accounts SET balance = balance + ? WHERE acc_no = ?";
                            PreparedStatement psDep = con.prepareStatement(depQuery);
                            psDep.setDouble(1, depAmt);
                            psDep.setInt(2, depNo);

                            int depRows = psDep.executeUpdate();

                            if (depRows > 0) {
                                System.out.println(" Deposit successful!");
                            } else {
                                System.out.println(" Account not found!");
                            }

                            psDep.close();

                        } catch (InputMismatchException e) {
                            System.out.println(" Invalid input!");
                            sc.next();
                        }
                        break;

                    case 4:
                        try {
                            System.out.print("Enter Account Number: ");
                            int wNo = sc.nextInt();

                            System.out.print("Enter Withdraw Amount: ");
                            double wAmt = sc.nextDouble();

                            String balQuery = "SELECT balance FROM accounts WHERE acc_no=?";
                            PreparedStatement psBal = con.prepareStatement(balQuery);
                            psBal.setInt(1, wNo);
                            ResultSet rsBal = psBal.executeQuery();

                            if (rsBal.next()) {
                                double currBal = rsBal.getDouble("balance");

                                if (currBal >= wAmt) {
                                    String wQuery = "UPDATE accounts SET balance = balance - ? WHERE acc_no = ?";
                                    PreparedStatement psWith = con.prepareStatement(wQuery);
                                    psWith.setDouble(1, wAmt);
                                    psWith.setInt(2, wNo);
                                    psWith.executeUpdate();

                                    System.out.println(" Withdrawal successful! Remaining balance: ₹" + (currBal - wAmt));
                                    psWith.close();
                                } else {
                                    System.out.println("Insufficient balance!");
                                }
                            } else {
                                System.out.println(" Account not found!");
                            }

                            rsBal.close();
                            psBal.close();

                        } catch (InputMismatchException e) {
                            System.out.println(" Invalid input!");
                            sc.next();
                        }
                        break;

                    case 5:
                        try {
                            System.out.print("Enter Account Number to Close: ");
                            int delNo = sc.nextInt();

                            String delQuery = "DELETE FROM accounts WHERE acc_no=?";
                            PreparedStatement psDel = con.prepareStatement(delQuery);
                            psDel.setInt(1, delNo);

                            int delRows = psDel.executeUpdate();

                            if (delRows > 0) {
                                System.out.println(" Account closed successfully!");
                            } else {
                                System.out.println(" Account not found!");
                            }

                            psDel.close();

                        } catch (InputMismatchException e) {
                            System.out.println(" Invalid input!");
                            sc.next();
                        }
                        break;

                    case 6:
                        System.out.println(" Exiting... Thank you for using the Bank System!");
                        con.close();
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println(" Invalid choice! Try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}