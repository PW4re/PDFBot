import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DataBaseManager {
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;

    public static void conn() throws ClassNotFoundException, SQLException
    {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:TEST1.s3db");

        System.out.println("База Подключена!");
    }

    public static void createDB() throws ClassNotFoundException, SQLException
    {
        statement = connection.createStatement();
        statement.execute(String.format("CREATE TABLE if not exists 'users' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text, 'phone' INT);"));

        System.out.println("Таблица создана или уже существует.");
    }

    public static void writeDB() throws SQLException
    {
        statement.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Petya', 125453); ");
        statement.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Vasya', 321789); ");
        statement.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Masha', 456123); ");

        System.out.println("Таблица заполнена");
    }

    public static void readDB() throws ClassNotFoundException, SQLException
    {
        resultSet = statement.executeQuery("SELECT * FROM users");

        while(resultSet.next())
        {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String phone = resultSet.getString("phone");
            System.out.println( "ID = " + id );
            System.out.println( "name = " + name );
            System.out.println( "phone = " + phone );
            System.out.println();
        }

        System.out.println("Таблица выведена");
    }

    public static void closeDB() throws ClassNotFoundException, SQLException
    {
        connection.close();
        statement.close();
        resultSet.close();

        System.out.println("Соединения закрыты");
    }

}