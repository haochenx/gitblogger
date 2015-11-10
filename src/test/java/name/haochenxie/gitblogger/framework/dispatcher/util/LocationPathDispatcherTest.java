package name.haochenxie.gitblogger.framework.dispatcher.util;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.Test;

import name.haochenxie.gitblogger.framework.dispatcher.LocationPathDispatcher;

public class LocationPathDispatcherTest {

    @Test
    public void testDecorate() {
        Function<String, String> decorate = LocationPathDispatcher.Helper::decorate;

        assertEquals("/view",
                decorate.apply("/view/"));
        assertEquals("/view",
                decorate.apply("view"));
        assertEquals("/view",
                decorate.apply("/view"));
        assertEquals("/view",
                decorate.apply("view/"));
        assertEquals("/view",
                decorate.apply("//view/"));
        assertEquals("/view",
                decorate.apply("/view///"));
    }

}
