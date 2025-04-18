package tn.learniverse.controllers.Competition;

import java.sql.Connection;
import java.sql.SQLException;
import  tn.learniverse.services.ChallengeService;

public class ChallengeController {
    private ChallengeService challengeService;
    private Connection connection;

    public ChallengeController(Connection connection) {
        this.connection = connection;
        this.challengeService = new ChallengeService(connection);
    }

    // Methods to handle UI interactions and call ChallengeService methods...
}
