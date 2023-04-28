package checkers.util;

import checkers.model.GameRecord;
import checkers.model.User;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckersSQLUtil {
    private static final String dbName = "checkers.db";
    private static final String dbURL = "jdbc:sqlite:" + dbName;

    private static CheckersSQLUtil checkersSQLUtilSingle  = null;

    //Apply singleton pattern
    public static CheckersSQLUtil getInstance(){
        if(checkersSQLUtilSingle == null){
            //Prevent threads concurrency
            synchronized (CheckersSQLUtil.class){
                if(checkersSQLUtilSingle == null){
                    checkersSQLUtilSingle = new CheckersSQLUtil();
                }
            }
        }
        return checkersSQLUtilSingle;
    }

    //Basic constructor to be used with the singleton pattern
    private CheckersSQLUtil(){
        createDB();
        setupDB();
    }

    //Create database if not exist
    public static void createDB() {
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            System.out.println("Database already created");
            return;
        }
        try (Connection ignored = DriverManager.getConnection(dbURL)) {
            System.out.println("A new database has been created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Remove the existing database
    public static void removeDB() {
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            boolean result = dbFile.delete();
            if (!result) {
                System.out.println("Couldn't delete existing db file");
                System.exit(-1);
            } else {
                System.out.println("Removed existing DB file.");
            }
        } else {
            System.out.println("No existing DB file.");
        }
    }

    //Create tables to set up the database
    public static void setupDB() {
        String createUserTableSQL =
                """
                CREATE TABLE IF NOT EXISTS users (
                    id integer PRIMARY KEY AUTOINCREMENT,
                    user_name text NOT NULL UNIQUE
                );
                """;

        String createGameTableSQL =
                """
                CREATE TABLE IF NOT EXISTS games (
                    game_id integer PRIMARY KEY AUTOINCREMENT,
                    user_id integer NOT NULL,
                    game_name text NOT NULL,
                    game_serialization text NOT NULL,
                    FOREIGN KEY (user_id)
                        REFERENCES users (id) 
                            ON DELETE CASCADE 
                );
                """;


        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement statement = conn.createStatement()) {
            statement.execute(createUserTableSQL);
            statement.execute(createGameTableSQL);

            System.out.println("Created tables");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Query a user by name from the users table
    public User queryUserByName(String name){
        User user = null;
        String userSQL =
                """
                    SELECT id
                    FROM users
                    WHERE user_name = ?;
                """;
        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(userSQL)) {
            statement.setString(1, name);
            ResultSet resultSets = statement.executeQuery();
            while (resultSets.next()){
                user = new User(resultSets.getInt("id"), name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    //Add a user to the users table
    public void addUserByName(String name){
        String addUserSQL =
               """
                INSERT INTO users(user_name) VALUES (?);
                """;

        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(addUserSQL)){
            statement.setString(1,name);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Add a game record to the games table
    public void addGame(String gameName,int userId,String serialization){
        String addGameSQL =
               """
                INSERT INTO games(user_id,game_name,game_serialization ) VALUES (?,?,?);
               """;

        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(addGameSQL)){
            statement.setInt(1,userId);
            statement.setString(2,gameName);
            statement.setString(3,serialization);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Update a game record in the games table
    public void updateGame(String gameName,int userId,String serialization){
        String addGameSQL =
                """
                 UPDATE games
                 SET game_serialization = ?
                 WHERE game_name = ? AND user_id = ?;
                """;

        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(addGameSQL)){
            statement.setInt(3,userId);
            statement.setString(2,gameName);
            statement.setString(1,serialization);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Query a game record by game name and username
    public GameRecord queryGameRecord(String gameName,String userName){
        GameRecord gameRecord = null;
        String gameQuerySQL =
                """
                    SELECT g.game_serialization, g.game_id, g.user_id
                    FROM games g NATURAL JOIN users u
                    WHERE u.user_name = ? AND g.game_name = ?;
                """;

        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(gameQuerySQL)) {
            statement.setString(1, userName);
            statement.setString(2, gameName);
            ResultSet resultSets = statement.executeQuery();
            while (resultSets.next()){
                String serialization = resultSets.getString("game_serialization");
                int gameId = resultSets.getInt("game_id");
                int userId = resultSets.getInt("user_id");
                gameRecord = new GameRecord(gameId,userId, gameName, serialization);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return gameRecord;
    }

    //Query game records by user
    public List<GameRecord> queryGamesByUser(int userId){
        List<GameRecord> gameRecords = new ArrayList<>();

        String queryGamesSQL =
                """
                    SELECT game_name, game_serialization, game_id
                    FROM games
                    WHERE user_id = ?;
                """;
        try(Connection connection = DriverManager.getConnection(dbURL);
            PreparedStatement statement =connection.prepareStatement(queryGamesSQL)) {
            statement.setInt(1, userId);
            ResultSet resultSets = statement.executeQuery();
            while (resultSets.next()){
                String serialization = resultSets.getString("game_serialization");
                int gameId = resultSets.getInt("game_id");
                String gameName = resultSets.getString("game_name");
                GameRecord gameRecord = new GameRecord(gameId,userId, gameName, serialization);

                gameRecords.add(gameRecord);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return gameRecords;
    }
}
