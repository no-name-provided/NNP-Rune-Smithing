package com.github.no_name_provided.nnp_rune_smithing.client.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class exists for one purpose: stop NeoForge from replacing our [native language]
 * config variables with annoyingly verbose translation keys when those keys aren't defined.
 *
 * <p>Alternatively, you could add a bunch of ["config option key": "config option key"] entries
 * to your localization file. That would be brittle and overly verbose, so it's discouraged.</p>
 **/
@ParametersAreNonnullByDefault @MethodsReturnNonnullByDefault
public class SensibleConfigurationScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    
    
    /**
     * Constructs a new section screen for the top-most section in a {@link ModConfig}.
     *
     * @param parent    The screen to return to when the user presses escape or the "Done" button.
     *                  If this is a {@link ConfigurationScreen}, additional information is passed before closing.
     * @param type      The {@link ModConfig.Type} this configuration is for. Only used to generate the title of the screen.
     * @param modConfig The actual config to show and edit.
     * @param title     The title of the screen.
     **/
    public SensibleConfigurationScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }
    
    /**
     * Check for the existence of a translation key's definition <i>before</i> replacing our sensible config names
     * with that programmatically generated key. Also bypasses log spam (developer warnings).
     **/
    @Override
    protected MutableComponent getTranslationComponent(String key) {
        if (I18n.exists(key)) {
            return super.getTranslationComponent(key);
        } else {
            return Component.literal(key);
        }
    }
    
    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new ConfigurationScreen(
                        container,
                        parent,
                        SensibleConfigurationScreen::new
                )
        );
    }
    
}

