package nl.utwente.di.OVSoftware.utils;

import nl.utwente.di.OVSoftware.models.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Database {

    public static Table mainDatabase = new Table("Amsterdam", "//farm03.ewi.utwente.nl:7016/docker", "docker", "YkOkimczn");

    //Creates a connection to the database
    private static Connection MakeConnection(Table database) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        //System.out.println(database);
        String url = "jdbc:postgresql:" + database.getLogin();
        Connection conn = DriverManager.getConnection(url, database.getUser(), database.getPass());
        return conn;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * sql queries statements for everything except search function
     * we put everything into prepared statements so it's secure against sql injection
     */

    //Gets all payrates of the employees
    private static ResultSet allPayrates(Connection conn) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "SELECT * " +
                        "FROM di08.employeerates");
        ResultSet res = p.executeQuery();
        return res;
    }

    //Gets all employees in the humres table
    private static ResultSet allEmployees(Connection conn) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "SELECT h.res_id, h.fullname, h.emp_stat " +
                        "FROM di08.humres h " +
                        "WHERE h.\"freefield 16\" = 'N' " +
                        "ORDER BY h.res_id");
        ResultSet res = p.executeQuery();
        return res;
    }

    //Gets a specific employees Payrates
    private static ResultSet specpr(Connection conn, int crdnr) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "SELECT r.crdnr, r.purchaseprice, r.vandatum, r.totdatum " +
                        "FROM di08.employeerates r, di08.humres h " +
                        "WHERE r.crdnr = h.res_id " +
                        "AND r.crdnr = ?");
        p.setInt(1, crdnr);
        ResultSet res = p.executeQuery();
        return res;
    }

    //Drops all employees in the database
    private static void dropall(Connection conn) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "DELETE FROM di08.employeerates;");
        p.execute();
    }

    //Drops all Payrates for an employee
    private static void delPayrate(Connection conn, int crdnr) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "DELETE FROM di08.employeerates WHERE crdnr = ?");
        p.setInt(1, crdnr);
        p.execute();
    }

    //Adds a list of payrates to the database.
    private static void addPayrates(Connection conn, List<Payrates> list) throws SQLException {
        PreparedStatement p = conn.prepareStatement(
                "INSERT INTO di08.employeerates(crdnr, purchaseprice, vandatum, totdatum) VALUES (?,?,?,?);");
        for (Payrates rate : list) {
            p.setInt(1, rate.getId());
            p.setDouble(2, rate.getCost());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            try {
                startDate.setTime(sdf.parse(rate.getStartDate()));
                endDate.setTime(sdf.parse(rate.getEndDate()));
                p.setDate(3, new java.sql.Date(startDate.getTimeInMillis()));
                p.setDate(4, new java.sql.Date(endDate.getTimeInMillis()));
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            p.execute();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * sql statements for all search queries also in prepared statements
     * we made different methods for different type of queries because it becomes slow if everything is put into a single method
     * Also we had ts vector column and index added to the table make full text search faster
     * But later we realized that we shouldn't change the tables in any way
     * So now we are just querying without index which is also pretty fast
     */

    public static ResultSet search(Connection conn, int crdnr, String fullname) {
        try {
            if (crdnr != -1 && !fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 " +
                                "WHERE ((ts @@ query1 OR ts @@ query2) " +
                                "OR (h.fullname ILIKE ? " +
                                "AND h.res_id::varchar LIKE ?)) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "ORDER BY rank1 DESC, rank2 DESC;");
                p.setString(1, crdnr + "");
                p.setString(2, fullname);
                p.setString(3, fullname + "%");
                p.setString(4, crdnr + "%");
                ResultSet res = p.executeQuery();
                return res;
            } else if (crdnr != -1 && fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query " +
                                "WHERE (ts @@ query " +
                                "OR h.res_id::varchar LIKE ?) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "ORDER BY rank DESC;");
                p.setString(1, crdnr + "");
                p.setString(2, crdnr + "%");
                ResultSet res = p.executeQuery();
                return res;
            } else if (crdnr == -1 && !fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query " +
                                "WHERE (ts @@ query " +
                                "OR h.fullname ILIKE ?) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "ORDER BY rank DESC;");
                p.setString(1, fullname);
                p.setString(2, fullname + "%");
                ResultSet res = p.executeQuery();
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ResultSet searchFilter(Connection conn, int crdnr, String fullname, String status) {
        try {
            if (crdnr != -1 && !fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 " +
                                "WHERE ((ts @@ query1 OR ts @@ query2) " +
                                "OR (h.fullname ILIKE ? " + "AND h.res_id::varchar LIKE ?)) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "AND h.emp_stat = ?" +
                                "ORDER BY rank1 DESC, rank2 DESC;");
                p.setString(1, crdnr + "");
                p.setString(2, fullname);
                p.setString(3, fullname + "%");
                p.setString(4, crdnr + "%");
                p.setString(5, status);
                ResultSet res = p.executeQuery();
                return res;
            } else if (crdnr != -1 && fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query " +
                                "WHERE (ts @@ query " +
                                "OR h.res_id::varchar LIKE ?) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "AND h.emp_stat = ?" +
                                "ORDER BY rank DESC;");
                p.setString(1, crdnr + "");
                p.setString(2, crdnr + "%");
                p.setString(3, status);
                ResultSet res = p.executeQuery();
                return res;
            } else if (crdnr == -1 && !fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank " +
                                "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query " +
                                "WHERE (ts @@ query " +
                                "OR h.fullname ILIKE ?) " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "AND h.emp_stat = ?" +
                                "ORDER BY rank DESC;");
                p.setString(1, fullname);
                p.setString(2, fullname + "%");
                p.setString(3, status);
                ResultSet res = p.executeQuery();
                return res;
            } else if (crdnr == -1 && fullname.equals("-1")) {
                PreparedStatement p = conn.prepareStatement(
                        "SELECT h.res_id, h.fullname, h.emp_stat " +
                                "FROM di08.humres h " +
                                "WHERE h.emp_stat = ? " +
                                "AND h.\"freefield 16\" = 'N' " +
                                "ORDER BY h.res_id");
                p.setString(1, status);
                ResultSet res = p.executeQuery();
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static ResultSet searchSort(Connection conn, int crdnr, String fullname, int sort) throws SQLException {
        PreparedStatement p;
        try {
            if (crdnr != -1 && !fullname.equals("-1")) {
                if (sort == 00) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                    + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                    + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank1 DESC, rank2 DESC, h.res_id;");
                    p.setString(1, crdnr + "");
                    p.setString(2, fullname);
                    p.setString(3, fullname + "%");
                    p.setString(4, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 01) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                    + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                    + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank1 DESC, rank2 DESC, h.res_id DESC;");
                    p.setString(1, crdnr + "");
                    p.setString(2, fullname);
                    p.setString(3, fullname + "%");
                    p.setString(4, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 10) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                    + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                    + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank1 DESC, rank2 DESC, h.fullname;");
                    p.setString(1, crdnr + "");
                    p.setString(2, fullname);
                    p.setString(3, fullname + "%");
                    p.setString(4, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 11) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                    + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                    + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank1 DESC, rank2 DESC, h.fullname DESC;");
                    p.setString(1, crdnr + "");
                    p.setString(2, fullname);
                    p.setString(3, fullname + "%");
                    p.setString(4, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                }
            } else if (crdnr != -1 && fullname.equals("-1")) {
                if (sort == 00) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.res_id::varchar LIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.res_id;");
                    p.setString(1, "'" + crdnr + "'");
                    p.setString(2, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 01) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.res_id::varchar LIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.res_id DESC;");
                    p.setString(1, "'" + crdnr + "'");
                    p.setString(2, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 10) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.res_id::varchar LIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.fullname;");
                    p.setString(1, "'" + crdnr + "'");
                    p.setString(2, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 11) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.res_id::varchar LIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.fullname DESC;");
                    p.setString(1, "'" + crdnr + "'");
                    p.setString(2, crdnr + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                }
            } else if (crdnr == -1 && !fullname.equals("-1")) {
                if (sort == 00) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.fullname ILIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.res_id;");
                    p.setString(1, "'" + fullname + "'");
                    p.setString(2, fullname + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 01) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.fullname ILIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.res_id DESC;");
                    p.setString(1, "'" + fullname + "'");
                    p.setString(2, fullname + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 10) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.fullname ILIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.fullname;");
                    p.setString(1, "'" + fullname + "'");
                    p.setString(2, fullname + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 11) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                    + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                    + "WHERE (ts @@ query "
                                    + "OR h.fullname ILIKE ?) "
                                    + "AND h.\"freefield 16\" = 'N' "
                                    + "ORDER BY rank DESC, h.fullname DESC;");
                    p.setString(1, "'" + fullname + "'");
                    p.setString(2, fullname + "%");
                    ResultSet res = p.executeQuery();
                    return res;
                }
            } else if (crdnr == -1 && fullname.equals("-1")) {
                if (sort == 00) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat "
                                    + "FROM di08.humres h "
                                    + "WHERE h.\"freefield 16\" = 'N' "
                                    + "ORDER BY h.res_id;");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 01) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat "
                                    + "FROM di08.humres h "
                                    + "WHERE h.\"freefield 16\" = 'N' "
                                    + "ORDER BY h.res_id DESC;");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 10) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat "
                                    + "FROM di08.humres h "
                                    + "WHERE h.\"freefield 16\" = 'N' "
                                    + "ORDER BY h.fullname;");
                    ResultSet res = p.executeQuery();
                    return res;
                } else if (sort == 11) {
                    p = conn.prepareStatement(
                            "SELECT h.res_id, h.fullname, h.emp_stat "
                                    + "FROM di08.humres h "
                                    + "WHERE h.\"freefield 16\" = 'N' "
                                    + "ORDER BY h.fullname DESC;");
                    ResultSet res = p.executeQuery();
                    return res;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ResultSet searchFilterSort(Connection conn, int crdnr, String fullname, String status, int sort) {
        PreparedStatement p;
        try {
            if (crdnr != -1 && !fullname.equals("-1")) {
                if (status.equals("-1")) {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.res_id;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.res_id DESC;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.fullname;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.fullname DESC;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                } else {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.res_id;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        p.setString(5, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.res_id DESC;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        p.setString(5, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.fullname;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        p.setString(5, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query1) AS rank1, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query2) AS rank2 "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query1, to_tsquery(?) query2 "
                                        + "WHERE ((ts @@ query1 OR ts @@ query2) "
                                        + "OR (h.fullname::varchar ILIKE ? AND h.res_id::varchar LIKE ?)) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank1 DESC, rank2 DESC, h.fullname DESC;");
                        p.setString(1, crdnr + "");
                        p.setString(2, fullname);
                        p.setString(3, fullname + "%");
                        p.setString(4, crdnr + "%");
                        p.setString(5, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                }
            } else if (crdnr != -1 && fullname.equals("-1")) {
                if (status.equals("-1")) {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.res_id;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.res_id DESC;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.fullname;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.fullname DESC;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                } else {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.res_id;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.res_id DESC;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.fullname;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.res_id::varchar LIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.fullname DESC;");
                        p.setString(1, "'" + crdnr + "'");
                        p.setString(2, crdnr + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                }
            } else if (crdnr == -1 && !fullname.equals("-1")) {
                if (status.equals("-1")) {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.res_id;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.res_id DESC;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.fullname;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "ORDER BY rank DESC, h.fullname DESC;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                } else {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.res_id;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.res_id DESC;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.fullname;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat, ts_rank(to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')), query) AS rank "
                                        + "FROM di08.humres h, to_tsvector('english', coalesce(res_id, '0') ||' '|| coalesce(fullname, '')) ts, to_tsquery(?) query "
                                        + "WHERE (ts @@ query "
                                        + "OR h.fullname ILIKE ?) "
                                        + "AND h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY rank DESC, h.fullname DESC;");
                        p.setString(1, "'" + fullname + "'");
                        p.setString(2, fullname + "%");
                        p.setString(3, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                }
            } else if (crdnr == -1 && fullname.equals("-1")) {
                if (status.equals("-1")) {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "ORDER BY h.res_id;");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "ORDER BY h.res_id DESC;");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "ORDER BY h.fullname;");
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "ORDER BY h.fullname DESC;");
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                } else {
                    if (sort == 00) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY h.res_id;");
                        p.setString(1, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 01) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY h.res_id DESC;");
                        p.setString(1, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 10) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY h.fullname;");
                        p.setString(1, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    } else if (sort == 11) {
                        p = conn.prepareStatement(
                                "SELECT h.res_id, h.fullname, h.emp_stat "
                                        + "FROM di08.humres h "
                                        + "WHERE h.\"freefield 16\" = 'N' "
                                        + "AND h.emp_stat = ?"
                                        + "ORDER BY h.fullname DESC;");
                        p.setString(1, status);
                        ResultSet res = p.executeQuery();
                        return res;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * All these methods gets the parameters from mainresource and use the sql statement methods above to send the result back
     */

    // gets all the employees in the database
    public static List<Employee> getEmployees(Table database) {
        Connection conn;
        try {
            conn = MakeConnection(database);
            ResultSet res = allEmployees(conn);
            List<Employee> l = new ArrayList<>();
            try {
                while (res.next()) {
                    String status = "Unknown";
                    switch (res.getString(3)) {
                        case "A":
                            status = "Active";
                            break;
                        case "I":
                            status = "Not Active";
                            break;
                        case "H":
                            status = "Not Active Yet";
                            break;
                    }
                    l.add(new Employee(res.getInt(1), res.getString(2), status));
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return new ArrayList<Employee>();
    }

    // gets all the google accounts saved in the database
    public static List<GoogleAccount> getGoogleAccounts() {
        Connection conn;
        try {
            conn = MakeConnection(mainDatabase);
            PreparedStatement p = conn.prepareStatement("SELECT * FROM di08.googleaccounts");
            ResultSet res = p.executeQuery();
            conn.close();
            List<GoogleAccount> l = new ArrayList<>();
            try {
                while (res.next()) {
                    l.add(new GoogleAccount(res.getString(1)));
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    // get the google account in the database when accepted
    public static boolean googleAccountAccepted(String email) {
        try {
            Connection conn = MakeConnection(mainDatabase);
            PreparedStatement p = conn.prepareStatement("SELECT * FROM di08.googleaccounts WHERE email=?");
            p.setString(1, email);
            ResultSet res = p.executeQuery();
            conn.close();
            try {
                while (res.next()) {
                    return true;
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    // get all the ov accounts on the database 
    public static List<OVAccount> getOVAccounts() {
        try {
            Connection conn = MakeConnection(mainDatabase);
            PreparedStatement p = conn.prepareStatement("SELECT * FROM di08.localaccounts");
            ResultSet res = p.executeQuery();
            conn.close();
            List<OVAccount> l = new ArrayList<>();
            try {
                while (res.next()) {
                    //don't send password hashes of all users to client for security reasons
                    l.add(new OVAccount(res.getString(1), "•••••••••"));
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    // get the ov account on the database when accepted
    public static boolean OVAccountAccepted(String username, String password) {
        try {
            Connection conn = MakeConnection(mainDatabase);
            PreparedStatement p = conn.prepareStatement("SELECT password FROM di08.localaccounts WHERE username= ?");
            p.setString(1, username);
            ResultSet res = p.executeQuery();
            conn.close();
            try {
                while (res.next()) {
                    return BCrypt.checkpw(password, res.getString(1));
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    // create a new ov account
    public static void createOVAccount(String username, String password) throws SQLException, ClassNotFoundException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        Connection conn = MakeConnection(mainDatabase);
        PreparedStatement p = conn.prepareStatement("INSERT INTO di08.localaccounts VALUES(?,?)");
        p.setString(1, username);
        p.setString(2, hashed);
        p.execute();
        conn.close();

    }

    // create a new google account
    public static void createGoogleAccount(String email) throws SQLException, ClassNotFoundException {
        Connection conn = MakeConnection(mainDatabase);
        PreparedStatement p = conn.prepareStatement("INSERT INTO di08.googleaccounts VALUES(?)");
        p.setString(1, email);
        p.execute();
        conn.close();

    }

    // delete an ov account
    public static void deleteOVAccount(String username) throws SQLException, ClassNotFoundException {
        Connection conn = MakeConnection(mainDatabase);
        PreparedStatement p = conn.prepareStatement("DELETE FROM di08.localaccounts WHERE username=?");
        p.setString(1, username);
        p.execute();
        conn.close();
    }

    // delete a google account
    public static void deleteGoogleAccount(String email) throws SQLException, ClassNotFoundException {
        Connection conn = MakeConnection(mainDatabase);
        PreparedStatement p = conn.prepareStatement("DELETE FROM di08.googleaccounts WHERE email=?");
        p.setString(1, email);
        p.execute();
        conn.close();
    }

    //Edit the payrates for one employee with deletion
    public static void editPayrates(int crdnr, List<Payrates> list, Table database) throws SQLException, ClassNotFoundException {
            Connection conn = MakeConnection(database);
            delPayrate(conn, crdnr);
            addPayrates(conn, list);
    }

    //Import the payrates for all employee with deletion
    public static void importPayrts(List<Payrates> list, Table database) {
        try {
            Connection conn = MakeConnection(database);
            try {
                dropall(conn);
                addPayrates(conn, list);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // get payrates for a specific employee
    public static List<Payrates> getPayratesSpecificEmployee(int crdnr, Table database) {
        try {
            Connection conn = MakeConnection(database);
            ResultSet res = specpr(conn, crdnr);
            conn.close();
            List<Payrates> l = new ArrayList<>();
            try {
                while (res.next()) {
                    try {
                        l.add(new Payrates(res.getInt(1), res.getDouble(2), res.getString(3), res.getString(4)));
                    } catch (ParseException e) {

                    }
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    // get all the payrates of the all employees in the database
    public static List<Payrates> getAllPayrates(Table database) {
        Connection conn;
        try {
            conn = MakeConnection(database);
            ResultSet res = allPayrates(conn);
            List<Payrates> l = new ArrayList<>();
            try {
                while (res.next()) {
                    try {
                        l.add(new Payrates(res.getInt(1), res.getDouble(2), res.getString(3), res.getString(4)));
                    } catch (ParseException e) {

                    }
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    // all the combinaitons of search, sort, filter queries are handled by this method
    public static List<Employee> searchEmployees(int crdnr, String fullname, String status, int sort, Table database) {
        try {
            Connection conn = MakeConnection(database);
            ResultSet res;
            if ((crdnr != -1 || !fullname.equals("-1")) && status.equals("-1") && sort == -1) {
                res = Database.search(conn, crdnr, fullname);
            } else if (!status.equals("-1") && sort == -1) {
                res = Database.searchFilter(conn, crdnr, fullname, status);
            } else if (status.equals("-1") && sort != -1) {
                res = Database.searchSort(conn, crdnr, fullname, sort);
            } else if (sort != -1) {
                res = Database.searchFilterSort(conn, crdnr, fullname, status, sort);
            } else {
                res = null;
            }
            List<Employee> l = new ArrayList<>();
            try {
                if (!res.wasNull()) {
                    while (res.next()) {
                        String stat = "unknown";
                        switch (res.getString(3)) {
                            case "A":
                                stat = "Active";
                                break;
                            case "I":
                                stat = "Not Active";
                                break;
                            case "H":
                                stat = "Not Active Yet";
                                break;
                        }
                        l.add(new Employee(res.getInt(1), res.getString(2), stat));
                    }
                }
            } catch (SQLException | NullPointerException e) {

            }
            return l;
        } catch (ClassNotFoundException | SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}