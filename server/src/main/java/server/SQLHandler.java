package server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;               // подключение
    private static PreparedStatement psGetNickname;     // запрос на получение ника
    private static PreparedStatement psRegistration;    // запрос на регистрацию
    private static PreparedStatement psChangeNick;      // запрос на смену ника
    private static PreparedStatement psAddMessage;      // запрос на добавление сообщения
    private static PreparedStatement psGetMessageForNick;   // запрос на получение сообщения по нику

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");       // обращаемся к драйверу
            connection = DriverManager.getConnection("jdbc:sqlite:base_3_2.db");        // получаем подключение
            prepareAllStatements();                 // запускаем метод в случае подключения
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        // по логину и пассворду получаем ник
        psGetNickname = connection.prepareStatement("SELECT nickname FROM userData WHERE login = ? AND password = ?;");
        // для регистрации передаем логин, пассворд, ник
        psRegistration = connection.prepareStatement("INSERT INTO userData(login, password, nickname) VALUES (?, ?, ?)");
        // смена ника
        psChangeNick = connection.prepareStatement("UPDATE userData SET nickname = ? WHERE nickname = ?;");
        // добавление сообщения
        psAddMessage = connection.prepareStatement("INSERT INTO messageData (sender, receiver, text, date) VALUES (\n" +
                "(SELECT id FROM userData WHERE nickname = ?), \n" +
                "(SELECT id FROM userData WHERE nickname = ?), \n" +
                "?, ?)"
        );
        // получаем ник
        psGetMessageForNick = connection.prepareStatement("SELECT (SELECT nickname FROM userData WHERE id = sender), \n" +
                "       (SELECT nickname FROM userData WHERE id = receiver), \n" +
                "       text, \n" +
                "       date \n" +
                "FROM messageData \n" +
                "WHERE sender = (SELECT id FROM userData WHERE nickname = ?) \n" +
                "OR receiver = (SELECT id FROM userData WHERE nickname = ?) \n" +
                "OR receiver = (SELECT id FROM userData WHERE nickname = 'all')"

        );
    }

    // метод для получения ника
    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet resultSet = psGetNickname.executeQuery();
            if (resultSet.next()) {
                nick = resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return nick;
    }

    // метод регистрации
    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    // метод смены ника
    public static boolean changeNick(String oldNickname, String newNickname) {
        try {
            psChangeNick.setString(1, newNickname);
            psChangeNick.setString(2, oldNickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }

    // метод внесения сообщения в базу данных (messageData)
    public static boolean addMessage(String sender, String receiver, String text, String date) {
        try {
            psAddMessage.setString(1, sender);
            psAddMessage.setString(2, receiver);
            psAddMessage.setString(3, text);
            psAddMessage.setString(4, date);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // метод получения сообщений из базы данных (messageData)
    public static String getMessageForNick(String nick) {
        StringBuilder sb = new StringBuilder();
        try {
            psGetMessageForNick.setString(1, nick);
            psGetMessageForNick.setString(2, nick);
            ResultSet rs = psGetMessageForNick.executeQuery();

            while (rs.next()) {
                String sender = rs.getString(1);
                String receiver = rs.getString(2);
                String text = rs.getString(3);
                String date = rs.getString(4);
                // сообщение для всех
                if (receiver.equals("all")) {
                    sb.append((String.format("%s : %s\n", sender, text)));
                } else {
                    // приватное сообщение
                    sb.append(String.format("[%s] private [%s]: %s", sender, receiver, text));
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sb.toString();
    }

    public static void disconnect() {
        try {
            psRegistration.close();
            psGetNickname.close();
            psChangeNick.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
