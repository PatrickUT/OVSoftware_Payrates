package nl.utwente.di.OVSoftware.resources;

import nl.utwente.di.OVSoftware.models.GoogleAccount;
import nl.utwente.di.OVSoftware.models.OVAccount;
import nl.utwente.di.OVSoftware.utils.Database;
import nl.utwente.di.OVSoftware.utils.Login;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("/admin")
public class AdminResource {

    //Returns a list off all OV accounts.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ovusers")
    public List<OVAccount> getAllOVUsers(@Context HttpServletRequest r) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            List<OVAccount> ovlist = Database.getOVAccounts();
            return ovlist;
        }
        return null;
    }

    //Returns a list of all Google accounts.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/googleusers")
    public List<GoogleAccount> getAllGoogleUsers(@Context HttpServletRequest r) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            List<GoogleAccount> googlelist = Database.getGoogleAccounts();
            return googlelist;
        }
        return null;
    }

    //Creates a new OV account.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ovuser")
    public void createOVUser(@Context HttpServletRequest r, OVAccount ovAccount) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            Database.createOVAccount(ovAccount.getUsername(), ovAccount.getPassword());
        }
    }

    //Creates a new Google account.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/googleuser")
    public void createGoogleUser(@Context HttpServletRequest r, GoogleAccount googleAccount) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            Database.createGoogleAccount(googleAccount.getEmail());
        }
    }

    //Deletes an OV accounts.
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ovuser")
    public void deleteOVUser(@Context HttpServletRequest r, OVAccount ovAccount) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            Database.deleteOVAccount(ovAccount.getUsername());
        }
    }

    //Deletes a Google account.
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/googleuser")
    public void deleteGoogleUser(@Context HttpServletRequest r, GoogleAccount googleAccount) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            Database.deleteGoogleAccount(googleAccount.getEmail());
        }
    }


}
