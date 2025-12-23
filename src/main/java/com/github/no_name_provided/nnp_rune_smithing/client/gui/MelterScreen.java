package com.github.no_name_provided.nnp_rune_smithing.client.gui;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.MelterMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability.MelterFluidHandler.MELTER_CAPACITY;

public class MelterScreen extends AbstractContainerScreen<MelterMenu> {
    private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/melter_screen.png");
    private final int TANK_HEIGHT;
    
    public MelterScreen(MelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        TANK_HEIGHT = getTankCoord(3) - getTankCoord(1);
    }
    
    /**
     * Renders the graphical user interface (GUI) elements.
     *
     * @param graphics    the GuiGraphics object used for rendering.
     * @param mouseX      the x-coordinate of the mouse cursor.
     * @param mouseY      the y-coordinate of the mouse cursor.
     * @param partialTick the partial tick time.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        // Required to make tooltips render
        renderTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        Fluid tankContents = BuiltInRegistries.FLUID.byId(menu.DATA.get(5));
        boolean overTank = x > getTankCoord(0) &&
                x < getTankCoord(2) &&
                y > getTankCoord(1) &&
                y < getTankCoord(3);
        if (overTank) {
            guiGraphics.renderComponentTooltip(
                    this.font,
                    List.of(
//                            Component.literal("Fluid ID: " + menu.DATA.get(5)),
                            Component.literal(I18n.get(tankContents.getFluidType().getDescriptionId())),
                            Component.literal(NumberFormat.getIntegerInstance().format(menu.DATA.get(4)) + " millibuckets"),
                            Component.literal(NumberFormat.getIntegerInstance().format(tankContents.getFluidType().getTemperature()) + " degrees C")
                    ),
                    x,
                    y
            );
        }
    }
    
    private int getTankCoord(int index) {
        
        return switch (index) {
            case 0 -> this.leftPos + 138;
            case 1 -> this.topPos + 17;
            case 2 -> this.leftPos + 161;
            case 3 -> this.topPos + 74;
            default -> 0;
        };
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        updateBurnTimeSprite(graphics);
        drawTankContents(graphics);
        int meltingTotalTime = menu.DATA.get(3);
        if (meltingTotalTime > 0) {
            int color = getEffectiveColor(meltingTotalTime, menu.DATA.get(2));
            graphics.fill(36 + leftPos, 18 +topPos, 51 + leftPos, 33 + topPos, color);
        }
    }
    
    private int getEffectiveColor(int meltingTotalTime, int meltingProgress) {
        int meltingPoint = BuiltInRegistries.FLUID.byId(menu.DATA.get(5)).getFluidType().getTemperature() * meltingProgress / meltingTotalTime;
        int temperature = meltingPoint * meltingProgress/meltingTotalTime;
        Map.Entry<Integer, Integer> lowerEntry = FluidHelper.tempToColor.floorEntry(Math.max(temperature, 200));
        Map.Entry<Integer, Integer> upperEntry = FluidHelper.tempToColor.ceilingEntry(Math.min(temperature, 1000));
        
        int alpha = temperature < 199 ? 255 * temperature / 199 : 255;
        
        if (lowerEntry.equals(upperEntry)) {
            
            return FastColor.ARGB32.color(alpha, lowerEntry.getValue());
        }
        
        int changeTemp = upperEntry.getKey() - lowerEntry.getKey();
        int changeRed = FastColor.ARGB32.red(upperEntry.getValue()) - FastColor.ARGB32.red(lowerEntry.getValue());
        float slopeRed = (float) (changeRed) / (changeTemp);
        int changeGreen = FastColor.ARGB32.green(upperEntry.getValue()) - FastColor.ARGB32.green(lowerEntry.getValue());
        float slopeGreen = (float) (changeGreen) / (changeTemp);
        int changeBlue = FastColor.ARGB32.blue(upperEntry.getValue()) - FastColor.ARGB32.blue(lowerEntry.getValue());
        float slopeBlue = (float) (changeBlue) / (changeTemp);
        
        return FastColor.ARGB32.color(
                alpha,
                (int)(FastColor.ARGB32.red(lowerEntry.getValue()) + changeTemp * slopeRed),
                (int)(FastColor.ARGB32.green(lowerEntry.getValue()) + changeTemp * slopeGreen),
                (int)(FastColor.ARGB32.blue(lowerEntry.getValue()) + changeTemp * slopeBlue)
        );
    }
    
    /**
     * Largely copied from vanilla (abstract) furnace code.
     */
    private void updateBurnTimeSprite(GuiGraphics graphics) {
        float litProgress = Mth.clamp((float) menu.DATA.get(0) / menu.DATA.get(1), 0.0f, 1.0f);
        graphics.blitSprite(
                ResourceLocation.withDefaultNamespace("container/furnace/lit_progress"),
                14,
                14,
                0,
                14 - Mth.ceil(litProgress * 13.0f) + 1,
                leftPos + 36 + 1,
                topPos - Mth.ceil(litProgress * 13.0f) - 1 + 7 + 44,
                14,
                Mth.ceil(litProgress * 13.0F) + 1
        );
    }
    
    /**
     * Renders the tank's fluid content. Should trigger a fluid type update via the menu.
     * <p>Though this may seem long, the menu is actually doing some of the heavy lifting.</p>
     */
    private void drawTankContents(GuiGraphics graphics) {
        // Make sure the fluid is up to date
        menu.DATA.get(5);
        // Ignore Fluids.EMPTY
        if (null != menu.DATA.outputTexture && menu.DATA.get(4) > 0) {
            // Ignore the default tint, which is otherwise far too strong.
            if (menu.DATA.get(6) != 0xFFFFFFFF) {
                graphics.blit(
                        getTankCoord(0),
                        Mth.floor(getTankCoord(1) + TANK_HEIGHT * (1 - (double) menu.DATA.get(4) / (double) MELTER_CAPACITY)),
                        1,
                        getTankCoord(2) - getTankCoord(0),
                        Mth.floor(TANK_HEIGHT * (double) menu.DATA.get(4) / (double) MELTER_CAPACITY),
                        Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(menu.DATA.outputTexture),
                        // Not documented anywhere, but these expect normalized values and will have bizarre overflow
                        // behavior if you pass it a straight color component. The tell, insofar as there is one, is
                        // probably the float parameter type. #BlametheNeoForgeTeam or #BlameParchment
                        (float) FastColor.ARGB32.red(menu.DATA.get(6)) / 255,
                        (float) FastColor.ARGB32.green(menu.DATA.get(6)) / 255,
                        (float) FastColor.ARGB32.blue(menu.DATA.get(6)) / 255,
                        1
                );
            } else {
                graphics.blit(
                        getTankCoord(0),
                        Mth.floor(getTankCoord(1) + TANK_HEIGHT * (1 - (double) menu.DATA.get(4) / (double) MELTER_CAPACITY)),
                        1,
                        getTankCoord(2) - getTankCoord(0),
                        Mth.floor(TANK_HEIGHT * (double) menu.DATA.get(4) / (double) MELTER_CAPACITY),
                        Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(menu.DATA.outputTexture)
                );
            }
        }
    }
}
