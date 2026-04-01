package net.katch0420.macebot.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.katch0420.macebot.client.gui.widgets.buttons.TexturedButtonWidget;
import net.katch0420.macebot.client.gui.widgets.core.ChildWidget;
import net.katch0420.macebot.client.gui.widgets.layout.GridLayout;
import net.katch0420.macebot.client.gui.widgets.panels.PanelWidget;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.main.kits.client.gui.KitsScreen;
import net.katch0420.macebot.main.networking.packets.c2s.ControlCommandC2SPacket;
import net.katch0420.macebot.main.settings.client.ClientSideSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ControlPanelScreen extends Screen {

    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private final Identifier MACEBOT_SPAWN_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_spawn.png");
    private final Identifier MACEBOT_SPAWN_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_spawn_disabled.png");
    private final Identifier MACEBOT_DESPAWN_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_despawn.png");
    private final Identifier MACEBOT_DESPAWN_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_despawn_disabled.png");
    private final Identifier MACEBOT_KITS_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_kits.png");
    private final Identifier MACEBOT_KITS_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_kits_disabled.png");
    private final Identifier MACEBOT_START_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_start.png");
    private final Identifier MACEBOT_START_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_start_disabled.png");
    private final Identifier MACEBOT_PAUSE_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_pause.png");
    private final Identifier MACEBOT_PAUSE_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_pause_disabled.png");
    private final Identifier MACEBOT_SETTINGS_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_settings.png");
    private final Identifier MACEBOT_SETTINGS_DISABLED_TEXTURE = Identifier.of("macebot","textures/gui/control_panel/buttons/macebot_settings_disabled.png");

    PanelWidget gridPanel;

    private final ChildWidget[][] gridButtons = {
            {
                    TexturedButtonWidget.builder()
                            .onClick(b -> executeCtrlCommand(ControlCommandC2SPacket.ControlCommands.MACEBOT_START))
                            .texture(MACEBOT_START_TEXTURE)
                            .textureDisabled(MACEBOT_START_DISABLED_TEXTURE)
                            .disableFunction(() -> !ClientSideSettings.isMacebotPaused())
                            .label("Start")
                            .labelMargin(5)
                            .tooltip(Text.of("Start the bot"))
                            .build(),

                    TexturedButtonWidget.builder()
                            .onClick(b -> executeCtrlCommand(ControlCommandC2SPacket.ControlCommands.MACEBOT_PAUSE))
                            .texture(MACEBOT_PAUSE_TEXTURE)
                            .textureDisabled(MACEBOT_PAUSE_DISABLED_TEXTURE)
                            .disableFunction(ClientSideSettings::isMacebotPaused)
                            .label("Pause")
                            .labelMargin(5)
                            .tooltip(Text.of("Pause the bot"))
                            .build(),


                    TexturedButtonWidget.builder()
                            .onClick(b -> client.setScreen(new KitsScreen(this)))
                            .texture(MACEBOT_KITS_TEXTURE)
                            .textureDisabled(MACEBOT_KITS_DISABLED_TEXTURE)
                            .disableFunction(() -> false)
                            .label("Kits")
                            .labelMargin(5)
                            .tooltip(Text.of("Build it Kits"))
                            .build()
            },
            {
                    TexturedButtonWidget.builder()
                            .onClick(b -> executeCtrlCommand(ControlCommandC2SPacket.ControlCommands.MACEBOT_SPAWN))
                            .texture(MACEBOT_SPAWN_TEXTURE)
                            .textureDisabled(MACEBOT_SPAWN_DISABLED_TEXTURE)
                            .disableFunction(ClientSideSettings::isMacebotOnline)
                            .label("Spawn")
                            .labelMargin(5)
                            .tooltip(Text.of("Spawn the bot"))
                            .build(),

                    TexturedButtonWidget.builder()
                            .onClick(b -> executeCtrlCommand(ControlCommandC2SPacket.ControlCommands.MACEBOT_DESPAWN))
                            .texture(MACEBOT_DESPAWN_TEXTURE)
                            .textureDisabled(MACEBOT_DESPAWN_DISABLED_TEXTURE)
                            .disableFunction(() -> !ClientSideSettings.isMacebotOnline())
                            .label("Despawn")
                            .labelMargin(5)
                            .tooltip(Text.of("Despawn the bot"))
                            .build(),

                    TexturedButtonWidget.builder()
                            .onClick(b -> openSubScreen(new OptionScreen(this)))
                            .texture(MACEBOT_SETTINGS_TEXTURE)
                            .textureDisabled(MACEBOT_SETTINGS_DISABLED_TEXTURE)
                            .disableFunction(() -> false)
                            .label("Options")
                            .labelMargin(5)
                            .tooltip(Text.of("Configuration of MaceBot"))
                            .build()
            }
    };
    public static boolean justClosed;

    public ControlPanelScreen(Text title) {
        super(title);
    }

    private static final int MIN_GRID_WIDTH = 90;   // scaled units
    private static final int MIN_GRID_HEIGHT = 60;   // scaled units
    private static final int MAX_GRID_WIDTH = 360;   // scaled units
    private static final int MAX_GRID_HEIGHT = 200;  // scaled units

    @Override
    protected void init() {
        gridPanel = PanelWidget.builder()
                .color(0x50000000)
                .layout(new GridLayout(gridButtons, 5))
                .build();
        addDrawableChild(gridPanel);
        applyGridBounds(getWindowWidth(), getWindowHeight());
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        applyGridBounds(width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if(keyCode == KeyBindingHelper.getBoundKeyOf(MaceBotKeyBinds.openOptionsGui).getCode()){
            close();
            justClosed = true;
            return true;
        }
        return false;
    }

    private void applyGridBounds(int scaledWidth, int scaledHeight) {
        // target size relative to window
        int targetWidth = scaledWidth * 19 / 20;
        int targetHeight = scaledHeight * 19 / 20;

        // clamp to min/max
        int clampedWidth = Math.max(MIN_GRID_WIDTH, Math.min(MAX_GRID_WIDTH, targetWidth));
        int clampedHeight = Math.max(MIN_GRID_HEIGHT, Math.min(MAX_GRID_HEIGHT, targetHeight));

        // center the grid in scaled coordinates
        int xPos = (scaledWidth - clampedWidth) / 2;
        int yPos = (scaledHeight - clampedHeight) / 2;

        gridPanel.setSize(clampedWidth, clampedHeight);
        gridPanel.setRelativePos(xPos, yPos);
    }


    protected void executeCtrlCommand(ControlCommandC2SPacket.ControlCommands command){
        if(command == ControlCommandC2SPacket.ControlCommands.MACEBOT_SPAWN){
            ClientSideSettings.setMacebotOnline(true);
        } else if (command == ControlCommandC2SPacket.ControlCommands.MACEBOT_DESPAWN){
            ClientSideSettings.setMacebotOnline(false);
        }
        ClientPlayNetworking.send(new ControlCommandC2SPacket(command));
    }

    protected void openSubScreen(Screen screen){
        assert minecraftClient.currentScreen != null;
        minecraftClient.currentScreen.close();
        minecraftClient.setScreen(screen);
    }

    private int getWindowWidth() {
        return minecraftClient.getWindow().getScaledWidth();
    }

    private int getWindowHeight() {
        return minecraftClient.getWindow().getScaledHeight();
    }

    @Override
    public void tick() {
        gridPanel.getChildren().forEach(
                c -> {
                    if(c instanceof TexturedButtonWidget t){
                        t.updateDisableStatus();
                    }
                }
        );
    }
}
