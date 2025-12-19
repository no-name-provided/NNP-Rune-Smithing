package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNE_DATA;

public class RSItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items NUGGETS = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items INGOTS = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items METAL_STORAGE_BLOCKS = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items ORE_BLOCKS = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items RAW_ORES = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items RUNES = DeferredRegister.Items.createItems(MODID);
    public static final DeferredRegister.Items WOODEN_CHARMS = DeferredRegister.Items.createItems(MODID);
    
    public static final DeferredHolder<Item, BlockItem> WHITTLING_TABLE = ITEMS.registerSimpleBlockItem(RSBlocks.WHITTLING_TABLE);
    public static final DeferredHolder<Item, Item> WHITTLING_KNIFE = ITEMS.register(
            "whittling_knife",
            () -> new WhittlingKnife(new Item.Properties())
    );
    public static final DeferredHolder<Item, BlockItem> MELTER = ITEMS.registerSimpleBlockItem(RSBlocks.MELTER);
    public static final DeferredHolder<Item, BlockItem> ALLOYER = ITEMS.registerSimpleBlockItem(RSBlocks.ALLOYER);
    public static final DeferredHolder<Item, BlockItem> CASTING_TABLE = ITEMS.registerSimpleBlockItem(RSBlocks.CASTING_TABLE);
    public static final DeferredHolder<Item, BlockItem> RUNE_ANVIL = ITEMS.registerSimpleBlockItem(RSBlocks.RUNE_ANVIL);
    
    public static final DeferredHolder<Item, BasicRuneItem> PLACE_HOLDER_RUNE = ITEMS.register("place_holder_rune", () -> new BasicRuneItem(new Item.Properties().component(RUNE_DATA, RuneData.DEFAULT)) {
        @Override public Type getType() {
            return Type.PLACE_HOLDER;
        }
    });
    
    public static final DeferredHolder<Item, Item> FIRE_CLAY_BALL = ITEMS.registerSimpleItem("fire_clay_ball");
    
    public static final DeferredHolder<Item, NuggetMold> NUGGET_MOLD = ITEMS.register(
            "nugget_mold",
            () -> new NuggetMold(new Item.Properties(), true)
    );
    public static final DeferredHolder<Item, NuggetMold> NUGGET_MOLD_REUSABLE = ITEMS.register(
            "nugget_mold_reusable",
            () -> new NuggetMold(new Item.Properties(), false)
    );
    public static final DeferredHolder<Item, IngotMold> INGOT_MOLD = ITEMS.register(
            "ingot_mold",
            () -> new IngotMold(new Item.Properties(), true)
    );
    public static final DeferredHolder<Item, IngotMold> INGOT_MOLD_REUSABLE = ITEMS.register(
            "ingot_mold_reusable",
            () -> new IngotMold(new Item.Properties(), false)
    );
    public static final DeferredHolder<Item, BlockMold> BLOCK_MOLD = ITEMS.register(
            "block_mold",
            () -> new BlockMold(new Item.Properties(), true)
    );
    public static final DeferredHolder<Item, BlockMold> BLOCK_MOLD_REUSABLE = ITEMS.register(
            "block_mold_reusable",
            () -> new BlockMold(new Item.Properties(), false)
    );
    
    public static final DeferredHolder<Item, Item> RUNE_SMITH_HAMMER = ITEMS.register(
            "rune_smith_hammer",
            () -> new RuneSmithHammer(new Item.Properties().durability(100))
    );
    
    public static final DeferredHolder<Item, BlankMold> BLANK_MOLD = ITEMS.register(
            "blank_mold",
            () -> new BlankMold(new Item.Properties())
    );
    
    public static final DeferredHolder<Item, WoodenCharm> WARRIOR_CHARM = WOODEN_CHARMS.register(
            "warrior_charm",
            () -> new WoodenCharm(new Item.Properties())
    );
    public static final DeferredHolder<Item, WoodenCharm> LUCK_CHARM = WOODEN_CHARMS.register(
            "luck_charm",
            () -> new WoodenCharm(new Item.Properties())
    );
    public static final DeferredHolder<Item, WoodenCharm> HEALTH_CHARM = WOODEN_CHARMS.register(
            "health_charm",
            () -> new WoodenCharm(new Item.Properties())
    );
    
    public static final DeferredHolder<Item, AbstractRuneItem> SELF_RUNE = RUNES.register(
            "self_rune",
            () -> new BasicRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BasicRuneItem.Mold> SELF_MOLD = ITEMS.register(
            "self_mold",
            () -> new BasicRuneItem.Mold(new Item.Properties(), SELF_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> SELF_TEMPLATE = ITEMS.register(
            "self_template",
            () -> new CastingTemplate(new Item.Properties(), SELF_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> COLLISION_RUNE = RUNES.register(
            "collision_rune",
            () -> new BasicRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BasicRuneItem.Mold> COLLISION_MOLD = ITEMS.register(
            "collision_mold",
            () -> new BasicRuneItem.Mold(new Item.Properties(), COLLISION_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> COLLISION_TEMPLATE = ITEMS.register(
            "collision_template",
            () -> new CastingTemplate(new Item.Properties(), COLLISION_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> WIELD_RUNE = RUNES.register(
            "wield_rune",
            () -> new BasicRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BasicRuneItem.Mold> WIELD_MOLD = ITEMS.register(
            "wield_mold",
            () -> new BasicRuneItem.Mold(new Item.Properties(), WIELD_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> WIELD_TEMPLATE = ITEMS.register(
            "wield_template",
            () -> new CastingTemplate(new Item.Properties(), WIELD_MOLD)
    );
    
    public static final DeferredHolder<Item, AbstractRuneItem> WARD_RUNE = RUNES.register(
            "ward_rune",
            () -> new WardRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, WardRuneItem.Mold> WARD_MOLD = ITEMS.register(
            "ward_mold",
            () -> new WardRuneItem.Mold(new Item.Properties())
    );
    public static final DeferredHolder<Item, CastingTemplate> WARD_TEMPLATE = ITEMS.register(
            "ward_template",
            () -> new CastingTemplate(new Item.Properties(), WARD_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> SIGHT_RUNE = RUNES.register(
            "sight_rune",
            () -> new WardRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BasicEffectRuneItem.Mold> SIGHT_MOLD = ITEMS.register(
            "sight_mold",
            () -> new BasicEffectRuneItem.Mold(new Item.Properties(), SIGHT_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> SIGHT_TEMPLATE = ITEMS.register(
            "sight_template",
            () -> new CastingTemplate(new Item.Properties(), SIGHT_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> EARTH_RUNE = RUNES.register(
            "earth_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.EARTH)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> EARTH_MOLD = ITEMS.register(
            "earth_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), EARTH_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> EARTH_TEMPLATE = ITEMS.register(
            "earth_template",
            () -> new CastingTemplate(new Item.Properties(), EARTH_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> FIRE_RUNE = RUNES.register(
            "fire_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.FIRE)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> FIRE_MOLD = ITEMS.register(
            "fire_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), FIRE_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> FIRE_TEMPLATE = ITEMS.register(
            "fire_template",
            () -> new CastingTemplate(new Item.Properties(), FIRE_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> AIR_RUNE = RUNES.register(
            "air_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.AIR)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> AIR_MOLD = ITEMS.register(
            "air_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), AIR_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> AIR_TEMPLATE = ITEMS.register(
            "air_template",
            () -> new CastingTemplate(new Item.Properties(), AIR_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> WATER_RUNE = RUNES.register(
            "water_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.WATER)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> WATER_MOLD = ITEMS.register(
            "water_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), WATER_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> WATER_TEMPLATE = ITEMS.register(
            "water_template",
            () -> new CastingTemplate(new Item.Properties(), WATER_MOLD)
    );
    
    public static final DeferredHolder<Item, AbstractRuneItem> VOID_RUNE = RUNES.register(
            "void_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.VOID)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> VOID_MOLD = ITEMS.register(
            "void_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), VOID_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> VOID_TEMPLATE = ITEMS.register(
            "void_template",
            () -> new CastingTemplate(new Item.Properties(), VOID_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> LIGHT_RUNE = RUNES.register(
            "light_rune",
            () -> new ElementalRuneItem(new Item.Properties(), ElementalRuneItem.Affinity.LIGHT)
    );
    public static final DeferredHolder<Item, ElementalRuneItem.Mold> LIGHT_MOLD = ITEMS.register(
            "light_mold",
            () -> new ElementalRuneItem.Mold(new Item.Properties(), LIGHT_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> LIGHT_TEMPLATE = ITEMS.register(
            "light_template",
            () -> new CastingTemplate(new Item.Properties(), LIGHT_MOLD)
    );
    
    public static final DeferredHolder<Item, AbstractRuneItem> WIDEN_RUNE = RUNES.register(
            "widen_rune",
            () -> new WidenRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, WidenRuneItem.Mold> WIDEN_MOLD = ITEMS.register(
            "widen_mold",
            () -> new WidenRuneItem.Mold(new Item.Properties())
    );
    public static final DeferredHolder<Item, CastingTemplate> WIDEN_TEMPLATE = ITEMS.register(
            "widen_template",
            () -> new CastingTemplate(new Item.Properties(), WIDEN_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> TIME_RUNE = RUNES.register(
            "time_rune",
            () -> new BaseModifierRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BaseModifierRuneItem.Mold> TIME_MOLD = ITEMS.register(
            "time_mold",
            () -> new BaseModifierRuneItem.Mold(new Item.Properties(), TIME_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> TIME_TEMPLATE = ITEMS.register(
            "time_template",
            () -> new CastingTemplate(new Item.Properties(), TIME_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> TUNNEL_RUNE = RUNES.register(
            "tunnel_rune",
            () -> new BaseModifierRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BaseModifierRuneItem.Mold> TUNNEL_MOLD = ITEMS.register(
            "tunnel_mold",
            () -> new BaseModifierRuneItem.Mold(new Item.Properties(), TUNNEL_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> TUNNEL_TEMPLATE = ITEMS.register(
            "tunnel_template",
            () -> new CastingTemplate(new Item.Properties(), TUNNEL_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> NARROW_RUNE = RUNES.register(
            "narrow_rune",
            () -> new BaseModifierRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BaseModifierRuneItem.Mold> NARROW_MOLD = ITEMS.register(
            "narrow_mold",
            () -> new BaseModifierRuneItem.Mold(new Item.Properties(), NARROW_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> NARROW_TEMPLATE = ITEMS.register(
            "narrow_template",
            () -> new CastingTemplate(new Item.Properties(), NARROW_MOLD)
    );
    public static final DeferredHolder<Item, AbstractRuneItem> INVERT_RUNE = RUNES.register(
            "invert_rune",
            () -> new BaseModifierRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BaseModifierRuneItem.Mold> INVERT_MOLD = ITEMS.register(
            "invert_mold",
            () -> new BaseModifierRuneItem.Mold(new Item.Properties(), INVERT_RUNE)
    );
    public static final DeferredHolder<Item, CastingTemplate> INVERT_TEMPLATE = ITEMS.register(
            "invert_template",
            () -> new CastingTemplate(new Item.Properties(), INVERT_MOLD)
    );
    
    public static final DeferredHolder<Item, AbstractRuneItem> AMPLIFY_RUNE = RUNES.register(
            "amplify_rune",
            () -> new AmplifyRuneItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, AmplifyRuneItem.Mold> AMPLIFY_MOLD = ITEMS.register(
            "amplify_mold",
            () -> new AmplifyRuneItem.Mold(new Item.Properties())
    );
    public static final DeferredHolder<Item, CastingTemplate> AMPLIFY_TEMPLATE = ITEMS.register(
            "amplify_template",
            () -> new CastingTemplate(new Item.Properties(), AMPLIFY_MOLD)
    );
    
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        WOODEN_CHARMS.register(bus);
        RUNES.register(bus);
        NUGGETS.register(bus);
        INGOTS.register(bus);
        METAL_STORAGE_BLOCKS.register(bus);
        RAW_ORES.register(bus);
        ORE_BLOCKS.register(bus);
    }
}
