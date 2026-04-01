package net.katch0420.macebot.client.inputs;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MaceBotKeyBinds{

    public static KeyBinding openOptionsGui;

    public static void register(){
        openOptionsGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.category"
        ));
    }
}
