package Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChallengeService {
    private Connection connection;

    public ChallengeService(Connection connection) {
        this.connection = connection;
    }

    // CRUD methods (createChallenge, getChallengeById, etc.) ...
}