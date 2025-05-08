package tn.learniverse.controllers.Competition;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeSolid;

public class CompetitionCardController implements Initializable {

    @FXML
    private MFXIconWrapper tagIcon;
    
    @FXML
    private MFXIconWrapper statusIcon;
    
    @FXML
    private MFXIconWrapper startIcon;
    
    @FXML
    private MFXIconWrapper endIcon;
    
    @FXML
    private MFXIconWrapper timerIcon;
    
    @FXML
    private MFXIconWrapper participantsIcon;
    
    @FXML
    private MFXIconWrapper viewIcon;
    
    @FXML
    private MFXIconWrapper editIcon;
    
    @FXML
    private MFXIconWrapper deleteIcon;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Don't use any icons as per user request
    }
} 