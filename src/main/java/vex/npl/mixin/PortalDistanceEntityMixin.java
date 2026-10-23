package vex.npl.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vex.npl.PortalTeleport;

@Mixin(Entity.class)
public abstract class PortalDistanceEntityMixin {
    //? if 1.20-fabric {
    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void onTeleport(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        Entity entity = (Entity) (Object) this;
        if (PortalTeleport.onTp(destination, entity) && !entity.isPlayer()) {
            cir.setReturnValue(entity); // отмена телепортации сущности
        }
    //?} elif >=1.21-fabric {
    /*@Inject(method = "teleportTo", at = @At("HEAD"), cancellable = true)
    private void onTeleport(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        Entity entity = (Entity) (Object) this;
        if (PortalTeleport.onTp(teleportTarget, entity) && !entity.isPlayer()) {
            cir.setReturnValue(entity); // отмена телепортации сущности
        }
    *///?}

    }
}