package nl.utwente.di.OVSoftware.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OVAccount {
    private String username;
    private String password;

    //OV accounts are used to store the usernames and passwords for local accounts.
    @JsonCreator
    public OVAccount(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
