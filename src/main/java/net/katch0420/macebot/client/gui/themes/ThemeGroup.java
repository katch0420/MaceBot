package net.katch0420.macebot.client.gui.themes;

import net.katch0420.macebot.client.gui.bodies.ThemeEditorBody;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A node in the theme's category tree: a named group that can hold further
 * sub-groups ("categories inside categories" - e.g. Screen > Bodys >
 * Controller > Entity Panel) and/or leaf color fields.
 * <p>
 * This is the whole extensibility story for theming: adding a new themeable
 * section anywhere in the app means adding one {@code .group(...)} call (and
 * its {@code .field(...)} leaves) to {@link ThemeTree#build}. No screen/body
 * UI code needs to change - {@link ThemeEditorBody} renders whatever tree
 * shape it's handed.
 */
public class ThemeGroup {

    public final String name;
    public final List<ThemeGroup> children = new ArrayList<>();
    public final List<ColorField> fields = new ArrayList<>();

    public ThemeGroup(String name) {
        this.name = name;
    }

    /** Adds (and returns) a new child sub-group - chain off it to add its own fields/sub-groups. */
    public ThemeGroup group(String name) {
        ThemeGroup child = new ThemeGroup(name);
        children.add(child);
        return child;
    }

    /** Adds a leaf color field to THIS group. Returns {@code this} so calls can be chained. */
    public ThemeGroup field(String label, IntSupplier getter, IntConsumer setter) {
        fields.add(new ColorField(label, getter, setter));
        return this;
    }

    public boolean isLeafOnly() {
        return children.isEmpty();
    }

    /** A single editable color field: a display name plus getter/setter into a live Theme instance. */
    public record ColorField(String name, IntSupplier getter, IntConsumer setter) {
    }
}