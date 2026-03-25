package net.inklinggamer.shopsandtools.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

public final class CelestiumTrialChamberMarkerIrisCompat {
    private static boolean registrationAttempted;

    private CelestiumTrialChamberMarkerIrisCompat() {
    }

    public static synchronized void register() {
        if (registrationAttempted) {
            return;
        }

        registrationAttempted = true;

        try {
            IrisApi irisApi = IrisApi.getInstance();
            if (irisApi.getMinorApiRevision() < 3) {
                ShopsAndTools.LOGGER.warn("Skipping Celestium trial chamber marker Iris integration because the installed Iris API is too old for assignPipeline support");
                return;
            }

            RenderPipeline pipeline = CelestiumTrialChamberMarkerRenderer.getOrCreatePipeline();
            if (pipeline == null) {
                return;
            }

            irisApi.assignPipeline(pipeline, IrisProgram.LINES);
            ShopsAndTools.LOGGER.info("Registered the Celestium trial chamber marker pipeline with Iris line rendering");
        } catch (RuntimeException | LinkageError exception) {
            ShopsAndTools.LOGGER.warn("Failed to register the Celestium trial chamber marker with Iris; falling back to vanilla-compatible rendering", exception);
        }
    }
}
