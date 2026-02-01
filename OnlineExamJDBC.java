import java.sql.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class OnlineExamJDBC {

    static final String URL = "jdbc:mysql://localhost:3306/online_exam";
    static final String USER = "root";
    static final String PASS = "om@123"; // change password

    static boolean submitted = false;
    static int score = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            // LOGIN
            System.out.print("Username: ");// admin
            String username = sc.next();
            System.out.print("Password: ");//1234
            String password = sc.next();

            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Invalid Login!");
                return;
            }

            int choice;
            do {
                System.out.println("\n1. Update Profile");
                System.out.println("2. Change Password");
                System.out.println("3. Start Exam");
                System.out.println("4. Logout");
                System.out.print("Enter choice: ");
                choice = sc.nextInt();

                switch (choice) {
                    case 1 -> updateProfile(con, sc, username);
                    case 2 -> updatePassword(con, sc, username);
                    case 3 -> startExam(con, sc);
                    case 4 -> System.out.println("Logged out successfully!");
                    default -> System.out.println("Invalid choice!");
                }
            } while (choice != 4);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void updateProfile(Connection con, Scanner sc, String username) throws SQLException {
        System.out.print("Enter new name: ");
        String name = sc.next();

        PreparedStatement ps = con.prepareStatement(
            "UPDATE users SET name=? WHERE username=?");
        ps.setString(1, name);
        ps.setString(2, username);
        ps.executeUpdate();

        System.out.println("✅ Profile updated!");
    }

    static void updatePassword(Connection con, Scanner sc, String username) throws SQLException {
        System.out.print("Enter new password: ");
        String pass = sc.next();

        PreparedStatement ps = con.prepareStatement(
            "UPDATE users SET password=? WHERE username=?");
        ps.setString(1, pass);
        ps.setString(2, username);
        ps.executeUpdate();

        System.out.println("✅ Password updated!");
    }

    static void startExam(Connection con, Scanner sc) throws SQLException {
        score = 0;
        submitted = false;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (!submitted) {
                    System.out.println("\n⏰ Time Over! Auto Submit");
                    submitExam();
                }
            }
        }, 30000); // 30 seconds

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM questions");

        while (rs.next() && !submitted) {
            System.out.println("\n" + rs.getString("question"));
            System.out.println("A) " + rs.getString("optionA"));
            System.out.println("B) " + rs.getString("optionB"));
            System.out.println("C) " + rs.getString("optionC"));
            System.out.println("D) " + rs.getString("optionD"));

            System.out.print("Answer: ");
            String ans = sc.next().toUpperCase();

            if (ans.charAt(0) == rs.getString("correct").charAt(0)) {
                score++;
            }
        }

        submitted = true;
        timer.cancel();
        submitExam();
    }

    static void submitExam() {
        System.out.println("\n✅ Exam Submitted!");
        System.out.println("Score: " + score);
    }
}
