package org.chat.server.core;

import java.sql.*;

public class SqlClient {
    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat-server/clients-db.sqlite");
            connection.setAutoCommit(false);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    synchronized static String getNick(String login, String password) {
        String query = String.format(
                "select nickname from users where login='%s' and password='%s'",
                login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next())
                return set.getString("nickname");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    synchronized static boolean loginIsNotUnique(String login) {
        try (PreparedStatement prepStatement = connection.prepareStatement("SELECT login from users " +
                "where lower(login) like lower(?)")) {
            prepStatement.setString(1, login);
            ResultSet set = prepStatement.executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized static boolean loginMatchesPass(String login, String password) {
        try (PreparedStatement prepStatement = connection.prepareStatement("SELECT login FROM users " +
                "WHERE lower(login) LIKE lower(?) AND password = ?")) {
            prepStatement.setString(1, login);
            prepStatement.setString(2, password);
            ResultSet set = prepStatement.executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized static boolean changePass(String login, String password) {
        try (PreparedStatement prepStatement = connection.prepareStatement("UPDATE users SET password = ? WHERE lower(login) LIKE lower(?)")) {
            prepStatement.setString(1, password);
            prepStatement.setString(2, login);
            int result = prepStatement.executeUpdate();
            if (result == 1) {
                connection.commit();
                return true;
            } else if (result == 0) {
                return false;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized static boolean changeName(String login, String name) {
        try (PreparedStatement prepStatement = connection.prepareStatement("UPDATE users SET nickname = ? WHERE lower(login) LIKE lower(?)")) {
            prepStatement.setString(1, name);
            prepStatement.setString(2, login);
            int result = prepStatement.executeUpdate();
            if (result == 1) {
                connection.commit();
                return true;
            } else if (result == 0) {
                return false;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized static boolean signUp(String login, String password, String nickname) {
        try (PreparedStatement prepStatement = connection.prepareStatement("insert into users (login, password, nickname) values (?, ?, ?)")) {
            prepStatement.setString(1, login);
            prepStatement.setString(2, password);
            prepStatement.setString(3, nickname);
            int result = prepStatement.executeUpdate();
            if (result == 1) {
                connection.commit();
                return true;
            } else if (result == 0){
                return false;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
