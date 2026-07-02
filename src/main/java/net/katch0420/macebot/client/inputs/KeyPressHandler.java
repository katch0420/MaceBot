package net.katch0420.macebot.client.inputs;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public class KeyPressHandler {
    public static Action getKeyAction(int k) {
        if (isCloseKey(k)) {
            return Action.ESCAPE;
        }
        if(k == GLFW.GLFW_KEY_UP) return Action.UP;
        if(k == GLFW.GLFW_KEY_DOWN) return Action.DOWN;
        if(k == GLFW.GLFW_KEY_LEFT) return Action.LEFT;
        if(k == GLFW.GLFW_KEY_RIGHT) return Action.DOWN;
        return Action.PASS;
    }

    public static boolean isCloseKey(int k){
        return (k == GLFW.GLFW_KEY_ESCAPE) || k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.open).getCode();
    }

    public enum Action{
        ESCAPE,
        PASS,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
}
