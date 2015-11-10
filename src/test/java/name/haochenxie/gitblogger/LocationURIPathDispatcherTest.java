package name.haochenxie.gitblogger;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.Test;

public class LocationURIPathDispatcherTest {

    @Test
    public void testDecorate() {
        Function<String, String> decorate = LocationURIPathDispatcher.Helper::decorate;

        assertEquals("/view/",
                decorate.apply("/view/"));
        assertEquals("/view/",
                decorate.apply("view"));
        assertEquals("/view/",
                decorate.apply("/view"));
        assertEquals("/view/",
                decorate.apply("view/"));
    }

}
