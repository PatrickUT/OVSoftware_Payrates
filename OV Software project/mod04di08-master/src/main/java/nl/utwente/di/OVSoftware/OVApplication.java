package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.resources.AdminResource;
import nl.utwente.di.OVSoftware.resources.LoginResource;
import nl.utwente.di.OVSoftware.resources.MainResource;
import nl.utwente.di.OVSoftware.resources.TestResource;
import nl.utwente.di.OVSoftware.utils.CsvWriter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class OVApplication extends Application {

    //This function makes sure all of the classes are registered.
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        classes.add(TestResource.class);
        classes.add(LoginResource.class);
        classes.add(MainResource.class);
        classes.add(CsvWriter.class);
        classes.add(AdminResource.class);
        return classes;
    }
}
