package vex.npl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import vex.npl.config.ConfigManager;
import net.minecraft.entity.Entity;

public class ProbabilityCalculation {
    public static float getProbability(Entity entity) {
        World currentLevel = entity.getWorld();

        // лимиты для Обычного мира
        float TpLimit = (float) ConfigManager.INSTANCE.OverWorld.guaranteed_teleportation_distance_limit;
        float DtLimit = (float) ConfigManager.INSTANCE.OverWorld.guaranteed_destruction_distance_limit;

        // уменьшение лимита для Незера
        if (currentLevel.getRegistryKey() == World.NETHER) {
            TpLimit = (float) (TpLimit / ConfigManager.INSTANCE.netherLimitScale);
            DtLimit = (float) (DtLimit / ConfigManager.INSTANCE.netherLimitScale);
        }
        // определение координат игрока со смещением
        double dx = entity.getX() - ConfigManager.INSTANCE.Center.x;
        double dz = entity.getZ() - ConfigManager.INSTANCE.Center.z;

        double distanceSq = dx * dx + dz * dz;
        double TpLimitSq = TpLimit * TpLimit;
        double DtLimitSq = DtLimit * DtLimit;

        float maxL = (float) Math.max(TpLimitSq, DtLimitSq);
        float minL = (float) Math.min(TpLimitSq, DtLimitSq);
        return (float) ((distanceSq - minL) / (maxL - minL)); // прощёт вероятности разрушения
    }

}
