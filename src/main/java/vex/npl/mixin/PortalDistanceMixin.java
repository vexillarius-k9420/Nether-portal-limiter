package vex.npl.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vex.npl.config.ConfigManager;

@Mixin(ServerPlayerEntity.class)
public abstract class PortalDistanceMixin {

    @Inject(method = "teleportTo", at = @At("HEAD"), cancellable = true)
    private void onTeleport(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        // проверка на телепортацию через портал (с 1.21)
        if (teleportTarget.postDimensionTransition() != TeleportTarget.NO_OP) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ServerWorld currentLevel = player.getServerWorld();

            // лимиты для Обычного мира
            double TpLimit = ConfigManager.INSTANCE.OverWorld.guaranteed_teleportation_distance_limit;
            double DtLimit = ConfigManager.INSTANCE.OverWorld.guaranteed_destruction_distance_limit;

            // уменьшение лимита для Незера
            if (currentLevel.getRegistryKey() == World.NETHER) {
                TpLimit = TpLimit / ConfigManager.INSTANCE.netherLimitScale;
                DtLimit = DtLimit / ConfigManager.INSTANCE.netherLimitScale;
            }

            // определение координат игрока со смещением
            double dx = player.getX() - ConfigManager.INSTANCE.Center.x;
            double dz = player.getZ() - ConfigManager.INSTANCE.Center.z;

            double distanceSq = dx * dx + dz * dz;
            double TpLimitSq = TpLimit * TpLimit;
            double DtLimitSq = DtLimit * DtLimit;

            float maxL = (float) Math.max(TpLimitSq, DtLimitSq);
            float minL = (float) Math.min(TpLimitSq, DtLimitSq);
            float probability = (float) ((distanceSq - minL) / (maxL - minL)); // прощёт вероятности разрушения
            float WRandom = currentLevel.random.nextFloat();

            // опредиление логики лимитов
            if (TpLimitSq < DtLimitSq ? WRandom < probability : WRandom > probability) {
                // определение направления: Овер->Незер ИЛИ Незер->Овер
                if ((currentLevel.getRegistryKey() == World.OVERWORLD && teleportTarget.world().getRegistryKey() == World.NETHER) ||
                        (currentLevel.getRegistryKey() == World.NETHER && teleportTarget.world().getRegistryKey() == World.OVERWORLD)) {

                    destroyPortalEffect(player, currentLevel);

                    player.sendMessage(Text.literal(ConfigManager.INSTANCE.displayMessage)
                            .formatted(Formatting.RED, Formatting.ITALIC), true);

                    cir.setReturnValue(player);
                }
            }
        }
    }

    @Unique
    // СПЕЦЭФФЕКТЫ!!!
    private void destroyPortalEffect(ServerPlayerEntity player, ServerWorld world) {
        BlockPos playerPos = player.getBlockPos();
        int radius = 4;

        // звук
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        BlockPos.stream(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))
                .forEach(pos -> {
                    // удаление блоков портала
                    if (world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }

                    // замена обсидиана
                    if (world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) {
                        if (world.random.nextFloat() < ConfigManager.INSTANCE.ObsidianReplacement) {
                            world.setBlockState(pos, Blocks.CRYING_OBSIDIAN.getDefaultState());
                        }
                    }
                });

        // частицы
        for (int i = 0; i < 40; i++) {
            world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                    player.getX() + (world.random.nextDouble() - 0.5) * 3,
                    player.getY() + world.random.nextDouble() * 3,
                    player.getZ() + (world.random.nextDouble() - 0.5) * 3,
                    1, 0, 0, 0, 0.1);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX() + (world.random.nextDouble() - 0.5) * 2,
                    player.getY() + world.random.nextDouble() * 3,
                    player.getZ() + (world.random.nextDouble() - 0.5) * 2,
                    1, 0, 0, 0, 0.1);
        }
    }
}