package com.horcruxid.main;

import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;

public class Application {

    public static Boolean authenticatePreconfiguredSingleUserWithPAM() {
        UnixUser user = null;
        Boolean authenticated;

        try {
            // PAM service name is read from ENV VAR (fixed key used here...)
            // username and password are the ones that person who is logging in enters...
            user = new PAM("pam service name")
                    .authenticate("username", "password");
            authenticated = true;
        } catch (PAMException e) {
            authenticated = false;
        }

        return authenticated;
    }

    /**
     * NOTE! Since this is a library that => no main method!
     * PAM service name, username, and password are
     * Also, NO main method therefore!
     */

    /*
    public static void main(String[] args) {

    }
    */
}
