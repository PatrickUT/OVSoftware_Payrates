package nl.utwente.di.OVSoftware.utils;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

@Provider
@Consumes("text/csv")
@Produces("text/csv")


//The CsvWriter is used to automatically create csv files from a list.
public class CsvWriter implements MessageBodyWriter<Object> {

    @Override
    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        return true;
    }

    @Override
    public void writeTo(Object o, @SuppressWarnings("rawtypes") Class arg1, Type arg2, Annotation[] arg3, MediaType arg4, @SuppressWarnings("rawtypes") MultivaluedMap arg5,
                        OutputStream out) throws IOException, WebApplicationException {
        if (o instanceof List<?>) {
            List<?> data = (List<?>) o;
            if (data != null && data.size() > 0) {
                CsvMapper m = new CsvMapper();
                CsvSchema s = m.schemaFor(data.get(0).getClass()).withHeader();
                m.writer(s).writeValue(out, data);
            }
        }
    }
}
