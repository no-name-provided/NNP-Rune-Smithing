package com.github.no_name_provided.nnp_rune_smithing.common.gui.menus;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MelterMenu>> MELTER_MENU = MENU_TYPES.register(
            "melter_menu_type",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new MelterMenu(
                            windowId,
                            inv.player.getInventory(),
                            data.readBlockPos()
                    )
            )
    );

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}
