package net.katch0420.macebot.client.inputs;

//? if <=1.21.11
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.bodies.Bodies;
//? if >1.21.11
//import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

//? if <=1.21.11 {
public class KeyPressHandler {
    public static boolean isCloseKey(int k, int scanCode, int modifiers){
        return k == GLFW.GLFW_KEY_ESCAPE
                || k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openControl).getValue()
                || k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openSettings).getValue()
                || k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openKits).getValue();
    }

    public static boolean canKeyBindClose(int k, int scanCode, int modifiers){
        return (k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openControl).getValue() && MaceBotClient.mainFrame.getCurrentBody() == Bodies.CONTROLLER)
                || (k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openSettings).getValue() && MaceBotClient.mainFrame.getCurrentBody() == Bodies.SETTINGS)
                || (k == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openKits).getValue() && MaceBotClient.mainFrame.getCurrentBody() == Bodies.KITS);
    }
}
//?} else {
/*public class KeyPressHandler {
    public static boolean isCloseKey(int k, int s, int m){
        KeyEvent e = new KeyEvent(k, s,m);
        return k == GLFW.GLFW_KEY_ESCAPE
                || MaceBotKeyBinds.openControl.matches(e)
                || MaceBotKeyBinds.openSettings.matches(e)
                || MaceBotKeyBinds.openKits.matches(e);
    }

    public static boolean canKeyBindClose(int k, int s, int m){
        KeyEvent e = new KeyEvent(k, s,m);
        return (MaceBotKeyBinds.openControl.matches(e) && MaceBotClient.mainFrame.getCurrentBody() == Bodies.CONTROLLER)
                || (MaceBotKeyBinds.openSettings.matches(e) && MaceBotClient.mainFrame.getCurrentBody() == Bodies.SETTINGS)
                || (MaceBotKeyBinds.openKits.matches(e) && MaceBotClient.mainFrame.getCurrentBody() == Bodies.KITS);
    }
}
*///?}
