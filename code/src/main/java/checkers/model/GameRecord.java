package checkers.model;

public class GameRecord {
    private int id;
    private int gameId;
    private String gameName;
    private String serialization;

    public GameRecord(int id, int gameId, String gameName, String serialization) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.serialization = serialization;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }
}
