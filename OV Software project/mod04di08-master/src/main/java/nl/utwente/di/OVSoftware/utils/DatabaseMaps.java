package nl.utwente.di.OVSoftware.utils;

import nl.utwente.di.OVSoftware.models.Table;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMaps {

    //Holds all of the databases.
    private List<Table> users = new ArrayList<>();

    //DatabaseMaps is used to store all of the databases that the can be used.
    public DatabaseMaps() {
        this.newDatabase("Oldenzaal", "//farm03.ewi.utwente.nl:7016/docker", "docker", "YkOkimczn");
        this.newDatabase("Den Haag", "//castle.ewi.utwente.nl:5432/di049", "di049", "Hzixmlr+");
    }

    //Adds a new database to the databases list.
    public void newDatabase(String name, String login, String user, String pass) {
        users.add(new Table(name, login, user, pass));
    }

    //Returns a list of names for all of the database.
    public List<String> getDatabases() {
        List<String> list = new ArrayList<String>();
        for (Table t : users) {
            list.add(t.getName());
        }
        return list;
    }

    //Returns the first database in the database list.
    public String getFirst() {
        return users.get(1).getLogin();
    }

    //Returns the database matching the name given.
    public Table nametotable(String n) {
        for (Table d : users) {
            if (d.getName().equals(n)) {
                return d;
            }
        }
        return null;
    }

}
