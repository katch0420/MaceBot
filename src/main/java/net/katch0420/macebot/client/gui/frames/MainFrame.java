package net.katch0420.macebot.client.gui.frames;

import net.katch0420.macebot.client.gui.MaceBotTextures;
import net.katch0420.macebot.client.gui.bodies.Bodies;
import net.katch0420.macebot.client.gui.bodies.Body;
import net.katch0420.macebot.client.gui.bodies.KitEditorBody;
import net.katch0420.macebot.client.gui.layout.ResponsiveLayout;
import net.katch0420.macebot.client.gui.themes.Theme;
import net.katch0420.macebot.client.gui.themes.Themes;
import net.katch0420.macebot.client.gui.widgets.buttons.Button;
import net.katch0420.macebot.client.gui.widgets.core.BaseWidget;
import net.katch0420.macebot.client.inputs.KeyPressHandler;
import net.katch0420.macebot.client.inputs.MaceBotKeyBinds;
import net.katch0420.macebot.client.utils.RenderUtils;
import net.katch0420.macebot.main.kits.main.Kit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
//? if <26.1.2 {
import net.minecraft.client.gui.GuiGraphics;
//?} else
//import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Root screen of the MaceBot GUI: a header bar, a left icon sidebar, a footer,
 * and a swappable body panel in the middle.
 * <p>
 * Layout is fully responsive: header/sidebar/footer thickness and all
 * internal spacing scale smoothly with the real available screen size
 * (which already reflects the player's GUI scale setting), instead of
 * using fixed pixel constants that only looked right at one scale.
 */
public class MainFrame extends Screen {

    // Base ("design reference") sizes - actual on-screen sizes are derived
    // from these via ResponsiveLayout.scaled(...) in initMetrics().
    private static final int BASE_HEADER_HEIGHT = 26;
    private static final int BASE_SIDEBAR_WIDTH = 26;
    private static final int BASE_FOOTER_HEIGHT = 22;
    private static final int BASE_ICON_SIZE = 20;
    private static final int BASE_ICON_GAP = 6;

    private float uiScale = 1.0f;
    private int HEADER_HEIGHT;
    private int SIDEBAR_WIDTH;
    private int FOOTER_HEIGHT;

    private Body currentBody;
    protected final List<Body> history = new ArrayList<>();

    long statusStartMillis;
    Component statusText;

    private final List<SidebarEntry> sidebarEntries = new ArrayList<>();

    public Theme theme = Themes.CURRENT;

    public MainFrame() {
        super(Component.nullToEmpty("Macebot"));
    }

    @Override
    protected void init() {
        theme = Themes.CURRENT;
        initMetrics();
        initSideBar();
        initBody();
    }

    /** Computes header/sidebar/footer thickness from the current responsive scale. */
    int prevW,prevH;
    private void initMetrics() {
        prevW = width;
        prevH = height;
        uiScale = ResponsiveLayout.scaleFactor(this.width);
        HEADER_HEIGHT = ResponsiveLayout.scaledMin(BASE_HEADER_HEIGHT, uiScale, 18);
        SIDEBAR_WIDTH = ResponsiveLayout.scaledMin(BASE_SIDEBAR_WIDTH, uiScale, 20);
        FOOTER_HEIGHT = ResponsiveLayout.scaledMin(BASE_FOOTER_HEIGHT, uiScale, 16);
    }

    private void initBody() {
        if (currentBody == null) setBody(Bodies.CONTROLLER);
        currentBody.setParentScreen(this);
        currentBody.setPosition(SIDEBAR_WIDTH, HEADER_HEIGHT);
        currentBody.setSize(width - SIDEBAR_WIDTH, height - HEADER_HEIGHT - FOOTER_HEIGHT);
        currentBody.clearAndInit();
        addWidget(currentBody);
    }

    private void initSideBar() {
        sidebarEntries.clear();
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_PANEL, Component.nullToEmpty("Controller"), Bodies.CONTROLLER));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_KITS, Component.nullToEmpty("Kits"), Bodies.KITS));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_SETTINGS, Component.nullToEmpty("SettingManager"), Bodies.SETTINGS));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_INFO, Component.nullToEmpty("About"), Bodies.ABOUT));

        int iconSize = ResponsiveLayout.scaledMin(BASE_ICON_SIZE, uiScale, 16);
        int gap = ResponsiveLayout.scaledMin(BASE_ICON_GAP, uiScale, 4);
        int sX = (SIDEBAR_WIDTH - iconSize) / 2;
        int blockHeight = sidebarEntries.size() * (iconSize + gap) - gap;
        int sY = Math.max(8, (height - FOOTER_HEIGHT - HEADER_HEIGHT) / 2 - blockHeight / 2 + HEADER_HEIGHT);

        AtomicInteger c = new AtomicInteger();
        for (SidebarEntry entry : sidebarEntries) {
            int y = sY + c.getAndIncrement() * (iconSize + gap);
            entry.button = Button.builder()
                    .texture(entry.texture)
                    .size(iconSize)
                    .position(sX, y)
                    .backgroundColor(0)
                    .hoverColor(theme.accent_hover() & 0x55FFFFFF)
                    .holdColor(theme.accent() & 0x77FFFFFF)
                    .onClick(b -> setBody(entry.body))
                    .build();
            addRenderableWidget(entry.button);
        }

        int exitSize;
        exitSize = iconSize;
        addRenderableWidget(Button.builder()
                .texture(MaceBotTextures.SIDE_PANEL_ICONS_EXIT)
                .size(exitSize)
                .backgroundColor(0)
                .foregroundColor(theme.screen_foreground())
                .hoverColor(theme.danger() & 0x55FFFFFF)
                .holdColor(theme.danger() & 0x77FFFFFF)
                .onClick(b -> goBack(
                        //? if >=1.21.9
                        b.ctrl
                ))
                .position(sX, height - (exitSize + FOOTER_HEIGHT + gap))
                .build());
    }
    //? if >1.21.11 {
    /*@Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float d) {
        if(prevH != height || prevW != width) { //Ensures proper scaling.
            repositionElements();
            return;
        }
        c.fill(0, 0, width, height, theme.screen_background());
        currentBody.extractRenderState(c,mx,my,d);
        extractSideBar(c);
        extractHeader(c, mx, my);
        extractFooter(c);
        super.extractRenderState(c, mx, my, d);
        extractStatusMessage(c);
    }

    private void extractStatusMessage(GuiGraphicsExtractor c) {
        if (statusText == null) return;

        long now = System.currentTimeMillis();
        long elapsed = now - statusStartMillis; // when you set the message, store statusStartMillis
        long duration = 4000; // total 4 seconds
        long fadeStart = 3000; // start fading after 3 seconds
        long fadeDuration = 1000; // fade lasts 1 second

        if (elapsed >= duration) {
            statusText = null; // expired
            return;
        }

        // Compute alpha
        int alpha = 255;
        if (elapsed > fadeStart) {
            float progress = (float)(elapsed - fadeStart) / fadeDuration;
            alpha = (int)(255 * (1.0f - progress));
        }

        // Positioning
        int sy = height - FOOTER_HEIGHT - 2 * font.lineHeight - 10;
        int sWidth = font.width(statusText) + 10;
        int sx = (width - sWidth) / 2;

        // Colors with alpha applied
        int bColor = theme.body_label() & 0x00FFFFFF | (alpha << 24);
        int bgColor = theme.body_background() & 0x00FFFFFF | (alpha << 24);
        int tColor = Objects.requireNonNull(statusText.getStyle().getColor()).getValue() & 0x00FFFFFF | (alpha << 24);

        // Draw background box
        c.fill(sx, sy, sx + sWidth, sy + font.lineHeight + 6, bgColor);

        // Draw border
        RenderUtils.drawBorder(c, sx, sy, sWidth, font.lineHeight + 6, bColor);

        // Draw text
        c.text(font, statusText, sx + 5, sy + 3, tColor);
    }

    private void extractFooter(GuiGraphicsExtractor c) {
        c.fill(0, height - FOOTER_HEIGHT, width, height, theme.footer_background());
        c.horizontalLine(0, width, height - FOOTER_HEIGHT, theme.panel_separator());

        // Subtle status line - mirrors a "professional app" footer (version / status), themed.
        String status = "MaceBot";
        int textY = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - font.lineHeight) / 2;
        c.text(font, status,  8, textY, theme.footer_foreground());
        // Accent-colored connection/status dot - purely cosmetic placeholder for a real status hook.
        int dotSize = Math.max(4, FOOTER_HEIGHT / 4);
        int dotX = width - dotSize - 10;
        int dotY = height - FOOTER_HEIGHT / 2 - dotSize / 2;
        c.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, theme.success());
    }

    private void extractHeader(GuiGraphicsExtractor c, int mx, int my) {c.fill(0, 0, width, HEADER_HEIGHT, theme.header_background());
        c.horizontalLine(0, width, HEADER_HEIGHT, theme.panel_separator());
        c.fill(0, HEADER_HEIGHT - 2, width, HEADER_HEIGHT, theme.accent() & 0x40FFFFFF);

        Component t = currentBody == null ? Component.literal("Macebot") : currentBody.getLabel();
        c.text(font, t, SIDEBAR_WIDTH + 10, (HEADER_HEIGHT - font.lineHeight) / 2, theme.header_foreground());
        // Breadcrumb-style back affordance when history has depth.
        if (history.size() > 1) {
            String back = "< Back";
            int bw = font.width(back);
            int bx = width - bw - 12;
            int by = (HEADER_HEIGHT - font.lineHeight) / 2;
            boolean hovered = mx >= bx - 4 && mx <= bx + bw + 4 && my >= 0 && my <= HEADER_HEIGHT;
            c.text(font, back, bx, by, hovered ? theme.accent_hover() : theme.accent());
        }
    }

    private void extractSideBar(GuiGraphicsExtractor c) {c.fill(0, 0, SIDEBAR_WIDTH, height, theme.sidebar_background());
        c.verticalLine(SIDEBAR_WIDTH, 0, height, theme.panel_separator());

        // Highlight the icon for the body currently being shown, so the sidebar
        // reads like a proper navigation rail instead of a row of inert icons.
        for (SidebarEntry entry : sidebarEntries) {
            if (entry.body == currentBody && entry.button != null) {
                int bx = entry.button.getX();
                int by = entry.button.getY();
                int bw = entry.button.getWidth();
                int bh = entry.button.getHeight();
                c.fill(0, by - 2, 3, by + bh + 2, theme.accent());
                c.fill(bx - 2, by - 2, bx + bw + 2, by + bh + 2, theme.accent() & 0x33FFFFFF);
            }
        }
    }
    *///?}

    //? if <=1.21.11 {
    @Override
    public void renderBackground(GuiGraphics c, int mx, int my, float d) {
        // Custom background is painted in render(); the vanilla dirt/blur backdrop is skipped.
    }

    @Override
    public void render(GuiGraphics c, int mx, int my, float d) {
        if(prevH != height || prevW != width) { //Ensures proper scaling.
            repositionElements();
            return;
        }
        c.fill(0, 0, width, height, theme.screen_background());
        currentBody.render(c,mx,my,d);
        renderSideBar(c);
        renderHeader(c, mx, my);
        renderFooter(c);
        super.render(c, mx, my, d);
        renderStatusMessage(c);
    }

    private void renderStatusMessage(GuiGraphics c) {
        if (statusText == null) return;

        long now = System.currentTimeMillis();
        long elapsed = now - statusStartMillis; // when you set the message, store statusStartMillis
        long duration = 4000; // total 4 seconds
        long fadeStart = 3000; // start fading after 3 seconds
        long fadeDuration = 1000; // fade lasts 1 second

        if (elapsed >= duration) {
            statusText = null; // expired
            return;
        }

        // Compute alpha
        int alpha = 255;
        if (elapsed > fadeStart) {
            float progress = (float)(elapsed - fadeStart) / fadeDuration;
            alpha = (int)(255 * (1.0f - progress));
        }

        // Positioning
        int sy = height - FOOTER_HEIGHT - 2 * font.lineHeight - 10;
        int sWidth = font.width(statusText) + 10;
        int sx = (width - sWidth) / 2;

        // Colors with alpha applied
        int bColor = theme.body_label() & 0x00FFFFFF | (alpha << 24);
        int bgColor = theme.body_background() & 0x00FFFFFF | (alpha << 24);
        int tColor = Objects.requireNonNull(statusText.getStyle().getColor()).getValue() & 0x00FFFFFF | (alpha << 24);

        // Draw background box
        c.fill(sx, sy, sx + sWidth, sy + font.lineHeight + 6, bgColor);

        // Draw border
        RenderUtils.drawBorder(c, sx, sy, sWidth, font.lineHeight + 6, bColor);

        // Draw text
        c.drawString(font, statusText, sx + 5, sy + 3, tColor);
    }


    private void renderFooter(@UnknownNullability GuiGraphics c) {
        c.fill(0, height - FOOTER_HEIGHT, width, height, theme.footer_background());
        c.hLine(0, width, height - FOOTER_HEIGHT, theme.panel_separator());

        // Subtle status line - mirrors a "professional app" footer (version / status), themed.
        String status = "MaceBot";
        int textY = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - font.lineHeight) / 2;
        c.drawString(font, status,  8, textY, theme.footer_foreground());
        // Accent-colored connection/status dot - purely cosmetic placeholder for a real status hook.
        int dotSize = Math.max(4, FOOTER_HEIGHT / 4);
        int dotX = width - dotSize - 10;
        int dotY = height - FOOTER_HEIGHT / 2 - dotSize / 2;
        c.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, theme.success());
    }

    private void renderSideBar(@UnknownNullability GuiGraphics c) {
        c.fill(0, 0, SIDEBAR_WIDTH, height, theme.sidebar_background());
        c.vLine(SIDEBAR_WIDTH, 0, height, theme.panel_separator());

        // Highlight the icon for the body currently being shown, so the sidebar
        // reads like a proper navigation rail instead of a row of inert icons.
        for (SidebarEntry entry : sidebarEntries) {
            if (entry.body == currentBody && entry.button != null) {
                int bx = entry.button.getX();
                int by = entry.button.getY();
                int bw = entry.button.getWidth();
                int bh = entry.button.getHeight();
                c.fill(0, by - 2, 3, by + bh + 2, theme.accent());
                c.fill(bx - 2, by - 2, bx + bw + 2, by + bh + 2, theme.accent() & 0x33FFFFFF);
            }
        }
    }

    private void renderHeader(@UnknownNullability GuiGraphics c, int mx, int my) {
        c.fill(0, 0, width, HEADER_HEIGHT, theme.header_background());
        c.hLine(0, width, HEADER_HEIGHT, theme.panel_separator());
        c.fill(0, HEADER_HEIGHT - 2, width, HEADER_HEIGHT, theme.accent() & 0x40FFFFFF);

        Component t = currentBody == null ? Component.literal("Macebot") : currentBody.getLabel();
        c.drawString(font, t, SIDEBAR_WIDTH + 10, (HEADER_HEIGHT - font.lineHeight) / 2, theme.header_foreground());
        // Breadcrumb-style back affordance when history has depth.
        if (history.size() > 1) {
            String back = "< Back";
            int bw = font.width(back);
            int bx = width - bw - 12;
            int by = (HEADER_HEIGHT - font.lineHeight) / 2;
            boolean hovered = mx >= bx - 4 && mx <= bx + bw + 4 && my >= 0 && my <= HEADER_HEIGHT;
            c.drawString(font, back, bx, by, hovered ? theme.accent_hover() : theme.accent());
        }
    }
    //?}
    @Override
    public boolean mouseClicked(
            //? if >=1.21.9 {
            MouseButtonEvent c, boolean bl
            //?} else
            //double mx, double my, int b
    ) {
        //? if >=1.21.9
        double mx = c.x(), my = c.y(); int b = c.button();
        if (b == 0 && my <= HEADER_HEIGHT && history.size() > 1) {
            String back = "< Back";
            int bw = font.width(back);
            int bx = width - bw - 12;
            if (mx >= bx - 4 && mx <= bx + bw + 4) {
                goBack(
                        //? if >=1.21.9
                        c.hasControlDown()
                );
                return true;
            }
        }
        return super.mouseClicked(
                //? if >=1.21.9 {
                c,bl
                //?} else
                //mx, my, b
        );
    }

    @Override
    protected void repositionElements() {
        history.clear();
        super.repositionElements();
    }

    @SuppressWarnings("unchecked")
    public <E extends Body> E setBody(E body) {
        if (currentBody == body || body == null) return (E) currentBody;

        int existingIndex = history.indexOf(body);
        if (existingIndex != -1) {
            history.subList(existingIndex + 1, history.size()).clear();
        } else {
            history.add(body);
        }
        if(currentBody != null){
            currentBody.removed();
            removeWidget(currentBody);
        }
        currentBody = body.setParentScreen(this);
        currentBody.setPosition(SIDEBAR_WIDTH, HEADER_HEIGHT);
        currentBody.setSize(width - SIDEBAR_WIDTH, height - HEADER_HEIGHT - FOOTER_HEIGHT);
        currentBody.clearAndInit();
        currentBody.parent = this;
        addRenderableWidget(currentBody);
        return (E) currentBody;
    }

    @Override
    public boolean keyPressed(
            //? if >=1.21.9 {
            KeyEvent keyEvent
            //?} else
            //int keyCode, int scanCode, int modifiers
    ) {
        //? if >=1.21.9
        int keyCode = keyEvent.key(), scanCode = keyEvent.scancode(), modifiers = keyEvent.modifiers();
        if(KeyPressHandler.canKeyBindClose(keyCode, scanCode, modifiers)) {
            onClose();
            return true;
        }
        if(getCurrentBody().isPrimary()){
            MaceBotKeyBinds.processKeybinds();
        }
        if(KeyPressHandler.isCloseKey(keyCode, scanCode, modifiers)) return goBack(
                //? >=1.21.9
                keyEvent.hasControlDown()
        );
        return currentBody != null && super.keyPressed(
                //? if >=1.21.9 {
                keyEvent
                //?} else
                //keyCode, scanCode, modifiers
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    //? if <1.21.9 {
    /*private boolean goBack(){
        return goBack(Screen.hasControlDown());
    }
    *///?}
    private boolean goBack(boolean ctrl) {
        if(currentBody instanceof KitEditorBody){
            setBody(Bodies.KITS);
        }
        if (ctrl || history.size() <= 1) {
            onClose();
        } else {
            history.removeLast();
            setBody(history.getLast());
        }
        return true;
    }

    public @NotNull Minecraft getClient() {
        assert minecraft != null;
        return minecraft;
    }

    public void navigateTo(Body body) {
        setBody(body);
    }

    public void applyTheme() {
        repositionElements();
    }

    @Override
    public void tick() {
        for(GuiEventListener e: children()){
            if(e instanceof BaseWidget<?> b) b.tick();
        }
    }

    @Override
    public void removed() {
        if(currentBody != null)currentBody.removed();
    }

    public void showWarningStatus(MutableComponent status) {
        statusStartMillis = System.currentTimeMillis();
        statusText = status.withStyle(ChatFormatting.YELLOW);
    }

    public void openKitEditor(Kit kit) {
        setBody(new KitEditorBody(kit));
    }

    public void showInfoStatus(MutableComponent status) {
        statusStartMillis = System.currentTimeMillis();
        statusText = status.withStyle(ChatFormatting.GREEN);
    }

    public Screen setInitialBody(Body body) {
        this.history.clear();
        this.setBody(body);
        return this;
    }

    public Body getCurrentBody(){
        return currentBody;
    }

    private static final class SidebarEntry {
        final Identifier texture;
        final Component label;
        final Body body;
        Button button;

        SidebarEntry(Identifier texture, Component label, Body body) {
            this.texture = texture;
            this.label = label;
            this.body = body;
        }
    }
}
