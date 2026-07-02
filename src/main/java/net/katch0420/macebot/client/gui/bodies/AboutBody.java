package net.katch0420.macebot.client.gui.bodies;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.katch0420.macebot.main.MaceBot;
import net.katch0420.macebot.client.MaceBotClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.katch0420.macebot.client.MaceBotClient.theme;

/**
 * About body — mod description at the top, then a scrollable list of
 * collapsible guide sections (like HTML <details>/<summary>).
 *
 * ── Adding content ────────────────────────────────────────────────────────────
 * In buildContent():
 *   Section s = addSection("Title");          // collapsible header
 *   s.text("Some paragraph text here.");      // plain paragraph
 *   s.bullet("A bullet point.");              // • prefixed line
 *   s.note("A highlighted tip or warning.");  // accent-colored note
 *   s.image(Identifier.of("macebot","textures/guide/my_image.png"), 160, 80);
 *                                             // embedded image (w×h in px)
 *   s.spacer();                               // blank line gap
 *
 * Images are loaded from your mod's resource pack. If the texture doesn't
 * exist yet, the slot renders a placeholder box so layout still works.
 */
@Environment(EnvType.CLIENT)
public class AboutBody extends Body {

    // ── Mod metadata ──────────────────────────────────────────────────────────
    private static final String MOD_NAME    = "MaceBot";
    private static final String MOD_DESC    =
            "MaceBot is a server-side Fabric mod for competitive Minecraft PvP. " +
                    "It provides a fully autonomous NPC bot that executes mace combos, " +
                    "tracks targets, manages potions and kits, and adapts to configurable " +
                    "difficulty settings. All state is synced in real-time between the " +
                    "server and every connected client.";
    private static final String[] AUTHORS   = { "katch0420" };
    private static final String MOD_LICENSE = "MIT";

    // ── Layout ────────────────────────────────────────────────────────────────
    private int panelX, panelY, panelWidth, panelHeight;
    private int margin, textHeight, lineH;
    private int descH;          // height of the top description block
    private int contentX, contentY, contentW, contentH;
    private int scrollOffset = 0, maxScroll = 0;

    // ── Sections ──────────────────────────────────────────────────────────────
    private final List<Section> sections = new ArrayList<>();

    @Override
    public Text getLabel() { return Text.of("About"); }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void init() {
        super.init();
        margin     = s(8, 6);
        textHeight = getTextRenderer().fontHeight;
        lineH      = textHeight + s(4, 3);

        panelX = x; panelY = y;
        panelWidth  = availableWidth;
        panelHeight = availableHeight;

        // Description block height: version row + wrapped MOD_DESC lines
        List<String> descLines = wrapText(MOD_DESC, panelWidth - margin * 2);
        descH = textHeight + margin             // version + compat warning row
                + margin / 2
                + descLines.size() * lineH        // wrapped description
                + margin                          // author / license row
                + textHeight
                + margin * 2                      // padding top + bottom
                + 1;                              // separator

        contentX = panelX + margin;
        contentY = panelY + descH;
        contentW = panelWidth - margin * 2;
        contentH = panelHeight - descH;

        buildContent();
        recalcScroll();
    }

    // ── Content definition ────────────────────────────────────────────────────

    private void buildContent() {
        sections.clear();

        Section ctrl = addSection("Controller");
        ctrl.text("The Controller tab is the main hub for managing the MaceBot.");
        ctrl.bullet("Use the Entity Panel on the left to view bot stats and quick-settings related to Macebot.");
        ctrl.bullet("The Control Panel on the right has Spawn / Kick / Play / Stop buttons.");
        ctrl.bullet("Difficulty cycles through Noob → Pro → Smart on click.");
        ctrl.bullet("Mode switches between NPC, Fight and Practice modes.");
        ctrl.bullet("Practice mode has different functions such as Stun Slam, Aim practice.");
        ctrl.note("The bot must be online for most actions to have any effect.");

        Section kits = addSection("Kit System");
        kits.text("Kits are saved inventory presets (41 slots: hotbar, inventory, armor, offhand).");
        kits.text("Built-in kits are read-only templates. Custom kits are fully editable.");
        kits.bullet("View  — read-only preview of all 41 slots.");
        kits.bullet("Load  — give the kit to MaceBot, yourself, or all players.");
        kits.bullet("Edit  — open the Kit Editor to modify slot contents.");
        kits.bullet("Dupe  — clone a kit as a new custom kit.");
        kits.bullet("Delete — remove a custom kit (irreversible).");
        kits.spacer();
        kits.note("Tip: Ctrl+S in the Kit Editor saves immediately.");

        Section editor = addSection("Kit Editor");
        editor.text("The Kit Editor has three columns:");
        editor.bullet("Left — Item Browser: two collapsible groups (Presets and vanilla Items).");
        editor.bullet("Middle — The 41 kit slots in inventory layout.");
        editor.bullet("Right — Item info panel with enchantments, component count, and Preset toggle.");
        editor.spacer();
        editor.text("Slot interactions:");
        editor.bullet("Left-click browser item — pick it up as cursor stack.");
        editor.bullet("Shift+click browser item — pick up with max stack count.");
        editor.bullet("Left-click slot — place cursor stack into slot.");
        editor.bullet("Right-click slot — place exactly 1 of cursor stack.");
        editor.bullet("Shift+click slot — remove item from slot.");
        editor.bullet("Ctrl / Middle-click slot — select slot for the info panel.");
        editor.bullet("Esc — clear cursor stack without placing.");
        editor.note("Presets are saved locally and persist across sessions. " +
                "Add any item via the + Add Preset button in the info panel.");

        Section settings = addSection("Settings");
        settings.text("Settings are split across three columns visible simultaneously:");
        settings.bullet("MaceBot — combat toggles: attacks, elytra, tracking, difficulty.");
        settings.bullet("Player  — auto-refill and buffs for the local player.");
        settings.bullet("Mod     — mode (NPC/Practice), chat/action-bar messages, practice type.");
        settings.spacer();
        settings.text("Columns collapse to a slim strip when the screen is narrow. " +
                "Click a collapsed strip to expand it; the least-recently-used column collapses instead.");
        settings.note("All changes sync to the server immediately and broadcast to every client.");

        Section theming = addSection("Theming");
        theming.text("The entire GUI is themeable. Three built-in themes ship with the mod:");
        theming.bullet("Professional Dark — default slate/blue-gray palette.");
        theming.bullet("Professional Light — bright variant, same accent color.");
        theming.bullet("Classic — original greyscale look.");
        theming.spacer();
        theming.text("Creating a custom theme:");
        theming.bullet("Go to Settings → Theme Editor.");
        theming.bullet("Click + New to fork the active theme as an editable copy.");
        theming.bullet("Expand Screen → Bodys → Panel (etc.) to find color fields.");
        theming.bullet("Click a color swatch to open the HSV color picker.");
        theming.bullet("Click Export to copy a share code (MBT1:...) to clipboard.");
        theming.bullet("Click Import on another client to load a shared code.");
        theming.note("Editing a built-in theme auto-forks it so the originals are never overwritten.");

        Section network = addSection("Networking");
        network.text("All kit and settings traffic is handled by two unified payloads:");
        network.bullet("KitSyncPayload — replaces 8+ old separate kit packets.");
        network.bullet("SettingsKey — every setting has a category, display name, and C2S/S2C handler built in.");
        network.text("Adding a new setting only requires one enum constant in SettingsKey — " +
                "the UI, sync, and packet routing are all automatic.");

        Section safety = addSection("Permissions & Safety");
        safety.text("MaceBot is intended for private servers and practice environments.");
        safety.bullet("The server operator controls all bot settings.");
        safety.bullet("Clients can only see/change settings the server has synced to them.");
        safety.bullet("All C2S packets are validated server-side before being applied.");
        safety.bullet("Built-in kits cannot be modified or deleted by clients.");
        safety.note("Do not use MaceBot on public servers you do not own.");

        Section credits = addSection("Credits");
        credits.text("Built with: Fabric API, Gson, LWJGL 3.");
        credits.text("Thanks to the Fabric modding community for tooling and documentation.");
        credits.bullet("Author: katch0420");
        credits.bullet("License: MIT");
    }

    private Section addSection(String title) {
        Section s = new Section(title);
        sections.add(s);
        return s;
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    private void recalcScroll() {
        int total = 0;
        for (Section s : sections) {
            total += s.headerH();
            if (s.expanded) total += s.contentH(this);
        }
        maxScroll    = Math.max(0, total - contentH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        if (mx >= contentX && mx < contentX + contentW
                && my >= contentY && my < contentY + contentH) {
            scrollOffset = Math.max(0, Math.min(maxScroll,
                    scrollOffset - (int)(vAmt * lineH * 2)));
            return true;
        }
        return super.mouseScrolled(mx, my, hAmt, vAmt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
        if (mx >= contentX && mx < contentX + contentW
                && my >= contentY && my < contentY + contentH) {
            int y = contentY - scrollOffset;
            for (Section s : sections) {
                int hh = s.headerH();
                if (my >= y && my < y + hh) {
                    s.expanded = !s.expanded;
                    recalcScroll();
                    return true;
                }
                y += hh;
                if (s.expanded) y += s.contentH(this);
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        c.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, theme.body_background());

        renderDescBlock(c);

        c.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        renderSections(c, mx, my);
        c.disableScissor();

        renderScrollbar(c);
    }

    private void renderDescBlock(DrawContext c) {
        int y = panelY + margin;

        // ── Version + compat warning ──────────────────────────────────────────
        String clientVer = MaceBot.VERSION != null ? MaceBot.VERSION : "unknown";
        String serverVer = MaceBotClient.SERVER_SIDE_VERSION != null ? MaceBotClient.SERVER_SIDE_VERSION : "unknown";

        String versionLine = MOD_NAME + "  v" + clientVer + "  \u2022  Server: v" + serverVer;
        c.drawText(getTextRenderer(), Text.of(versionLine), panelX + margin, y, theme.accent(), true);

        // Compat check: compare the minor (x) component of x.y.z
        boolean mismatch = checkMinorMismatch(clientVer, serverVer);
        if (mismatch) {
            int warnX = panelX + margin + getTextRenderer().getWidth(versionLine) + margin;
            String warn = "\u26A0 Version mismatch — some features may not work correctly";
            c.drawText(getTextRenderer(), Text.of(warn), warnX, y, theme.warning(), true);
        }
        y += textHeight + margin / 2;

        // ── Wrapped description ───────────────────────────────────────────────
        for (String line : wrapText(MOD_DESC, panelWidth - margin * 2)) {
            c.drawText(getTextRenderer(), Text.of(line), panelX + margin, y, theme.body_value(), true);
            y += lineH;
        }
        y += margin;

        // ── Author / license one-liner ────────────────────────────────────────
        String meta = "by " + String.join(", ", AUTHORS) + "  \u2022  " + MOD_LICENSE;
        c.drawText(getTextRenderer(), Text.of(meta), panelX + margin, y, theme.body_value() & 0xAAFFFFFF, true);

        y += textHeight + margin;

        // Separator
        c.fill(panelX, y, panelX + panelWidth, y + 1, theme.panel_separator());
    }

    private void renderSections(DrawContext c, int mx, int my) {
        int y = contentY - scrollOffset;

        for (Section s : sections) {
            int hh = s.headerH();

            // Only draw if on screen
            if (y + hh >= contentY && y < contentY + contentH) {
                boolean hov = mx >= contentX && mx < contentX + contentW
                        && my >= y && my < y + hh;

                // Header row background
                int headerBg = hov
                        ? (theme.body_button_background() & 0x00FFFFFF | 0x77000000)
                        : (theme.body_background() & 0x00FFFFFF | 0x44000000);
                c.fill(contentX, y, contentX + contentW, y + hh, headerBg);

                // Accent left bar when expanded
                if (s.expanded)
                    c.fill(contentX, y, contentX + 2, y + hh, theme.accent());

                // Arrow + title
                String arrow = s.expanded ? "\u25BC " : "\u25B6 ";
                c.drawText(getTextRenderer(), Text.of(arrow + s.title),
                        contentX + margin, y + (hh - textHeight) / 2,
                        s.expanded ? theme.accent() : theme.body_label(), true);

                // Bottom border
                c.fill(contentX, y + hh - 1, contentX + contentW, y + hh,
                        theme.panel_separator() & 0x44FFFFFF);
            }
            y += hh;

            // Content entries
            if (s.expanded) {
                for (Entry e : s.entries) {
                    int eh = e.height(this);
                    if (y + eh >= contentY && y < contentY + contentH) {
                        e.render(c, contentX, y, contentW, this);
                    }
                    y += eh;
                }
            }
        }
    }

    private void renderScrollbar(DrawContext c) {
        if (maxScroll <= 0) return;
        int trackX = panelX + panelWidth - s(5, 4);
        c.fill(trackX, contentY, trackX + 3, contentY + contentH, theme.panel_separator());
        int thumbH = Math.max(16, contentH * contentH / Math.max(1, contentH + maxScroll));
        int thumbY = contentY + (contentH - thumbH) * scrollOffset / Math.max(1, maxScroll);
        c.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, theme.accent());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean checkMinorMismatch(String v1, String v2) {
        try {
            String[] p1 = v1.split("[.\\-]");
            String[] p2 = v2.split("[.\\-]");
            if (p1.length >= 2 && p2.length >= 2)
                return !p1[1].equals(p2[1]);
        } catch (Exception ignored) {}
        return false;
    }

    List<String> wrapText(String text, int maxW) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String test = cur.isEmpty() ? word : cur + " " + word;
            if (getTextRenderer().getWidth(test) > maxW) {
                if (!cur.isEmpty()) lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        return lines;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) { return false; }

    // ── Section + Entry model ─────────────────────────────────────────────────

    private class Section {
        final String title;
        final List<Entry> entries = new ArrayList<>();
        boolean expanded = false;

        Section(String title) { this.title = title; }

        int headerH() {
            // Uniform header height with scaled padding
            return lineH + s(6, 4);
        }

        int contentH(AboutBody body) {
            int h = 0;
            for (Entry e : entries) {
                h += e.height(body) + s(2, 1); // consistent padding between entries
            }
            return h + s(4, 2); // bottom padding when expanded
        }

        Section text(String paragraph) {
            entries.add(new TextEntry(paragraph, false, false));
            return this;
        }

        Section bullet(String line) {
            entries.add(new TextEntry(line, true, false));
            return this;
        }

        Section note(String line) {
            entries.add(new TextEntry(line, false, true));
            return this;
        }

        Section spacer() {
            entries.add(new SpacerEntry());
            return this;
        }

        Section image(Identifier id, int imgW, int imgH) {
            entries.add(new ImageEntry(id, imgW, imgH));
            return this;
        }
    }


    // ── Entry types ───────────────────────────────────────────────────────────

    private interface Entry {
        int    height(AboutBody body);
        void   render(DrawContext c, int x, int y, int w, AboutBody body);
    }

    private class TextEntry implements Entry {
        final String text;
        final boolean isBullet;
        final boolean isNote;

        TextEntry(String text, boolean isBullet, boolean isNote) {
            this.text = text;
            this.isBullet = isBullet;
            this.isNote = isNote;
        }

        private List<String> wrapped(int w, AboutBody body) {
            int indent = isBullet ? body.s(16, 12) : body.s(8, 6);
            return body.wrapText(text, w - indent);
        }

        @Override
        public int height(AboutBody body) {
            return wrapped(contentW, body).size() * lineH + body.s(2, 1);
        }

        @Override
        public void render(DrawContext c, int x, int y, int w, AboutBody body) {
            int indent = isBullet ? body.s(16, 12) : body.s(8, 6);
            int col    = isNote ? theme.warning() : theme.body_value();

            if (isNote) {
                List<String> lines = wrapped(w, body);
                int h = lines.size() * lineH + body.s(2, 1);
                c.fill(x, y, x + w, y + h, theme.accent() & 0x11FFFFFF);
                c.fill(x, y, x + 2, y + h, theme.accent() & 0x66FFFFFF);
            }
            if (isBullet) {
                c.drawText(getTextRenderer(), Text.of("\u2022"),
                        x + body.s(8, 6), y + body.s(1, 0), theme.accent(), false);
            }
            List<String> lines = wrapped(w, body);
            for (int i = 0; i < lines.size(); i++) {
                c.drawText(getTextRenderer(), Text.of(lines.get(i)),
                        x + indent, y + i * lineH, col, false);
            }
        }
    }


    private static class SpacerEntry implements Entry {
        @Override public int  height(AboutBody body) { return body.lineH / 2; }
        @Override public void render(DrawContext c, int x, int y, int w, AboutBody body) {}
    }

    private class ImageEntry implements Entry {
        final Identifier id;
        final int        imgW, imgH;

        ImageEntry(Identifier id, int imgW, int imgH) {
            this.id   = id;
            this.imgW = imgW;
            this.imgH = imgH;
        }

        private int dispW(int availW) { return Math.min(imgW, availW - margin * 2); }
        private int dispH(int availW) {
            if (imgW <= 0) return imgH;
            return imgH * dispW(availW) / imgW;
        }

        @Override
        public int height(AboutBody body) {
            return dispH(contentW) + margin;
        }

        @Override
        public void render(DrawContext c, int x, int y, int w, AboutBody body) {
            int dw = dispW(w);
            int dh = dispH(w);
            int dx = x + margin + (w - margin * 2 - dw) / 2; // centered

            boolean exists = net.minecraft.client.MinecraftClient.getInstance()
                    .getResourceManager().getResource(id).isPresent();

            if (exists) {
                c.drawTexture(
                        //? if >=1.21.4 {
                        /* net.minecraft.client.render.RenderLayer::getGuiTextured,
                         *///?}
                        id, dx, y, 0, 0, dw, dh, dw, dh);
            } else {
                // Placeholder box so layout doesn't break while image is missing
                c.fill(dx, y, dx + dw, y + dh, theme.body_border() & 0x44FFFFFF);
                drawBorder(c, dx, y, dw, dh, theme.body_border());
                String lbl = "[image: " + id.getPath() + "]";
                int lw = getTextRenderer().getWidth(lbl);
                if (lw < dw - 4)
                    c.drawText(getTextRenderer(), Text.of(lbl),
                            dx + (dw - lw) / 2, y + (dh - textHeight) / 2,
                            theme.body_value() & 0x88FFFFFF, false);
            }
        }

        private void drawBorder(DrawContext c, int x, int y, int w, int h, int col) {
            c.fill(x, y, x+w, y+1, col); c.fill(x, y+h-1, x+w, y+h, col);
            c.fill(x, y, x+1, y+h, col); c.fill(x+w-1, y, x+w, y+h, col);
        }
    }
}