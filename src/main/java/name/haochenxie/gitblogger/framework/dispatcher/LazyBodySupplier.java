package name.haochenxie.gitblogger.framework.dispatcher;

import java.io.OutputStream;
import java.util.function.Supplier;

public interface LazyBodySupplier extends Supplier<OutputStream> {

}
