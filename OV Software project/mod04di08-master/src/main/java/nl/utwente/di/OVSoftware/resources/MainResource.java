package nl.utwente.di.OVSoftware.resources;

import nl.utwente.di.OVSoftware.exceptions.DateException;
import nl.utwente.di.OVSoftware.models.Employee;
import nl.utwente.di.OVSoftware.models.Payrates;
import nl.utwente.di.OVSoftware.models.Table;
import nl.utwente.di.OVSoftware.utils.Database;
import nl.utwente.di.OVSoftware.utils.DatabaseMaps;
import nl.utwente.di.OVSoftware.utils.Login;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Path("/main")
public class MainResource {

    DatabaseMaps tables = new DatabaseMaps();

    //returns a list of all employees in the database
    @GET
    @Path("/employees")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> getEmployees(@Context HttpServletRequest r) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            return Database.getEmployees((Table) r.getSession().getAttribute("Database"));
        }
        return null;
    }

    //returns a list of all pay rates for a specific employee.
    @GET
    @Path("/employees/{crdnr}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Payrates> getEmployees(@Context HttpServletRequest r, @PathParam("crdnr") int n) throws ParseException, SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            return Database.getPayratesSpecificEmployee(n, (Table) r.getSession().getAttribute("Database"));
        }
        return null;
    }

    //returns a list of all employees that are compliant with the search parameters.
    @GET
    @Path("/search/{crdnr}/{name}/status/{status}/sort/{num}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> search(@Context HttpServletRequest r, @PathParam("crdnr") int n, @PathParam("name") String c, @PathParam("status") String s, @PathParam("num") int so) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            return Database.searchEmployees(n, c, s, so, (Table) r.getSession().getAttribute("Database"));
        }
        return null;
    }

    //Receives a string of all the new pay rates for an employee and puts these in the database after validating these.
    @POST
    @Path("/editPayrates")
    @Consumes(MediaType.TEXT_PLAIN)
    public String editPayrates(@Context HttpServletRequest r, String payrates) throws ParseException, DateException, ClassNotFoundException, SQLException {
        if (Login.Security(r.getSession()) == 1) {
            Scanner s = new Scanner(payrates);
            String ret = null;
            List<Payrates> prts = new ArrayList<>();
            int crdnr = -1;
            if (s.hasNextLine()) {
                crdnr = Integer.parseInt(s.nextLine());
            }
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] elems = line.split(",");
                double cost = Double.parseDouble(elems[0]);
                if (cost < 0) {
                    throw new NumberFormatException();
                }
                prts.add(new Payrates(crdnr, cost, elems[1], elems[2]));
            }
            s.close();
            Payrates.checkIntegrity(prts);
            Database.editPayrates(crdnr, prts, (Table) r.getSession().getAttribute("Database"));
            if (ret == null) {
                return "1";
            }
        }
        return null;
    }


    //Exports a complete pay rates list from the database in csv format.
    @GET
    @Path("/export.csv")
    @Produces("text/csv")
    public List<Payrates> exportcsv(@Context HttpServletRequest r) throws SQLException, ClassNotFoundException {
        if (Login.Security(r.getSession()) == 1) {
            return Database.getAllPayrates((Table) r.getSession().getAttribute("Database"));
        }
        return null;
    }

    //Imports a csv from the user and overwrites the database with it.
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String importcsv(@Context HttpServletRequest r, @FormDataParam("files") InputStream in) {
        String ret = "import success";
        if (Login.Security(r.getSession()) == 1) {
            try {
                List<Payrates> list = new ArrayList<>();
                Scanner s = new Scanner(in);
                while (!s.nextLine().equals("id,cost,startDate,endDate"));
                String line = " ";
                while (s.hasNextLine() || line.equals("")) {
                    line = s.nextLine();
                    String[] words = line.split(",");
                    if (words.length == 4) {
                        int id = Integer.parseInt(words[0]);
                        double cost = Double.parseDouble(words[1]);
                        if (cost < 0) {
                            s.close();
                            throw new NumberFormatException();
                        }
                        try {
                            list.add(new Payrates(id, cost, words[2], words[3]));
                        } catch (ParseException e) {
                            s.close();
                            return "A date is not valid";
                        }
                    }
                }
                s.close();
                try {
                    Payrates.checkIntegrity(list);
                    Database.importPayrts(list, (Table) r.getSession().getAttribute("Database"));
                } catch (DateException e) {
                    ret = e.getMessage();
                }
            } catch (NumberFormatException e) {
                return "A cost is not valid";
            }

        }
        return ret;
    }

    //returns all database names.
    @GET
    @Path("/databases")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getDatabases(@Context HttpServletRequest r) {
        if (Login.Security(r.getSession()) == 1) {
            return tables.getDatabases();
        }
        return null;
    }

    //Selects the database for a session.
    @POST
    @Path("/databases/{selection}")
    @Produces(MediaType.TEXT_PLAIN)
    public int selectDatabases(@Context HttpServletRequest r, @PathParam("selection") String n) {
        if (Login.Security(r.getSession()) == 1) {
            r.getSession().setAttribute("Database", tables.nametotable(n));
            return 1;
        }
        return 0;
    }


}
