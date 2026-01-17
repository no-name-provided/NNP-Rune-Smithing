package com.github.no_name_provided.nnp_rune_smithing.client.gui;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.MelterMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability.MelterFluidHandler.MELTER_CAPACITY;

public class MelterScreen extends AbstractContainerScreen<MelterMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/melter_screen.png");
    private static final ResourceLocation THERMOMETER = ResourceLocation.fromNamespaceAndPath(MODID, "container/melter/melt_progress_thermometer");
    private final int TANK_HEIGHT;
    private final int THERMOMETER_PARTIAL_HEIGHT;
    
    public MelterScreen(MelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        THERMOMETER_PARTIAL_HEIGHT = getThermometerCoord(3) - getThermometerCoord(1);
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
            // For some reason, the empty fluid defaults to air at 0 deg Celsius. That's silly
            int temperature = tankContents == Fluids.EMPTY ? 20 : tankContents.getFluidType().getTemperature();
            ArrayList<Component> tooltip = new ArrayList<>(List.of(
                    Component.literal(I18n.get(tankContents.getFluidType().getDescriptionId())),
                    Component.literal(NumberFormat.getIntegerInstance().format(menu.DATA.get(4)) + " millibuckets"),
                    Component.literal(NumberFormat.getIntegerInstance().format(temperature) + " °C")
            ));
            @SuppressWarnings("deprecation") // If this needs to be refactored at some point, we should probably go the extra mile and add caching
            CastableFluidData data = tankContents.builtInRegistryHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
            if (null != data) {
                tooltip.add(Component.literal("Tier " + NumberFormat.getIntegerInstance().format(data.tier())));
            }
            guiGraphics.renderComponentTooltip(
                    this.font,
                    tooltip,
                    x,
                    y
            );
        }
    }
    
    private int getTankCoord(int index) {
        
        return switch (index) {
            case 0 -> this.leftPos + 138;
            case 1 -> this.topPos + 17;
            case 2 -> this.leftPos + 162;
            case 3 -> this.topPos + 74;
            default -> 0;
        };
    }
    
    private int getThermometerCoord(int index) {
        
        return switch (index) {
            case 0 -> this.leftPos + 84;
            case 1 -> this.topPos + 22;
            case 2 -> this.leftPos + 104;
            case 3 -> this.topPos + 57;
            default -> 0;
        };
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        updateBurnTimeSprite(graphics);
        drawTankContents(graphics);
        drawThermometerFluid(graphics);
        int meltingProgress = menu.DATA.get(2);
        int meltingTotalTime = menu.DATA.get(3);
        if (meltingTotalTime > 0) {
            int color = getEffectiveColor(meltingTotalTime, meltingProgress);
            graphics.fill(36 + leftPos, 18 + topPos, 51 + leftPos, 33 + topPos, color);
        }
    }
    
    /**
     * Interpolates the correct color for a molten metal at this temperature, using a hard coded table based on
     * blackbody radiation assumptions.
     *
     * <p>
     * We don't use #computeIfAbsent, as that usually mutates the map.
     * </p>
     *
     * @param meltingTotalTime Total time required to melt.
     * @param meltingProgress  Time already spent melting.
     * @return Color of fluid at this stage in its melting progress.
     */
    private int getEffectiveColor(int meltingTotalTime, int meltingProgress) {
        int meltingPoint = BuiltInRegistries.FLUID.byId(menu.DATA.get(5)).getFluidType().getTemperature();// * meltingProgress / meltingTotalTime;
        int temperature = meltingPoint * meltingProgress / meltingTotalTime;
        // We use the actual temperature to calculate alpha, before normalizing it to avoid edge effects
        int alpha = temperature < 199 ? (int) (255f * temperature / 199) : 255;
        // Prevent weird backwards interpolations near ends of range
        temperature = temperature > FluidHelper.tempToColor.lastKey() ? FluidHelper.tempToColor.lastKey() : temperature;
        temperature = temperature < FluidHelper.tempToColor.firstKey() ? FluidHelper.tempToColor.firstKey() : temperature;
        
        Map.Entry<Integer, Integer> lowerEntry = FluidHelper.tempToColor.floorEntry(Math.max(temperature, 200));
        Map.Entry<Integer, Integer> upperEntry = FluidHelper.tempToColor.ceilingEntry(Math.min(temperature, 1000));
        
        
        
        if (lowerEntry.equals(upperEntry)) {
            
            return FastColor.ARGB32.color(alpha, lowerEntry.getValue());
        }
        
        int changeTemp = temperature - lowerEntry.getKey();
        int rangeTemp = upperEntry.getKey() - lowerEntry.getKey();
        int rangeRed = FastColor.ARGB32.red(upperEntry.getValue()) - FastColor.ARGB32.red(lowerEntry.getValue());
        float slopeRed = (float) (rangeRed) / (rangeTemp);
        int rangeGreen = FastColor.ARGB32.green(upperEntry.getValue()) - FastColor.ARGB32.green(lowerEntry.getValue());
        float slopeGreen = (float) (rangeGreen) / (rangeTemp);
        int rangeBlue = FastColor.ARGB32.blue(upperEntry.getValue()) - FastColor.ARGB32.blue(lowerEntry.getValue());
        float slopeBlue = (float) (rangeBlue) / (rangeTemp);
        
        return FastColor.ARGB32.color(
                alpha,
                (int) (FastColor.ARGB32.red(lowerEntry.getValue()) + changeTemp * slopeRed),
                (int) (FastColor.ARGB32.green(lowerEntry.getValue()) + changeTemp * slopeGreen),
                (int) (FastColor.ARGB32.blue(lowerEntry.getValue()) + changeTemp * slopeBlue)
        );
    }
    
    /**
     * Largely copied from vanilla (abstract) furnace code.
     */
    private void updateBurnTimeSprite(GuiGraphics graphics) {
        float litProgress = Mth.clamp((float) menu.DATA.get(0) / menu.DATA.get(1), 0.0f, 1.0f);
        graphics.blitSprite(
                ResourceLocation.fromNamespaceAndPath(MODID, "container/melter/lit_progress"),
                15,
                15,
                0,
                14 - Mth.ceil(litProgress * 13.0f) - 1,
                leftPos + 36,
                topPos - Mth.ceil(litProgress * 13.0f) + 6 + 44,
                14,
                Mth.ceil(litProgress * 13.0f + 2)
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
                int fluidHeight = Mth.floor(TANK_HEIGHT * (double) menu.DATA.get(4) / (double) MELTER_CAPACITY);
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(menu.DATA.outputTexture);
                // Requires AT. Public wrappers don't let me specify both tint and sprite cropping (to prevent stretching)
                // Strangely, looks almost identical to other approach. There may be an issue with using this method on extended textures (animations)
                // May need to switch to a tiled approach
//                graphics.innerBlit(
//                        sprite.atlasLocation(),
//                        getTankCoord(0),
//                        getTankCoord(2),
//                        getTankCoord(3) - fluidHeight,
//                        getTankCoord(3),
//                        3,
//                        sprite.getU0(),
//                        sprite.getU1(),
//                        sprite.getV0(),//sprite.getV0() + Mth.floor((sprite.getV1() - sprite.getV0()) * (double) menu.DATA.get(4) / (double) MELTER_CAPACITY), //1 - (sprite.getV0() + (sprite.getV1() - sprite.getV0()) * fluidHeight / TANK_HEIGHT),
//                        sprite.getV1(),
//                        (float) FastColor.ARGB32.red(menu.DATA.get(6)) / 255,
//                        (float) FastColor.ARGB32.green(menu.DATA.get(6)) / 255,
//                        (float) FastColor.ARGB32.blue(menu.DATA.get(6)) / 255,
//                        1
//                );
                
                graphics.blit(
                        getTankCoord(0),
                        Mth.floor(getTankCoord(1) + TANK_HEIGHT * (1 - (double) menu.DATA.get(4) / (double) MELTER_CAPACITY)),
                        2,
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
    
    /**
     * Renders the thermometer fluid content, reflecting heating progress.
     */
    private void drawThermometerFluid(GuiGraphics graphics) {
        double meltingProgress = menu.DATA.get(2);
        double meltingTotalTime = menu.DATA.get(3);
        double meltingRatio = Mth.clamp(meltingProgress / meltingTotalTime, 0, 1);
        int spriteHeight = 35;
        if (meltingProgress > 0) {
            graphics.blitSprite(
                    THERMOMETER,
                    20,
                    spriteHeight,
                    0,
                    spriteHeight - Mth.ceil(spriteHeight * meltingRatio) - 1,
                    getThermometerCoord(0),
                    Mth.floor(getThermometerCoord(1) + THERMOMETER_PARTIAL_HEIGHT * (1 - meltingRatio)),
                    1,
                    20,
                    Mth.ceil((spriteHeight - 1) * meltingRatio) + 1
            );
        }
    }
}
