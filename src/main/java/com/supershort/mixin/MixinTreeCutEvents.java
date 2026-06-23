package com.supershort.mixin;

import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.natamus.treeharvester_common_fabric.events.TreeCutEvents", remap = false)
public class MixinTreeCutEvents {

    @Inject(at = @At("HEAD"), method = "onTreeHarvest", cancellable = true, remap = false)
    private static void requireTreeHarvesterPowerup(
            Level level, Player player, BlockPos blockPos,
            BlockState blockState, BlockEntity blockEntity,
            CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide()) return;

        boolean hasPowerup = false;
        if (player instanceof JobsPlayer jobsPlayer) {
            Job lumberjackJob = jobsPlayer.jobsplus$getJob(
                    Identifier.fromNamespaceAndPath("jobsplus", "lumberjack"));
            if (lumberjackJob != null) {
                hasPowerup = lumberjackJob.getPowerupManager()
                        .getPowerup(Identifier.fromNamespaceAndPath("jobplusharvest", "lumberjack/treeharvester"))
                        .map(powerup -> powerup.getState() == PowerupState.ACTIVE)
                        .orElse(false);
            }
        }

        if (!hasPowerup) {
            // Return true = normal break
            cir.setReturnValue(true);
        }
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lcom/natamus/collective_common_fabric/functions/BlockFunctions;dropBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.AFTER,
            remap = false
        ),
        method = "onTreeHarvest",
        remap = false
    )
    private static void awardXpPerHarvestedLog(
            Level level, Player player, BlockPos blockPos,
            BlockState blockState, BlockEntity blockEntity,
            CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof JobsPlayer jobsPlayer)) return;

        Job lumberjackJob = jobsPlayer.jobsplus$getJob(
                Identifier.fromNamespaceAndPath("jobsplus", "lumberjack"));
        if (lumberjackJob == null) return;

        boolean hasPowerup = lumberjackJob.getPowerupManager()
                .getPowerup(Identifier.fromNamespaceAndPath("jobplusharvest", "lumberjack/treeharvester"))
                .map(powerup -> powerup.getState() == PowerupState.ACTIVE)
                .orElse(false);

        if (hasPowerup) {
            // Award XP per extra log
            double exp = 3.0 + level.getRandom().nextInt(4);
            lumberjackJob.addExperience(exp);
        }
    }

    @Inject(at = @At("HEAD"), method = "onHarvestBreakSpeed", cancellable = true, remap = false)
    private static void requireTreeHarvesterPowerupForSpeed(
            Level level, Player player, float speed,
            BlockState blockState,
            CallbackInfoReturnable<Float> cir) {

        boolean hasPowerup = false;
        if (player instanceof JobsPlayer jobsPlayer) {
            Job lumberjackJob = jobsPlayer.jobsplus$getJob(
                    Identifier.fromNamespaceAndPath("jobsplus", "lumberjack"));
            if (lumberjackJob != null) {
                hasPowerup = lumberjackJob.getPowerupManager()
                        .getPowerup(Identifier.fromNamespaceAndPath("jobplusharvest", "lumberjack/treeharvester"))
                        .map(powerup -> powerup.getState() == PowerupState.ACTIVE)
                        .orElse(false);
            }
        }

        if (!hasPowerup) {
            // Return unmodified speed
            cir.setReturnValue(speed);
        }
    }
}
