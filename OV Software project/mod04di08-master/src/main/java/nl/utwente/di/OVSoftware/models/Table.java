package nl.utwente.di.OVSoftware.models;

public class Table {

    //Display name of the database.
    private final String name;

    //Link to the database.
    private final String login;

    //Username for the database.
    private final String user;

    //Password for the database.
    private final String pass;

    //Tables are used to provide databases to pull data from.
    public Table(String n, String l, String u, String p) {
        name = n;
        login = l;
        user = u;
        pass = p;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public String toString() {
        return getName() + " " + getUser() + " " + getLogin() + " " + getPass();
    }

}
