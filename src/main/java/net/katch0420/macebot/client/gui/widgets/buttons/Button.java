package net.katch0420.macebot.client.gui.widgets.buttons;

import net.katch0420.macebot.client.gui.widgets.builder.ButtonBuilder;
import net.katch0420.macebot.client.gui.widgets.core.ButtonWidget;
import net.katch0420.macebot.client.utils.ColorHelper;
import net.katch0420.macebot.client.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Button extends ButtonWidget<Button> {

    private final Data data;
    private final DisplayData displayData;

    private int shuffleTextureIndex = 0;
    private int shuffleLabelIndex = 0;

    public boolean toggle = true;

    public Button(int x, int y, int width, int height, int backgroundColor, int foregroundColor, int holdColor, int hoverColor, int borderColor, Tooltip tooltip, Consumer<Button> onClick, Supplier<Boolean> activeSupplier, Data data) {
        super(backgroundColor, foregroundColor, holdColor, hoverColor, borderColor, tooltip, onClick, activeSupplier);
        setPosition(x, y);
        setSize(width, height);
        this.data = data;
        this.displayData = new DisplayData(data);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        switch (data.renderType) {
            case TEXTURED -> renderTexturedButton(context, mouseX, mouseY);
            case CLASSIC -> renderClassicButton(context, mouseX, mouseY);
        }
        if(borderColor != 0) RenderUtils.drawBorder(context, x, y, width, height, active ? borderColor : ColorHelper.darken(borderColor, 0.6f));
    }

    private void renderTexturedButton(DrawContext c, int mx, int my) {
        c.fill(x, y, x + width, y + height, !active ? (held ? holdColor : (hovered ? hoverColor : backgroundColor)) : backgroundColor);

        Identifier displayTexture = getDisplayTexture();
        if (displayTexture != null) {
            RenderUtils.drawTexture(c, displayTexture, x, y, 0, 0, width, height, width, height);
        }

        Text label = getDisplayLabel();
        if (label != null) {
            if (data.captionMode == CaptionMode.BELOW_TEXTURE) {
                // Render as a real caption under the icon instead of a tooltip.
                int textY = y + height + 2;
                c.drawCenteredTextWithShadow(textRenderer, label, x + width / 2, textY, foregroundColor);
            } else if (hovered) {
                // FIX: this used to call drawTooltip() unconditionally, every
                // single frame, regardless of whether the mouse was anywhere
                // near the button - so a tooltip would hover at the cursor
                // permanently once any textured button existed. Tooltip mode
                // now only shows while actually hovering this button.
                c.drawTooltip(textRenderer, label, mx, my);
            }
        }

        if (!active) {
            drawDisabledOverlay(c);
        }
    }

    private void renderClassicButton(DrawContext c, int mx, int my) {
        c.fill(x, y, x + width, y + height, !active ? (held ? holdColor : (hovered ? hoverColor : backgroundColor)) : backgroundColor);
        Text label = getDisplayLabel();
        if (label != null) {
            int textY = y + (height - textRenderer.fontHeight) / 2;
            c.drawCenteredTextWithShadow(textRenderer, label, x + width / 2, textY, active ? foregroundColor : ColorHelper.darken(foregroundColor, 0.6f));
        }
        if (!active) {
            drawDisabledOverlay(c);
        }
    }

    /**
     * FIX: the previous disabled overlay computed
     * {@code (int)(backgroundColor * 0.6 + 0xFF000000)} - multiplying a
     * packed ARGB int as if it were a plain number instead of darkening it
     * channel-by-channel. That produces a garbage/wrong-looking color
     * (often a wildly different hue, not a darkened version of the button).
     * A flat semi-transparent black overlay reads correctly as "disabled"
     * over any background color and matches the classic-button style below.
     */
    private void drawDisabledOverlay(DrawContext c) {
        c.fill(x, y, x + width, y + height, 0x80101010);
    }

    private Identifier getDisplayTexture() {
        if (data.functionType == null) return null;
        return switch (data.functionType) {
            case TOGGLE -> toggle ? displayData.toggleTexture1 : displayData.toggleTexture2;
            case CLICK -> held ? displayData.heldTexture : (hovered ? displayData.hoverTexture : displayData.baseTexture);
            case SHUFFLE -> {
                List<Identifier> textures = displayData.shuffleTextures;
                yield (textures == null || textures.isEmpty()) ? null : textures.get(shuffleTextureIndex);
            }
        };
    }

    private Text getDisplayLabel() {
        if (data.functionType == null) return null;
        return switch (data.functionType) {
            case TOGGLE -> toggle ? displayData.toggleLabel1 : displayData.toggleLabel2;
            case CLICK -> held ? displayData.heldLabel : (hovered ? displayData.hoverLabel : displayData.baseLabel);
            case SHUFFLE -> {
                List<Text> labels = displayData.shuffleLabel;
                yield (labels == null || labels.isEmpty()) ? null : labels.get(shuffleLabelIndex);
            }
        };
    }

    /**
     * Updates the base label (and any hover/held label that was just mirroring
     * the base label) - used e.g. by "click to cycle difficulty" buttons that
     * rewrite their own caption after being pressed.
     */
    public void setLabelIfClickButton(Text label) {
        if (displayData.hoverLabel == displayData.baseLabel) displayData.hoverLabel = label;
        if (displayData.heldLabel == displayData.baseLabel) displayData.heldLabel = label;
        displayData.baseLabel = label;
    }

    public void setSingleLabel(Text label){
        displayData.hoverLabel = label;
        displayData.heldLabel = label;
        displayData.baseLabel = label;
    }

    /** Swaps the base texture (and any hover/held texture mirroring it) at runtime. */
    public void setTextureIfClickButton(Identifier texture) {
        if (displayData.hoverTexture == displayData.baseTexture) displayData.hoverTexture = texture;
        if (displayData.heldTexture == displayData.baseTexture) displayData.heldTexture = texture;
        displayData.baseTexture = texture;
    }

    /** Forces the toggle button into a specific state without simulating a click. */
    public void setToggle(boolean value) {
        this.toggle = value;
    }

    public DisplayData getDisplayData() {
        return displayData;
    }

    @Override
    protected void handleClick(double mouseX, double mouseY) {
        applyClickEffect();
        super.handleClick(mouseX, mouseY);
    }

    @Override
    protected void handleRightClick(double mouseX, double mouseY) {
        if (data.rightClickCycles) {
            applyClickEffect();
        }
        super.handleRightClick(mouseX, mouseY);
    }

    private void applyClickEffect() {
        switch (data.functionType) {
            case TOGGLE -> toggle = !toggle;
            case SHUFFLE -> {
                List<Identifier> textures = displayData.shuffleTextures;
                List<Text> labels = displayData.shuffleLabel;

                if (textures != null && !textures.isEmpty()) {
                    shuffleTextureIndex = (shuffleTextureIndex >= textures.size() - 1) ? 0 : shuffleTextureIndex + 1;
                }
                if (labels != null && !labels.isEmpty()) {
                    shuffleLabelIndex = (shuffleLabelIndex >= labels.size() - 1) ? 0 : shuffleLabelIndex + 1;
                }
            }
            case CLICK -> {
                // no built-in state change
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ButtonBuilder<Button,Builder> {
        protected RenderType renderType = RenderType.CLASSIC;
        protected FunctionType functionType = FunctionType.CLICK;
        protected CaptionMode captionMode = CaptionMode.TOOLTIP;
        protected boolean rightClickCycles = false;

        protected boolean toggled = true;

        protected HashMap<Integer, Identifier> textures = new HashMap<>();
        protected HashMap<Integer, Text> labels = new HashMap<>();

        protected int tIdx = 0;
        protected int lIdx = 0;
        protected int sTexIdx = 1;
        protected int sLabIdx = 1;

        public Builder toggleTexture(Identifier texture) {
            textures.put(tIdx++, texture);
            renderType = RenderType.TEXTURED;
            functionType = FunctionType.TOGGLE;
            return this;
        }

        public Builder texture(Identifier texture) {
            textures.put(2, texture);
            renderType = RenderType.TEXTURED;
            return this;
        }

        public Builder heldTexture(Identifier texture) {
            textures.put(3, texture);
            renderType = RenderType.TEXTURED;
            return this;
        }

        public Builder hoverTexture(Identifier texture) {
            textures.put(4, texture);
            renderType = RenderType.TEXTURED;
            return this;
        }

        public Builder shuffleTexture(Identifier texture) {
            textures.put(-(sTexIdx++), texture);
            renderType = RenderType.TEXTURED;
            functionType = FunctionType.SHUFFLE;
            return this;
        }

        public Builder toggleLabel(Text label) {
            labels.put(lIdx++, label);
            functionType = FunctionType.TOGGLE;
            return this;
        }

        public Builder baseLabel(Text label) {
            labels.put(2, label);
            return this;
        }

        public Builder heldLabel(Text label) {
            labels.put(3, label);
            return this;
        }

        public Builder hoverLabel(Text label) {
            labels.put(4, label);
            return this;
        }

        public Builder shuffleLabel(Text label) {
            labels.put(-(sLabIdx++), label);
            functionType = FunctionType.SHUFFLE;
            return this;
        }

        public Builder renderType(RenderType type) {
            this.renderType = type;
            return this;
        }

        public Builder functionType(FunctionType type) {
            this.functionType = type;
            return this;
        }

        /**
         * For TEXTURED buttons: render the label as a permanent caption under
         * the icon instead of a hover-only tooltip. Useful for icon+caption
         * rows like quick-setting buttons.
         */
        public Builder captionBelowTexture() {
            this.captionMode = CaptionMode.BELOW_TEXTURE;
            return this;
        }

        /** For TEXTURED buttons (default): label only shows as a hover tooltip. */
        public Builder captionAsTooltip() {
            this.captionMode = CaptionMode.TOOLTIP;
            return this;
        }

        /** Right-click also advances TOGGLE/SHUFFLE state (e.g. cycle backward UX could hook this). */
        public Builder rightClickCycles(boolean value) {
            this.rightClickCycles = value;
            return this;
        }

        /** Convenience: sets a constant active/inactive state instead of wiring an activeSupplier. */
        public Builder enabled(boolean enabled) {
            this.activeSupplier = () -> enabled;
            return this;
        }

        public Builder toggled(boolean toggled) {
            this.toggled = toggled;
            return this;
        }

        public Button build() {
            RenderType rt = renderType != null ? renderType : RenderType.CLASSIC;
            FunctionType ft = functionType != null ? functionType : FunctionType.CLICK;
            Button button = new Button(x, y, width, height, backgroundColor, foregroundColor, holdColor, hoverColor, borderColor, tooltip, onClick, activeSupplier, new Data(ft, rt, captionMode, rightClickCycles, textures, labels));
            if (onRightClick != null) {
                button.setOnRightClick(onRightClick);
            }
            if (onKeyActivate != null) {
                button.setOnKeyActivate(onKeyActivate);
            }
            button.setToggle(toggled);
            return button;
        }
    }

    public enum RenderType {TEXTURED, CLASSIC}

    public enum FunctionType {CLICK, TOGGLE, SHUFFLE}

    /** How a TEXTURED button shows its label, if any. */
    public enum CaptionMode {TOOLTIP, BELOW_TEXTURE}

    public record Data(FunctionType functionType, RenderType renderType, CaptionMode captionMode, boolean rightClickCycles, HashMap<Integer, Identifier> textures, HashMap<Integer, Text> labels) {
        public Data(FunctionType functionType, RenderType renderType, CaptionMode captionMode, boolean rightClickCycles, HashMap<Integer, Identifier> textures, HashMap<Integer, Text> labels) {
            this.functionType = functionType;
            this.renderType = renderType;
            this.captionMode = captionMode != null ? captionMode : CaptionMode.TOOLTIP;
            this.rightClickCycles = rightClickCycles;
            this.textures = textures != null ? new HashMap<>(textures) : new HashMap<>();
            this.labels = labels != null ? new HashMap<>(labels) : new HashMap<>();
        }
    }

    public static class DisplayData {
        public Identifier baseTexture;
        public Identifier heldTexture;
        public Identifier hoverTexture;
        public Identifier toggleTexture1;
        public Identifier toggleTexture2;
        public List<Identifier> shuffleTextures;

        public Text baseLabel;
        public Text heldLabel;
        public Text hoverLabel;
        public Text toggleLabel1;
        public Text toggleLabel2;
        public List<Text> shuffleLabel;

        public DisplayData(Data data) {
            this.shuffleTextures = new ArrayList<>();
            this.shuffleLabel = new ArrayList<>();

            this.baseTexture = data.textures.get(2);
            this.heldTexture = data.textures.getOrDefault(3, baseTexture);
            this.hoverTexture = data.textures.getOrDefault(4, baseTexture);
            this.toggleTexture1 = data.textures.get(0);
            this.toggleTexture2 = data.textures.get(1);

            data.textures.forEach((key, value) -> {
                if (key < 0) this.shuffleTextures.add(value);
            });

            this.baseLabel = data.labels.get(2);
            this.heldLabel = data.labels.getOrDefault(3, baseLabel);
            this.hoverLabel = data.labels.getOrDefault(4, baseLabel);
            this.toggleLabel1 = data.labels.get(0);
            this.toggleLabel2 = data.labels.get(1);

            data.labels.forEach((key, value) -> {
                if (key < 0) this.shuffleLabel.add(value);
            });
        }

        public DisplayData setLabel(Text label) {
            if(heldLabel == baseLabel) heldLabel = label;
            if(hoverLabel == baseLabel) hoverLabel = label;
            this.baseLabel = label;
            return this;
        }

        public DisplayData setTexture(Identifier texture) {
            if(heldTexture == baseTexture) heldTexture = texture;
            if(hoverTexture == baseTexture) hoverTexture = texture;
            this.baseTexture = texture;
            return this;
        }
    }
}
