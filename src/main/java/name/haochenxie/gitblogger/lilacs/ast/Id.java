package name.haochenxie.gitblogger.lilacs.ast;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Id {

    private String name;

    public Id(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public static Id of(String name) {
        return new Id(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Id) {
            Id id = (Id) obj;
            return Objects.equals(name(), id.name());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name())
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("ID['%s']", name());
    }

}
