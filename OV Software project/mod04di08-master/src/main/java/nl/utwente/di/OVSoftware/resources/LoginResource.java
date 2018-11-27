package nl.utwente.di.OVSoftware.resources;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import nl.utwente.di.OVSoftware.utils.Database;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Collections;

@Path("/")
public class LoginResource {

    private final static String CLIENT_ID = "477517551933-umld4fqt1rrf4mhou2t7pd4nb5ruepus.apps.googleusercontent.com";
    private static final JacksonFactory jacksonFactory = new JacksonFactory();
    private static final HttpTransport transport = new NetHttpTransport();

    //Checks the login for local OV accounts.
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/login/{user}/{pass}")
    public int getClichedMessage(@PathParam("user") String user, @PathParam("pass") String pass, @Context HttpServletRequest r) throws SQLException, ClassNotFoundException {
        if (Database.OVAccountAccepted(user, pass)) {
            r.getSession().setAttribute("Timeout", System.currentTimeMillis());
            r.getSession().setAttribute("Database", Database.mainDatabase);
            return 1;
        } else {
            return 0;

        }

    }

    //Log the current session out.
    @GET
    @Path("/logout")
    public void logOut(@Context HttpServletRequest r, @Context HttpServletResponse resp) {
        r.getSession().invalidate();

    }

    //Google login service.
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/googleLogin")
    public int handleLogin(String token, @Context HttpServletRequest request) throws SQLException, ClassNotFoundException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jacksonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(token);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            if (Database.googleAccountAccepted(payload.getEmail())) {
                request.getSession().setAttribute("Timeout", System.currentTimeMillis());
                request.getSession().setAttribute("Database", Database.mainDatabase);
                return 1;
            }
        }
        return 0;
    }
}
