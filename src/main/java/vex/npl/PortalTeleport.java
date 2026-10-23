package vex.npl;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vex.npl.config.ConfigManager;

public class PortalTeleport {

    //? if 1.20-fabric {
    public static boolean onTp(ServerWorld destination, Entity entity) {
    //?} elif >=1.21-fabric {
    /*public static boolean onTp(TeleportTarget teleportTarget, Entity entity) {
        if (teleportTarget == null) return false;
        ServerWorld destination = teleportTarget.world();
    *///?}

        World currentLevel = entity.getWorld();

        // проверки на телепортацию через портал (1.21+)
        //? if 1.21-fabric {
        /*if (teleportTarget.postDimensionTransition() != TeleportTarget.NO_OP) {
        *///?} elif 1.21.5-fabric {
        /*if (teleportTarget.postTeleportTransition() != TeleportTarget.NO_OP) {
        *///?}

            double TpLimit = ConfigManager.INSTANCE.OverWorld.guaranteed_teleportation_distance_limit;
            double DtLimit = ConfigManager.INSTANCE.OverWorld.guaranteed_destruction_distance_limit;

            float probability = ProbabilityCalculation.getProbability(entity);
            float WRandom = currentLevel.random.nextFloat();

            // опредиление логики лимитов
            if (TpLimit < DtLimit ? WRandom < probability : WRandom > probability) {
                // определение направления: Овер->Незер ИЛИ Незер->Овер
                if ((currentLevel.getRegistryKey() == World.OVERWORLD && destination.getRegistryKey() == World.NETHER) ||
                        (currentLevel.getRegistryKey() == World.NETHER && destination.getRegistryKey() == World.OVERWORLD)) {

                    destroyPortalEffect(entity, (ServerWorld) currentLevel);

                    if (entity.isPlayer()) {
                        ((ServerPlayerEntity) entity).sendMessage(Text.literal(ConfigManager.INSTANCE.displayMessage)
                            .formatted(Formatting.RED, Formatting.ITALIC), true);
                    }
                    return true;
                }
                //? if >=1.21-fabric {
            /*}
            *///?}
        }
        return false;
    }

    // СПЕЦЭФФЕКТЫ!!!
    public static void destroyPortalEffect(Entity entity, ServerWorld world) {
        BlockPos playerPos = entity.getBlockPos();
        int radius = 4;

        // звук
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
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
        for (int i = 0; i < 30; i++) {
            world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                entity.getX() + (world.random.nextDouble() - 0.5) * 3,
                entity.getY() + world.random.nextDouble() * 3,
                entity.getZ() + (world.random.nextDouble() - 0.5) * 3,
                1, 0, 0, 0, 0.07);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                entity.getX() + (world.random.nextDouble() - 0.5) * 2,
                entity.getY() + world.random.nextDouble() * 3,
                entity.getZ() + (world.random.nextDouble() - 0.5) * 2,
                1, 0, 0, 0, 0.1);
        }
    }
}
