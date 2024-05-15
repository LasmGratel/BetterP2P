package dev.lasm.betterp2p.mixin;

import appeng.core.definitions.AEParts;
import dev.lasm.betterp2p.BetterP2P;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = AEParts.class, remap = false)
public class MixinAEParts {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        // https://fabricmc.net/wiki/documentation:entrypoint#a_note_about_load_order_and_phases_or_a_lack_thereof
        // Fabric thinks load ordering is a bad practice and shouldn't be guaranteed in the first place.
        // But you cannot ensure every modder init their objects properly.
        // In this particular case, AE2 inits their object statically. You can't just use them in setup event.

        // Fabric try to force every developer accept their "philosophies" (if could be so called).
        // Well, we have seen what happened to Rust nowadays.
        BetterP2P.INSTANCE.getLogger().info("Tunnels init");
        BetterP2P.INSTANCE.getProxy().initTunnels();
    }
}
