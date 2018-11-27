package nl.utwente.di.OVSoftware.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleAccount {
    private final String email;

    //Google accounts stores Google accounts from the database
    @JsonCreator
    public GoogleAccount(@JsonProperty("email") String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
