package baguchan.bagusmob.entity.brain;

import bagu_chan.bagus_lib.entity.brain.behaviors.AttackWithAnimation;
import bagu_chan.bagus_lib.util.BrainUtils;
import baguchan.bagusmob.entity.RudeHog;
import baguchan.bagusmob.entity.brain.behaviors.NoticedHoglin;
import baguchan.bagusmob.entity.brain.behaviors.Pacified;
import baguchan.bagusmob.registry.ModEntityRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RudeHogAi {
    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final int PLAYER_ANGER_RANGE = 16;
    private static final int ANGER_DURATION = 600;
    private static final int ADMIRE_DURATION = 120;
    private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
    private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
    private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
    private static final int CELEBRATION_TIME = 300;
    protected static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
    private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
    private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
    private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final int EAT_COOLDOWN = 200;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
    private static final int INTERACTION_RANGE = 8;
    private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
    private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

    public static Brain<?> makeBrain(RudeHog p_34841_, Brain<RudeHog> p_34842_) {
        initCoreActivity(p_34842_);
        initIdleActivity(p_34842_);
        initFightActivity(p_34841_, p_34842_);
        initCelebrateActivity(p_34842_);
        initRetreatActivity(p_34842_);
        initRideHoglinActivity(p_34842_);
        p_34842_.setCoreActivities(ImmutableSet.of(Activity.CORE));
        p_34842_.setDefaultActivity(Activity.IDLE);
        p_34842_.useDefaultActivity();
        return p_34842_;
    }

    public static void initMemories(RudeHog p_219206_, RandomSource p_219207_) {
        int i = TIME_BETWEEN_HUNTS.sample(p_219207_);
        p_219206_.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long) i);
    }

    private static void initCoreActivity(Brain<RudeHog> p_34821_) {
        p_34821_.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), StopHoldingItemIfNoLongerAdmiring.create(), StartAdmiringItemIfSeen.create(120), StartCelebratingIfTargetDead.create(300, RudeHogAi::wantsToDance), StopBeingAngryIfTargetDead.create()));
    }

    private static void initIdleActivity(Brain<RudeHog> p_34892_) {
        p_34892_.addActivity(Activity.IDLE, 10, ImmutableList.of(Pacified.create(), SetEntityLookTarget.create(RudeHogAi::isPlayerHoldingLovedItem, 14.0F), StartAttacking.<Piglin>create(AbstractPiglin::isAdult, RudeHogAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(RudeHog::canHunt, StartHuntingHoglin.create()), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(RudeHog p_34904_, Brain<RudeHog> p_34905_) {
        p_34905_.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<BehaviorControl<? super RudeHog>>of(Pacified.create(), StopAttackingIfTargetInvalid.<Piglin>create((p_34981_) -> {
            return !isNearestValidAttackTarget(p_34904_, p_34981_);
        }), new AttackWithAnimation<>(p_34904_.attackAnimationLength - p_34904_.attackAnimationActionPoint, p_34904_.attackAnimationLength, 40, 1.0F), new NoticedHoglin<>(), RememberIfHoglinWasKilled.create()), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Brain<RudeHog> p_34921_) {
        p_34921_.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, ImmutableList.of(SetEntityLookTarget.create(RudeHogAi::isPlayerHoldingLovedItem, 14.0F), StartAttacking.<Piglin>create(AbstractPiglin::isAdult, RudeHogAi::findNearestValidAttackTarget), BehaviorBuilder.<Piglin>triggerIf((p_34804_) -> {
            return !p_34804_.isDancing();
        }, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)), BehaviorBuilder.<Piglin>triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)), new RunOne<Piglin>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(ModEntityRegistry.RUDEHOG.get(), 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(RandomStroll.stroll(0.6F, 2, 1), 1), Pair.of(new DoNothing(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initRetreatActivity(Brain<RudeHog> p_34959_) {
    }

    private static void initRideHoglinActivity(Brain<RudeHog> p_34974_) {
        p_34974_.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(Mount.create(0.8F), SetEntityLookTarget.create(RudeHogAi::isPlayerHoldingLovedItem, 8.0F), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(ImmutableList.<Pair<? extends Trigger<? super LivingEntity>, Integer>>builder().addAll(createLookBehaviors()).add(Pair.of(BehaviorBuilder.triggerIf((p_258950_) -> {
            return true;
        }), 1)).build())), DismountOrSkipMounting.<Piglin>create(8, RudeHogAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(SetEntityLookTarget.create(ModEntityRegistry.RUDEHOG.get(), 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1));
    }

    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder().addAll(createLookBehaviors()).add(Pair.of(new DoNothing(30, 60), 1)).build());
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(InteractWith.of(ModEntityRegistry.RUDEHOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(RudeHogAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static BehaviorControl<PathfinderMob> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }

    private static BehaviorControl<Piglin> babyAvoidNemesis() {
        return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static boolean isBabyRidingBaby(Piglin p_34993_) {
        if (!p_34993_.isBaby()) {
            return false;
        } else {
            Entity entity = p_34993_.getVehicle();
            return entity instanceof Piglin && ((Piglin) entity).isBaby() || entity instanceof Hoglin && ((Hoglin) entity).isBaby();
        }
    }


    private static void holdInOffhand(RudeHog p_34933_, ItemStack p_34934_) {
        if (isHoldingItemInOffHand(p_34933_)) {
            p_34933_.spawnAtLocation(p_34933_.getItemInHand(InteractionHand.OFF_HAND));
        }

        p_34933_.holdInOffHand(p_34934_);
    }

    private static void putInInventory(RudeHog p_34953_, ItemStack p_34954_) {
        ItemStack itemstack = p_34953_.addToInventory(p_34954_);
        throwItemsTowardRandomPos(p_34953_, Collections.singletonList(itemstack));
    }

    private static void throwItems(Piglin p_34861_, List<ItemStack> p_34862_) {
        Optional<Player> optional = p_34861_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            throwItemsTowardPlayer(p_34861_, optional.get(), p_34862_);
        } else {
            throwItemsTowardRandomPos(p_34861_, p_34862_);
        }

    }

    private static void throwItemsTowardRandomPos(Piglin p_34913_, List<ItemStack> p_34914_) {
        throwItemsTowardPos(p_34913_, p_34914_, getRandomNearbyPos(p_34913_));
    }

    private static void throwItemsTowardPlayer(Piglin p_34851_, Player p_34852_, List<ItemStack> p_34853_) {
        throwItemsTowardPos(p_34851_, p_34853_, p_34852_.position());
    }

    private static void throwItemsTowardPos(Piglin p_34864_, List<ItemStack> p_34865_, Vec3 p_34866_) {
        if (!p_34865_.isEmpty()) {
            p_34864_.swing(InteractionHand.OFF_HAND);

            for (ItemStack itemstack : p_34865_) {
                BehaviorUtils.throwItem(p_34864_, itemstack, p_34866_.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    public static void stopHoldingOffHandItem(RudeHog p_34868_, boolean p_34869_) {
        ItemStack itemstack = p_34868_.getItemInHand(InteractionHand.OFF_HAND);
        p_34868_.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (p_34868_.isAdult()) {
            boolean flag = itemstack.isPiglinCurrency();
            if (p_34869_ && flag) {
                throwItems(p_34868_, getBarterResponseItems(p_34868_));
            } else if (!flag) {
                boolean flag1 = !p_34868_.equipItemIfPossible(itemstack).isEmpty();
                if (!flag1) {
                    putInInventory(p_34868_, itemstack);
                }
            }
        } else {
            boolean flag2 = !p_34868_.equipItemIfPossible(itemstack).isEmpty();
            if (!flag2) {
                ItemStack itemstack1 = p_34868_.getMainHandItem();
                if (isLovedItem(itemstack1)) {
                    putInInventory(p_34868_, itemstack1);
                } else {
                    throwItems(p_34868_, Collections.singletonList(itemstack1));
                }

                p_34868_.holdInMainHand(itemstack);
            }
        }

    }

    private static List<ItemStack> getBarterResponseItems(Piglin p_34997_) {
        LootTable loottable = p_34997_.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel) p_34997_.level())).withParameter(LootContextParams.THIS_ENTITY, p_34997_).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }

    private static boolean wantsToDance(LivingEntity p_34811_, LivingEntity p_34812_) {
        if (p_34812_.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return RandomSource.create(p_34811_.level().getGameTime()).nextFloat() < 0.1F;
        }
    }

    protected static boolean wantsToPickup(RudeHog p_34858_, ItemStack p_34859_) {
        if (p_34858_.isBaby() && p_34859_.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        } else if (p_34859_.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        } else if (isAdmiringDisabled(p_34858_) && p_34858_.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (p_34859_.isPiglinCurrency()) {
            return isNotHoldingLovedItemInOffHand(p_34858_);
        } else {
            boolean flag = p_34858_.canAddToInventory(p_34859_);
            if (p_34859_.is(Items.GOLD_NUGGET)) {
                return flag;
            } else if (isFood(p_34859_)) {
                return !hasEatenRecently(p_34858_) && flag;
            } else if (!isLovedItem(p_34859_)) {
                return p_34858_.canReplaceCurrentItem(p_34859_);
            } else {
                return isNotHoldingLovedItemInOffHand(p_34858_) && flag;
            }
        }
    }

    public static boolean isLovedItem(ItemStack p_149966_) {
        return p_149966_.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(Piglin p_34835_, Entity p_34836_) {
        if (!(p_34836_ instanceof Mob mob)) {
            return false;
        } else {
            return !mob.isBaby() || !mob.isAlive() || wasHurtRecently(p_34835_) || wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
        }
    }

    private static boolean isNearestValidAttackTarget(Piglin p_34901_, LivingEntity p_34902_) {
        return findNearestValidAttackTarget(p_34901_).filter((p_34887_) -> {
            return p_34887_ == p_34902_;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin p_35001_) {
        Brain<Piglin> brain = p_35001_.getBrain();
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(p_35001_, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && BrainUtils.isEntityAttackableIgnoringLineOfSight(p_35001_, optional.get(), 24)) {
            return optional;
        } else {
            if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                Optional<Player> optional1 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                if (optional1.isPresent()) {
                    return optional1;
                }
            }

            Optional<Mob> optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            if (optional3.isPresent()) {
                return optional3;
            } else {
                Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(p_35001_, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                return optional1.isPresent() ? optional1 : Optional.empty();
            }
        }
    }

    private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin p_35092_, MemoryModuleType<? extends LivingEntity> p_35093_) {
        return p_35092_.getBrain().getMemory(p_35093_).filter((p_35108_) -> {
            return p_35108_.closerThan(p_35092_, 24.0D);
        });
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin p_34948_) {
        return p_34948_.getBrain().getActiveNonCoreActivity().map((p_34908_) -> {
            return getSoundForActivity(p_34948_, p_34908_);
        });
    }

    private static SoundEvent getSoundForActivity(Piglin p_34855_, Activity p_34856_) {
        if (p_34856_ == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        } else if (p_34855_.isConverting()) {
            return SoundEvents.PIGLIN_RETREAT;
        } else if (p_34856_ == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        } else if (p_34856_ == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        } else if (seesPlayerHoldingLovedItem(p_34855_)) {
            return SoundEvents.PIGLIN_JEALOUS;
        } else {
            return isNearRepellent(p_34855_) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
        }
    }

    protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin p_35005_) {
        return p_35005_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin p_34961_) {
        return p_34961_.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static void stopWalking(Piglin p_35007_) {
        p_35007_.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        p_35007_.getNavigation().stop();
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker setentitylooktargetsometimes$ticker = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create((p_289472_) -> {
            return p_289472_.isBaby() && setentitylooktargetsometimes$ticker.tickDownAndCheck(p_289472_.level().random);
        }, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
    }

    public static Optional<LivingEntity> getAvoidTarget(Piglin p_34987_) {
        return p_34987_.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? p_34987_.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin p_34894_) {
        return p_34894_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? p_34894_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    private static void broadcastRetreat(Piglin p_34930_, LivingEntity p_34931_) {
        getVisibleAdultPiglins(p_34930_).stream().filter((p_34985_) -> {
            return p_34985_ instanceof Piglin;
        }).forEach((p_34819_) -> {
            retreatFromNearestTarget((Piglin) p_34819_, p_34931_);
        });
    }

    private static void retreatFromNearestTarget(Piglin p_34950_, LivingEntity p_34951_) {
        Brain<Piglin> brain = p_34950_.getBrain();
        LivingEntity $$3 = BehaviorUtils.getNearestTarget(p_34950_, brain.getMemory(MemoryModuleType.AVOID_TARGET), p_34951_);
        $$3 = BehaviorUtils.getNearestTarget(p_34950_, brain.getMemory(MemoryModuleType.ATTACK_TARGET), $$3);
        setAvoidTargetAndDontHuntForAWhile(p_34950_, $$3);
    }

    private static boolean wantsToStopFleeing(Piglin p_35009_) {
        Brain<Piglin> brain = p_35009_.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity livingentity = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> entitytype = livingentity.getType();
            if (entitytype == EntityType.HOGLIN) {
                return piglinsEqualOrOutnumberHoglins(p_35009_);
            } else if (isZombified(entitytype)) {
                return !brain.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingentity);
            } else {
                return false;
            }
        }
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin p_35011_) {
        return !hoglinsOutnumberPiglins(p_35011_);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin p_35013_) {
        int i = p_35013_.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = p_35013_.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return j > i;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin p_34968_, LivingEntity p_34969_) {
        p_34968_.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        p_34968_.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        p_34968_.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        p_34968_.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, p_34969_, (long) RETREAT_DURATION.sample(p_34968_.level().random));
        dontKillAnyMoreHoglinsForAWhile(p_34968_);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin p_34923_) {
        p_34923_.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long) TIME_BETWEEN_HUNTS.sample(p_34923_.level().random));
    }

    private static void eat(Piglin p_35015_) {
        p_35015_.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin p_35017_) {
        Vec3 vec3 = LandRandomPos.getPos(p_35017_, 4, 2);
        return vec3 == null ? p_35017_.position() : vec3;
    }

    private static boolean hasEatenRecently(Piglin p_35019_) {
        return p_35019_.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(AbstractPiglin p_34943_) {
        return p_34943_.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity p_34919_) {
        return p_34919_.isHolding(is -> is.getItem() instanceof net.minecraft.world.item.CrossbowItem);
    }

    private static void admireGoldItem(LivingEntity p_34939_) {
        p_34939_.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean isAdmiringItem(Piglin p_35021_) {
        return p_35021_.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(ItemStack p_149968_) {
        return p_149968_.is(BARTERING_ITEM);
    }

    private static boolean isFood(ItemStack p_149970_) {
        return p_149970_.is(ItemTags.PIGLIN_FOOD);
    }

    private static boolean isNearRepellent(Piglin p_35023_) {
        return p_35023_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity p_34972_) {
        return p_34972_.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity p_34983_) {
        return !seesPlayerHoldingLovedItem(p_34983_);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity p_34884_) {
        return p_34884_.getType() == EntityType.PLAYER && p_34884_.isHolding(RudeHogAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(Piglin p_35025_) {
        return p_35025_.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity p_34989_) {
        return p_34989_.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(Piglin p_35027_) {
        return !p_35027_.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin p_35029_) {
        return p_35029_.getOffhandItem().isEmpty() || !isLovedItem(p_35029_.getOffhandItem());
    }

    public static boolean isZombified(EntityType<?> p_34807_) {
        return p_34807_ == EntityType.ZOMBIFIED_PIGLIN || p_34807_ == EntityType.ZOGLIN;
    }
}
