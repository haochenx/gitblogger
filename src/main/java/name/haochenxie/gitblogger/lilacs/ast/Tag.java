package name.haochenxie.gitblogger.lilacs.ast;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

public class Tag {

    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public static Tag of(String name) {
        return new Tag(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return Objects.equal(name(), ((Tag) obj).name());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name())
                .hashCode();
    }

    @Override
    public String toString() {
        return ":" + name();
    }

}
