package net.katch0420.macebot.client.gui.widgets.core;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParentWidget extends ChildWidget{
    public final List<ChildWidget> children = Lists.newArrayList();

    public void addDrawableChild(ChildWidget child){
        if(child != null){
            children.add(child);
        }
    }

    public void removeDrawableChild(ChildWidget child){
        children.remove(child);
    }

    public void clearChildren(){
        children.clear();
    }

    public List<ChildWidget> getChildren(){
        return children;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for(ChildWidget child : children){
            child.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for(ChildWidget child : children){
            child.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ChildWidget child : children) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ChildWidget child : children) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (ChildWidget child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (ChildWidget child : children) {
            if (child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ChildWidget child : children) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ChildWidget child : children) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ChildWidget child : children) {
            if (child.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (ChildWidget child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        for (ChildWidget child : children) {
            GuiNavigationPath path = child.getNavigationPath(navigation);
            if (path != null) {
                return path;
            }
        }
        return null;
    }
}
