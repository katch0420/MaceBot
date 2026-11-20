package net.katch0420.macebot.client.inputs;

//? if <=1.21.11 {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?} else
//import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.katch0420.macebot.client.MaceBotClient;
import net.katch0420.macebot.client.gui.bodies.Bodies;
import net.katch0420.macebot.client.gui.bodies.Body;
import net.katch0420.macebot.main.messenger.ClientChatMessageManager;
import net.katch0420.macebot.main.messenger.Messages;
import net.katch0420.macebot.main.settings.Settings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.katch0420.macebot.main.settings.Settings.hasAccess;

public class MaceBotKeyBinds {

    public static KeyMapping openControl;
    public static KeyMapping openSettings;
    public static KeyMapping openKits;
    public static KeyMapping toggleCombatHitBox;
    public static KeyMapping toggleClient;

    public static Minecraft client(){return Minecraft.getInstance();}

    public static void register() {
        //? if >=1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("bot","general"));
        //?} else
        //String category = "key.category.bot.general";
        //? if <=1.21.11 {
        openControl = KeyBindingHelper.registerKeyBinding( new KeyMapping("key.gui.control", 80, category));
        openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.gui.settings", 79, category));
        openKits = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.gui.kits", 75, category));
        toggleCombatHitBox = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.toggle.hitbox", 72, category));
        toggleClient = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.toggle.client", 74, category));
        //?} else {
        /*openControl = KeyMappingHelper.registerKeyMapping( new KeyMapping("key.gui.control", 80, category));
        openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.gui.settings", 79, category));
        openKits = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.gui.kits", 75, category));
        toggleCombatHitBox = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toggle.hitbox", 72, category));
        toggleClient = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toggle.client", 74, category));
        *///?}
    }

    public static void processKeybinds() {
        //? if <26.2 {
        if (client().screen == null) {
        //?} else
        //if (client().gui.screen() == null) {
            while (openControl.consumeClick()) openBody(Bodies.CONTROLLER);
            while (openSettings.consumeClick()) openBody(Bodies.SETTINGS);
            while (openKits.consumeClick()) openBody(Bodies.KITS);
            while (toggleClient.consumeClick()) {
                if(hasConnected()) {
                    Settings.CLIENT.toggleAndGet();
                    ClientChatMessageManager.info(Component.literal("MaceBot Client has been " + (Settings.CLIENT.getValue() ? "Enabled." :"Disabled.")));
                }
            }
            if(Settings.CLIENT.getValue()) while (toggleCombatHitBox.consumeClick()) {
                if(hasConnected()){
                    Settings.COMBAT_HITBOX.toggleAndGet();
                }
            }
        }
    }

    private static void openBody(Body b){
        if(!hasAccess(true) && hasConnected()) {
            return;
        }
        setScreen(MaceBotClient.mainFrame.setInitialBody(b));
    }

    private static void setScreen(Screen screen){
        //? if <26.2 {
        Minecraft.getInstance().setScreen(screen);
        //?} else
        //Minecraft.getInstance().setScreenAndShow(screen);
    }

    private static boolean hasConnected() {
        if(Settings.CLIENT.getValue()) return true;
        else ClientChatMessageManager.warn(Messages.W_NO_CONNECTION);
        return false;
    }
}
