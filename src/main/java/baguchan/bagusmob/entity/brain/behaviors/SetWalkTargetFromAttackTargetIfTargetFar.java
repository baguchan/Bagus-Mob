package baguchan.bagusmob.entity.brain.behaviors;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Optional;
import java.util.function.Function;

public class SetWalkTargetFromAttackTargetIfTargetFar {
    public static BehaviorControl<Mob> create(float p_259228_, int range) {
        return create((p_147908_) -> {
            return p_259228_;
        }, range);
    }

    public static BehaviorControl<Mob> create(Function<LivingEntity, Float> p_259507_, int range) {
        return BehaviorBuilder.create((p_258687_) -> {
            return p_258687_.group(p_258687_.registered(MemoryModuleType.WALK_TARGET), p_258687_.registered(MemoryModuleType.LOOK_TARGET), p_258687_.present(MemoryModuleType.ATTACK_TARGET), p_258687_.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(p_258687_, (p_258699_, p_258700_, p_258701_, p_258702_) -> {
                return (p_258694_, p_258695_, p_258696_) -> {
                    LivingEntity livingentity = p_258687_.get(p_258701_);
                    Optional<NearestVisibleLivingEntities> optional = p_258687_.tryGet(p_258702_);
                    if (optional.isPresent() && optional.get().contains(livingentity) && p_258695_.closerThan(livingentity, 10)) {
                        p_258699_.erase();
                    } else {
                        p_258700_.set(new EntityTracker(livingentity, true));
                        p_258699_.set(new WalkTarget(new EntityTracker(livingentity, false), p_259507_.apply(p_258695_), 0));
                    }

                    return true;
                };
            });
        });
    }
}
