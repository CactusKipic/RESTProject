package fr.cactus_industries.restservice.survey;

import fr.cactus_industries.query.ListSondage;
import fr.cactus_industries.query.Proposition;
import fr.cactus_industries.query.Sondage;
import fr.cactus_industries.query.Vote;
import fr.cactus_industries.restservice.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Survey {
    
    public static int getNumberOfSurvey() {
        int result = -1;
        //REQUETE
        String query = "SELECT COUNT(*) FROM SONDAGES";
        Connection con = Database.getDBConnection();
        
        if(con !=null)
        //CONNEXION
        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            //RECUPERATION DES DONNEES (ici : première ligne, contenant le nombre de sondages créés)
            if (rs.next()) {
                result= rs.getInt(1);
            }
        }
        //EN CAS D'ERREUR
        catch (SQLException e) {
            result=-2;
            e.printStackTrace();
        }
        //ON RETOURNE LE NOMBRE DE SONDAGES DANS LA BDD
        return result;
    }
    
    public static ListSondage getAllPublicSurveys() {
        String query = "select id from SONDAGES where sondagePrive='0';";
        Connection con = Database.getDBConnection();
        ListSondage LSond = new ListSondage();

        if(con != null)
            //CONNEXION
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()){
                    LSond.add(getSurveyById(rs.getInt("id")));
                }
                return LSond;
            }
            //EN CAS D'ERREUR
            catch (SQLException e) {
                e.printStackTrace();
            }
        return null;

    }
    
    
    public static Sondage getSurveyById(int id) {
        String query = "select nom, description, authorId, sondagePrive from SONDAGES where id="+id+";";
        
        Connection con = Database.getDBConnection();
        
        if(con != null)
        //CONNEXION
        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                //RECUPERATION DES DONNEES
                String nom = rs.getString("nom");
                String description = rs.getString("description");
                int authorId = rs.getInt("authorId");
                int sondagePrive = rs.getInt("sondagePrive");
                
                //CREATION LOCAL DU SONDAGE
                Sondage sondageSuccess = new Sondage(id, nom, description, authorId, sondagePrive, PropositionRDV.getListOfPropositionsBySondageId(id));
                return sondageSuccess;
            }
        }
        //EN CAS D'ERREUR
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static ListSondage getSurveysFromUser(int userID) {
        String query = "SELECT id, nom, description, sondagePrive from SONDAGES where authorId="+userID+";";
        
        Connection con = Database.getDBConnection();
        ListSondage LSond = new ListSondage();
        
        if(con != null)
        //CONNEXION
        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                LSond.add(new Sondage(rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        userID,
                        rs.getInt("sondagePrive"),
                        PropositionRDV.getListOfPropositionsBySondageId(rs.getInt("id")))
                );
            }
            return LSond;
        }
        //EN CAS D'ERREUR
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int createSurvey(String nom, String description, int authorId, int sondagePrive){
        int id = getNumberOfSurvey();
        //String query = "INSERT INTO SONDAGES (id, nom, description, authorId, sondagePrive) VALUES ('1', 'SondageTest', 'CeciEstUnTest', '2', '0');";
        
        String query = "INSERT INTO SONDAGES (id, nom, description, authorId, sondagePrive) VALUES ('"+id+"', '"+nom+"', '"+description+"', '"+authorId+"', '"+sondagePrive+"');";
        
        Connection con = Database.getDBConnection();
        
        if(con != null)
        //CONNEXION
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
        }
        //EN CAS D'ERREUR
        catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    
    
    public static int deleteSurvey(int id, int authorId) {
        //String query = "INSERT INTO SONDAGES (id, nom, description, authorId, sondagePrive) VALUES ('1', 'SondageTest', 'CeciEstUnTest', '2', '0');";
        
        String query = "DELETE FROM SONDAGES WHERE '"+id+"';";
        
        Connection con = Database.getDBConnection();
        
        if(con != null)
        //CONNEXION
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
        }
        //EN CAS D'ERREUR
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean checkAlreadyVote(int idProposition, int idUser) {
        String query = "select id, associatedProposition, associatedUser from VOTES where associatedProposition='"+idProposition+"' AND associatedUser='"+idUser+"'";

        Connection con = Database.getDBConnection();
        List<Proposition> listOfPropositions = new ArrayList<>();
        if(con != null) {
            //CONNEXION
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    if( (rs.getInt("associatedProposition")==idProposition) && (rs.getInt("associatedUser")==idUser) ) {
                        return true;
                    }
                }
                rs.close();
            }
            //EN CAS D'ERREUR
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
