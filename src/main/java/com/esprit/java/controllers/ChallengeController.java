package com.esprit.java.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import Services.ChallengeService;

public class ChallengeController {
    private ChallengeService challengeService;
    private Connection connection;

    public ChallengeController(Connection connection) {
        this.connection = connection;
        this.challengeService = new ChallengeService(connection);
    }

    // Methods to handle UI interactions and call ChallengeService methods...
}
