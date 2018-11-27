package nl.utwente.di.OVSoftware.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nl.utwente.di.OVSoftware.exceptions.DateException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@JsonPropertyOrder({"id", "cost", "startDate", "endDate"})
public class Payrates implements Comparable<Payrates> {

    private final int id;
    private final double cost;
    private final Calendar startDate;
    private final Calendar endDate;

    //Payrates are used to store pay rates for employees

    @JsonCreator
    public Payrates(@JsonProperty("emp_id") int i, @JsonProperty("price") double c, @JsonProperty("startdate") String s, @JsonProperty("enddate") String e) throws ParseException {
        id = i;
        cost = c;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        startDate.setTime(sdf.parse(s));
        endDate.setTime(sdf.parse(e));
    }

    //Checks if a list of payrates is valid.
    public static void checkIntegrity(List<Payrates> mainlist) throws DateException {
        List<Payrates> head = new ArrayList<>(mainlist);
        Collections.sort(head);
        checkPayrate(mainlist);
        checkDates(head, mainlist);
    }

    //Checks for a list of payrates if all of the start dates are before the end dates.
    private static void checkPayrate(List<Payrates> head) throws DateException {
        int i = 0;
        while (i < head.size()) {
            if (!head.get(i).checkDates()) {
                throw new DateException(i + 1 + ": " + head.get(i).getStartDate() + " " + head.get(i).getEndDate() + " Start date after end date");
            }
            i++;
        }
    }

    //Checks for a list of payrates if all of the sequential payrates have a start date the day after the previous' payrates end date.
    private static void checkDates(List<Payrates> head, List<Payrates> mainlist) throws DateException {
        List<Payrates> list = new ArrayList<Payrates>(head);
        while (!list.isEmpty()) {
            int id = list.get(0).getId();
            List<Payrates> temp = new ArrayList<Payrates>();
            int i = 0;
            int i2 = 0;
            while (i < list.size()) {
                Payrates item = list.get(i);
                if (item.getId() == id) {
                    temp.add(item);
                    list.remove(i);
                } else {
                    i++;
                }
            }
            while (i2 < temp.size() - 1) {
                if (!temp.get(i2).isNextDate(temp.get(i2 + 1).getStartDate())) {
                    throw new DateException((mainlist.indexOf(temp.get(i2 + 1)) + 1) + ": " + temp.get(i2).getEndDate() + " " + temp.get(i2 + 1).getStartDate() + " Start date is not next day from previous end date");
                }
                i2++;
            }
        }
    }

    public int getId() {
        return id;
    }

    public double getCost() {
        return cost;
    }

    //a comparing function to check if the string is equivalent to the day after this pay rates end date ends
    public boolean isNextDate(String next) {
        Calendar temp = (Calendar) endDate.clone();
        temp.add(Calendar.DATE, 1);
        return format(temp).equals(next);
    }

    public String getStartDate() {
        return format(startDate);
    }

    public String format(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date.getTime());
    }

    public String getEndDate() {
        return format(endDate);
    }

    @Override
    public int compareTo(Payrates o) {
        return startDate.compareTo(o.startDate);
    }

    public boolean checkDates() {
        return startDate.before(endDate);
    }

    public String toString() {
        return getId() + " " + getCost() + " " + getStartDate() + " " + getEndDate();
    }

}