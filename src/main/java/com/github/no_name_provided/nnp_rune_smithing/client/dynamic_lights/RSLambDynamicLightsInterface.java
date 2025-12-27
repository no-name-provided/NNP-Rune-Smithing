package com.github.no_name_provided.nnp_rune_smithing.client.dynamic_lights;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.neoforged.fml.ModList;

/**
 * References to the LambDynamic Lights API from my out-of-package code are siloed in this helper class
 * to avoid class def not found errors. All calls should be gated behind a ModList or LoadingModList check.
 */
public class RSLambDynamicLightsInterface {
    public static boolean isLoaded() {
        return ModList.get().isLoaded("lambdynlights") || ModList.get().isLoaded("lambdynlights_runtime");
    }
    public static void addDynamicLight(DynamicLightBehavior lightBehavior) {
        RSLambDynamicLightsInitializer.behaviorManager.add(lightBehavior);
    }
    public static void removeDynamicLight(DynamicLightBehavior lightBehavior) {
        RSLambDynamicLightsInitializer.behaviorManager.remove(lightBehavior);
    }
}
