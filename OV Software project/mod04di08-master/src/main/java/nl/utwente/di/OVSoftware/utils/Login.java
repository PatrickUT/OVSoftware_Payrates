package nl.utwente.di.OVSoftware.utils;

import javax.servlet.http.HttpSession;

public class Login {

    //The timeout in milliseconds before the session expires.
    public static Long TIMEOUT = (long) 300000;

    //Security check ran by all of the resources to check whether the user is still logged in or has timed out.
    public static int Security(HttpSession s) {
        Object x = s.getAttribute("Timeout");
        if (x != null && x instanceof Long) {
            if (System.currentTimeMillis() - (Long) x < TIMEOUT) {
                s.setAttribute("Timeout", System.currentTimeMillis());
                if (s.getAttribute("Database") == null) {
                    s.setAttribute("Database", Database.mainDatabase);
                }
                return 1;
            }
        }
        return 0;
    }

}
