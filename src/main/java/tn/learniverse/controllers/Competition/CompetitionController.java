package tn.learniverse.controllers.Competition;

import tn.learniverse.services.CompetitionService;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Challenge;

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
