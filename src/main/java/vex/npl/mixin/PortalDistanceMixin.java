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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vex.npl.config.ModConfig;

@Mixin(ServerPlayerEntity.class)
public abstract class PortalDistanceMixin {

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void onTeleport(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld currentLevel = player.getServerWorld();

        // Базовый лимит для Обычного мира
        double limit = ModConfig.overworldLimit;

        // Если мы в Незере, уменьшаем лимит
        if (currentLevel.getRegistryKey() == World.NETHER) {
            limit = ModConfig.overworldLimit / ModConfig.netherLimitScale;
        }

        double dx = player.getX();
        double dz = player.getZ();
        double distanceSq = dx * dx + dz * dz;
        double limitSq = limit * limit;

        if (distanceSq > limitSq) {
            // Проверяем направления: Овер -> Незер ИЛИ Незер -> Овер
            if ((currentLevel.getRegistryKey() == World.OVERWORLD && destination.getRegistryKey() == World.NETHER) ||
                    (currentLevel.getRegistryKey() == World.NETHER && destination.getRegistryKey() == World.OVERWORLD)) {

                destroyPortalEffect(player, currentLevel);

                player.sendMessage(Text.literal(ModConfig.displayMessage)
                        .formatted(Formatting.RED, Formatting.ITALIC), true);

                cir.setReturnValue(player);
            }
        }
    }

    @Unique
    private void destroyPortalEffect(ServerPlayerEntity player, ServerWorld world) {
        BlockPos playerPos = player.getBlockPos();
        int radius = 4;

        // Эффекты звука
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        BlockPos.stream(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))
                .forEach(pos -> {
                    // Удаляем блоки портала
                    if (world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }

                    // Заменяем обсидиан
                    if (world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) {
                        if (world.random.nextFloat() < ModConfig.probabilityOfObsidianReplacement) {
                            world.setBlockState(pos, Blocks.CRYING_OBSIDIAN.getDefaultState());
                        }
                    }
                });

        // Частицы
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