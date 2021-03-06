package fr.cactus_industries.restservice.login;

import fr.cactus_industries.restservice.Database;
import fr.cactus_industries.restservice.FailResponse;
import fr.cactus_industries.restservice.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// Handle Login and Register
public class LogIn {

    // Test l'existance d'un compte pour un login et pass, renvoie un objet contenant le token, l'ID du compte et l'expiration du token en cas de succès
    public static LoggedTokenInfo login(String User, String pass){
        
        String query = "SELECT id FROM USERS where login='"+User+"' AND pass='"+digestPass(pass)+"';";

        Connection con = Database.getDBConnection();

        if(con != null)
        // CONNEXION
        try (Statement stmt = con.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                return LoggedTokenInfo.getToken(rs.getInt("id"));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        
        return null;
    }
    
    // Renvoie les infos associés à un token si il existe et s'il est encore valide, sinon renvoie null
    public static LoggedTokenInfo login(String token){
        LoggedTokenInfo tokenInfo = TokenHandler.getIDFromToken(token);
        
        if(tokenInfo != null)
            if(tokenInfo.isValid())
                return tokenInfo;
            else
                TokenHandler.deleteToken(tokenInfo);
        
        return null;
    }

    // Renvoie en réponse les infos d'un compte s'il existe
    public static Response getAccountInfo(int ID){
        String query = "SELECT login, mail FROM USERS where id='"+ID+"';";
        
        Connection con = Database.getDBConnection();
        
        if(con != null)
            // CONNEXION
            try (Statement stmt = con.createStatement()){
                ResultSet rs = stmt.executeQuery(query);
                if(rs.next()){
                    return new AccountResponse(rs.getString("mail"), rs.getString("login"));
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        return new FailResponse(FailResponse.Reason.ACCOUNTNOTFOUND);
    }

    // Enregistre un nouveau compte après avoir vérifié la conformité du mail, login et pass
    public static Response register(String mail, String user, String pass){
        if(!mail.matches("(?i)^\\w+[+.\\w-]*@(?:[\\w-]+\\.)*\\w+[\\w-]*\\.(?:[a-z]{2,4})$")){
            return new FailResponse(FailResponse.Reason.INVALIDMAIL);
        }
        if(!user.matches("(?i)[A-Za-z_\\-]{3,32}"))
            return new FailResponse(FailResponse.Reason.INVALIDUSER);
        if(!pass.matches("(?i)[A-Za-z_\\-()\\[\\]]{8,32}"))
            return new FailResponse(FailResponse.Reason.INVALIDPASS);

        String HPass = digestPass(pass);
        if(HPass == null)
            return new FailResponse();
        System.out.println("HPass: "+HPass);

        String query = "INSERT INTO USERS (mail, login, pass) VALUES ('"
                + mail +"', '"+ user+"', '"+ HPass +"');";

        Connection con = Database.getDBConnection();

        if(con != null)
        // CONNEXION
        try (Statement stmt = con.createStatement()){
            // EXECUTION DE L'INSERT
            if(stmt.executeUpdate(query) == 0)
                return new FailResponse();

        } catch (SQLException throwable) {
            if(throwable.getErrorCode() == 1062)
                return new FailResponse(FailResponse.Reason.DUPLICATELOGIN);
            throwable.printStackTrace();
            return new FailResponse();
        }

        return new Response("OK");
    }
    
    // Modifie une ou plusieurs info d'un compte si le mot de passe fourni est correct
    public static Response changeAccountInfo(int ID, String pass, String mail, String user, String newPass) {
        String query = "SELECT login FROM USERS where id='"+ID+"' AND pass='"+digestPass(pass)+"';";
        
        Connection con = Database.getDBConnection();
        
        if(con != null)
        // CONNEXION
        try (Statement stmt = con.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next() && rs.getString("login") == null)
                return new FailResponse(FailResponse.Reason.BADLOGIN);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        HashMap<String, String> renew = new HashMap<>();
        query = "UPDATE USERS SET ";
        if(!mail.equals(""))
        if(!mail.matches("(?i)^\\w+[+.\\w-]*@(?:[\\w-]+\\.)*\\w+[\\w-]*\\.(?:[a-z]{2,4})$"))
            return new FailResponse(FailResponse.Reason.INVALIDMAIL);
        else
            renew.put("mail",mail);
        
        if(!user.equals(""))
        if(!user.matches("(?i)[A-Za-z_\\-]{3,32}"))
            return new FailResponse(FailResponse.Reason.INVALIDUSER);
        else
            renew.put("login", user);
        
        if(!newPass.equals(""))
        if(!newPass.matches("(?i)[A-Za-z_\\-()\\[\\]]{8,32}"))
            return new FailResponse(FailResponse.Reason.INVALIDPASS);
        else
            renew.put("pass", digestPass(newPass));
        
        ArrayList<String> entries = new ArrayList<>(renew.keySet());
        int i;
        query += entries.get(0)+"='"+renew.get(entries.get(0))+"' ";
        
        for(i = 1; i < entries.size(); i++)
            query += "AND "+entries.get(i)+"='"+renew.get(entries.get(i))+"' "; // C'est moche, mais pas grave
        query += "WHERE id='"+ID+"';";
    
        if(con != null)
        // CONNEXION
        try (Statement stmt = con.createStatement()){
            // EXECUTION DE L'INSERT
            if(stmt.executeUpdate(query) == 0)
                return new FailResponse();
        
        } catch (SQLException throwable) {
            if(throwable.getErrorCode() == 1062)
                return new FailResponse(FailResponse.Reason.DUPLICATELOGIN);
            throwable.printStackTrace();
            return new FailResponse();
        }
        
        return new Response("OK");
    }

    // Renvoie un hash du pass en SHA3-256 pour le stockage sur la BDD
    private static String digestPass(String pass){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA3-256");
            StringBuilder HPass = new StringBuilder();
            for(Byte b:md.digest(pass.getBytes(StandardCharsets.UTF_8)))
                HPass.append(String.format("%02x",b));
            return HPass.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
