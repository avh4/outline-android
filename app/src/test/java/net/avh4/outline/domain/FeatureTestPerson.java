package net.avh4.outline.domain;

import junit.framework.AssertionFailedError;
import net.avh4.outline.Outline;
import net.avh4.outline.OutlineNode;
import net.avh4.outline.OutlineNodeId;

import java.util.Arrays;


public class FeatureTestPerson {
    public final TestApp app;
    private final FakeFilesystem filesystem = new FakeFilesystem();

    public FeatureTestPerson(FakeTime time) {
        app = new TestApp(filesystem, time);
    }

    private static OutlineNode findChild(Outline outline, OutlineNode parent, String itemName) {
        for (OutlineNodeId childId : parent.getChildren()) {
            OutlineNode child = outline.getNode(childId);
            if (child.getText().equals(itemName)) return child;
        }
        return null;
    }

    public void putFileOnDevice(String filename, String contents) {
        filesystem.link(filename, contents);
    }

    public void importFile(String filename) {
        app.importFile(filename);
    }

    public void assertHasItem(String... itemNamePath) {
        Outline outline = app.inspectOutline();

        OutlineNode current = outline.getNode(outline.getRoot());
        for (String itemName : itemNamePath) {
            current = findChild(outline, current, itemName);
            if (current == null) {
                throw new AssertionFailedError("Did not find: " + itemName);
            }
        }
    }

    public void assertDoesntHaveItem(String... itemPath) {
        try {
            assertHasItem(itemPath);
        } catch (AssertionFailedError e) {
            return;
        }
        throw new AssertionFailedError("Found: " + Arrays.toString(itemPath));
    }
}
