package tn.learniverse.services;

import com.google.gson.Gson;
import tn.learniverse.entities.Submission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

public class SubmissionService {
    private Connection connection;

    public SubmissionService(Connection connection) {
        this.connection = connection;
    }

void saveSubmission(Submission submission) throws SQLException {
      String sql = "INSERT INTO submission (id_challenge_id, id_user_id, student_try, time_taken, date, rating, ai_feedback, corrected_code, competition_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
      try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
          statement.setInt(1, submission.getIdChallenge().getId());
          statement.setInt(2, submission.getIdUser().getId());
          statement.setString(3, submission.getStudentTry());
          statement.setInt(4, submission.getTimeTaken());
          statement.setTimestamp(5, Timestamp.valueOf(submission.getDate().atStartOfDay()));

          // Handle null rating
          if (submission.getRating() == null) {
              statement.setNull(6, java.sql.Types.INTEGER);
          } else {
              statement.setInt(6, submission.getRating());
          }

          // Handle ai_feedback
          statement.setString(7, (submission.getAiFeedback() == null || submission.getAiFeedback().length == 0) ?
                  null : Arrays.toString(submission.getAiFeedback()));

          // Handle corrected_code
          statement.setString(8, (submission.getCorrectedCode() == null || submission.getCorrectedCode().length == 0) ?
                  null : Arrays.toString(submission.getCorrectedCode()));

          // Handle competition_id
          if (submission.getComp() == null) {
              statement.setNull(9, java.sql.Types.INTEGER);
          } else {
              statement.setInt(9, submission.getComp().getId());
          }

          // Execute the insert
          statement.executeUpdate();

          // Retrieve the generated ID
          try (var generatedKeys = statement.getGeneratedKeys()) {
              if (generatedKeys.next()) {
                  submission.setId(generatedKeys.getInt(1)); // Set the generated ID to the submission object
              } else {
                  throw new SQLException("Failed to retrieve the generated ID for the submission.");
              }
          }
      } catch (SQLException e) {
          throw new SQLException("Failed to save submission: " + e.getMessage(), e);
      }
  }
    // In SubmissionService class
void updateSubmission(Submission submission) throws SQLException {
    String sql = "UPDATE submission SET rating = ?, ai_feedback = ?, corrected_code = ? WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        // Handle null rating
        if (submission.getRating() == null) {
            statement.setNull(1, java.sql.Types.INTEGER);
        } else {
            statement.setInt(1, submission.getRating());
        }

        // Convert ai_feedback to JSON string or set null
        if (submission.getAiFeedback() == null || submission.getAiFeedback().length == 0) {
            statement.setNull(2, java.sql.Types.VARCHAR);
        } else {
            String aiFeedbackJson = new Gson().toJson(submission.getAiFeedback());
            statement.setString(2, aiFeedbackJson);
        }

        // Convert corrected_code to JSON string or set null
        if (submission.getCorrectedCode() == null || submission.getCorrectedCode().length == 0) {
            statement.setNull(3, java.sql.Types.VARCHAR);
        } else {
            String correctedCodeJson = new Gson().toJson(submission.getCorrectedCode());
            statement.setString(3, correctedCodeJson);
        }

        // Set the ID
        statement.setInt(4, submission.getId());

        // Execute the update
        statement.executeUpdate();
    } catch (SQLException e) {
        throw new SQLException("Failed to update submission: " + e.getMessage(), e);
    }
}

}
