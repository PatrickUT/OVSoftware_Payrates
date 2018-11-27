package nl.utwente.di.OVSoftware.exceptions;


public class DateException extends Exception {

    private static final long serialVersionUID = 1L;

    //Exception used to notify that a list of pay rates is not valid.
    public DateException(String s) {
        super(s);
    }
}
