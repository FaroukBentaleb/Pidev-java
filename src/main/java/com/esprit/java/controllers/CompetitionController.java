package com.esprit.java.controllers;

import com.esprit.java.Services.CompetitionService;
import com.esprit.java.Models.Competition;
import com.esprit.java.Models.Challenge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompetitionController {
    private CompetitionService competitionService;
    private Connection connection;

    public CompetitionController(Connection connection) {
        this.connection = connection;
        this.competitionService = new CompetitionService(connection);
    }




}
