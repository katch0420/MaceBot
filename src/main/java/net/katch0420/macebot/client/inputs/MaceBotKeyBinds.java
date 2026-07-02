package net.katch0420.macebot.client.inputs;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.main.messenger.ModMessages;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static net.katch0420.macebot.main.settings.client.ClientSideSettings.hasAccess;

public class MaceBotKeyBinds {

    public static KeyBinding open;
    public static KeyBinding test;

    static MinecraftClient client = MinecraftClient.getInstance();

    public static void register() {
        open = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                //? if >=1.21.9 {
                /*new KeyBinding.Category(Identifier.of("macebot","key.category"))
                 *///?} else
                "key.category"));
        test = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.test", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category"));
    }

    public static void processKeybinds() {
        if (client.currentScreen == null) {
            while (open.wasPressed()) if(hasConnected())if (hasAccess()) client.setScreen(MaceBotClient.mainFrame);
        }
    }

    private static boolean hasConnected() {
        if(ClientSideSettings.isConnected()) return true;
        else MaceBotClient.getClientPlayer().sendMessage(ModMessages.WARNING.copy().append(ModMessages.CLIENT_WARN_CHAT_NOT_CONNECTED));
        return false;
    }
}
