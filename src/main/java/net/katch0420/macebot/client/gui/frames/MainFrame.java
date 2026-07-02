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
import net.katch0420.macebot.client.utils.RenderUtils;
import net.katch0420.macebot.main.kits.main.Kit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Root screen of the MaceBot GUI: a header bar, a left icon sidebar, a footer,
 * and a swappable body panel in the middle.
 *
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
    Text statusText;

    private final List<SidebarEntry> sidebarEntries = new ArrayList<>();

    public Theme theme = Themes.CURRENT;

    public MainFrame() {
        super(Text.of("Macebot"));
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
        addSelectableChild(currentBody);
    }

    private void initSideBar() {
        sidebarEntries.clear();
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_PANEL, Text.of("Controller"), Bodies.CONTROLLER));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_KITS, Text.of("Kits"), Bodies.KITS));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_SETTINGS, Text.of("Settings"), Bodies.SETTINGS));
        sidebarEntries.add(new SidebarEntry(MaceBotTextures.SIDE_PANEL_ICONS_INFO, Text.of("About"), Bodies.ABOUT));

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
            addDrawableChild(entry.button);
        }

        int exitSize;
        exitSize = iconSize;
        addDrawableChild(Button.builder()
                .texture(MaceBotTextures.SIDE_PANEL_ICONS_EXIT)
                .size(exitSize)
                .backgroundColor(0)
                .foregroundColor(theme.screen_foreground())
                .hoverColor(theme.danger() & 0x55FFFFFF)
                .holdColor(theme.danger() & 0x77FFFFFF)
                .onClick(b -> goBack())
                .position(sX, height - (exitSize + FOOTER_HEIGHT + gap))
                .build());
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float d) {
        // Custom background is painted in render(); the vanilla dirt/blur backdrop is skipped.
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        if(prevH != height || prevW != width) { //Ensures proper scaling.
            clearAndInit();
            return;
        }
        c.fill(0, 0, width, height, theme.screen_background());
        currentBody.render(c,mx,my,d);
        renderSideBar(c, mx, my);
        renderHeader(c, mx, my);
        renderFooter(c, mx, my);
        super.render(c, mx, my, d);
        renderStatusMessage(c,mx,my);
    }

    private void renderStatusMessage(DrawContext c, int mx, int my) {
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
        int sy = height - FOOTER_HEIGHT - 2 * textRenderer.fontHeight - 10;
        int sWidth = textRenderer.getWidth(statusText) + 10;
        int sx = (width - sWidth) / 2;

        // Colors with alpha applied
        int bColor = theme.body_label() & 0x00FFFFFF | (alpha << 24);
        int bgColor = theme.body_background() & 0x00FFFFFF | (alpha << 24);
        int tColor = Objects.requireNonNull(statusText.getStyle().getColor()).getRgb() & 0x00FFFFFF | (alpha << 24);

        // Draw background box
        c.fill(sx, sy, sx + sWidth, sy + textRenderer.fontHeight + 6, bgColor);

        // Draw border
        RenderUtils.drawBorder(c, sx, sy, sWidth, textRenderer.fontHeight + 6, bColor);

        // Draw text
        c.drawTextWithShadow(textRenderer, statusText, sx + 5, sy + 3, tColor);
    }


    private void renderFooter(DrawContext c, int mx, int my) {
        c.fill(0, height - FOOTER_HEIGHT, width, height, theme.footer_background());
        c.drawHorizontalLine(0, width, height - FOOTER_HEIGHT, theme.panel_separator());

        // Subtle status line - mirrors a "professional app" footer (version / status), themed.
        String status = "MaceBot";
        int textY = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - textRenderer.fontHeight) / 2;
        c.drawTextWithShadow(textRenderer, Text.of(status),  8, textY, theme.footer_foreground());

        // Accent-colored connection/status dot - purely cosmetic placeholder for a real status hook.
        int dotSize = Math.max(4, FOOTER_HEIGHT / 4);
        int dotX = width - dotSize - 10;
        int dotY = height - FOOTER_HEIGHT / 2 - dotSize / 2;
        c.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, theme.success());
    }

    private void renderSideBar(DrawContext c, int mx, int my) {
        c.fill(0, 0, SIDEBAR_WIDTH, height, theme.sidebar_background());
        c.drawVerticalLine(SIDEBAR_WIDTH, 0, height, theme.panel_separator());

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

    private void renderHeader(DrawContext c, int mx, int my) {
        c.fill(0, 0, width, HEADER_HEIGHT, theme.header_background());
        c.drawHorizontalLine(0, width, HEADER_HEIGHT, theme.panel_separator());
        c.fill(0, HEADER_HEIGHT - 2, width, HEADER_HEIGHT, theme.accent() & 0x40FFFFFF);

        Text t = currentBody == null ? Text.of("Macebot") : currentBody.getLabel();
        c.drawTextWithShadow(textRenderer, t, SIDEBAR_WIDTH + 10, (HEADER_HEIGHT - textRenderer.fontHeight) / 2, theme.header_foreground());

        // Breadcrumb-style back affordance when history has depth.
        if (history.size() > 1) {
            String back = "< Back";
            int bw = textRenderer.getWidth(back);
            int bx = width - bw - 12;
            int by = (HEADER_HEIGHT - textRenderer.fontHeight) / 2;
            boolean hovered = mx >= bx - 4 && mx <= bx + bw + 4 && my >= 0 && my <= HEADER_HEIGHT;
            c.drawTextWithShadow(textRenderer, Text.of(back), bx, by, hovered ? theme.accent_hover() : theme.accent());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY <= HEADER_HEIGHT && history.size() > 1) {
            String back = "< Back";
            int bw = textRenderer.getWidth(back);
            int bx = width - bw - 12;
            if (mouseX >= bx - 4 && mouseX <= bx + bw + 4) {
                goBack();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void clearAndInit() {
        history.clear();
        super.clearAndInit();
    }

    private void setBody(Body body) {
        if (currentBody == body || body == null) return;

        int existingIndex = history.indexOf(body);
        if (existingIndex != -1) {
            history.subList(existingIndex + 1, history.size()).clear();
        } else {
            history.add(body);
        }
        if(currentBody != null){
            currentBody.removed();
            remove(currentBody);
        }
        currentBody = body.setParentScreen(this);
        currentBody.setPosition(SIDEBAR_WIDTH, HEADER_HEIGHT);
        currentBody.setSize(width - SIDEBAR_WIDTH, height - HEADER_HEIGHT - FOOTER_HEIGHT);
        currentBody.clearAndInit();
        currentBody.parent = this;
        addSelectableChild(currentBody);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (KeyPressHandler.isCloseKey(k)) return goBack();
        return currentBody != null && currentBody.keyPressed(k, s, m);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private boolean goBack() {
        if (Screen.hasControlDown() || history.size() <= 1) {
            close();
        } else {
            history.removeLast();
            setBody(history.getLast());
        }
        return true;
    }

    /**
     * Returns the body's available drawing area at the CURRENT resolution/scale.
     * Kept for API compatibility with bodies that may still reference it, but
     * {@link Body} now reads its own
     * width/height directly instead of going through fixed scale buckets.
     */
    public Dimension getBodyDimensionInScale(int scale) {
        return new Dimension(width - SIDEBAR_WIDTH, height - HEADER_HEIGHT - FOOTER_HEIGHT);
    }

    public @NotNull MinecraftClient getClient() {
        assert client != null;
        return client;
    }

    public void navigateTo(Body body) {
        setBody(body);
    }

    public void applyTheme() {
        clearAndInit();
    }

    @Override
    public void tick() {
        for(Element e: children()){
            if(e instanceof BaseWidget<?> b) b.tick();
        }
    }

    @Override
    public void removed() {
        if(currentBody != null)currentBody.removed();
    }

    public void showWarningStatus(MutableText status) {
        statusStartMillis = System.currentTimeMillis();
        statusText = status.formatted(Formatting.YELLOW);
    }

    public void navigateBack() {
        goBack();
    }

    public void openKitEdior(Kit kit) {
        setBody(new KitEditorBody(kit));
    }

    private static final class SidebarEntry {
        final Identifier texture;
        final Text label;
        final Body body;
        Button button;

        SidebarEntry(Identifier texture, Text label, Body body) {
            this.texture = texture;
            this.label = label;
            this.body = body;
        }
    }
}
