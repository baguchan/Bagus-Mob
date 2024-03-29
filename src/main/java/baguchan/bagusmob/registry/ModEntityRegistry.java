package baguchan.bagusmob.registry;

import baguchan.bagusmob.BagusMob;
import baguchan.bagusmob.entity.*;
import baguchan.bagusmob.entity.projectile.SpinBlade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = BagusMob.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES_REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, BagusMob.MODID);

    public static final Supplier<EntityType<Tengu>> TENGU = ENTITIES_REGISTRY.register("tengu", () -> EntityType.Builder.of(Tengu::new, MobCategory.MONSTER).sized(0.6F, 2.0F).clientTrackingRange(8).build(prefix("tengu")));
    public static final Supplier<EntityType<Ninjar>> NINJAR = ENTITIES_REGISTRY.register("ninjar", () -> EntityType.Builder.of(Ninjar::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(prefix("ninjar")));
    public static final Supplier<EntityType<RudeHog>> RUDEHOG = ENTITIES_REGISTRY.register("rudehog", () -> EntityType.Builder.of(RudeHog::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(prefix("rudehog")));
    public static final Supplier<EntityType<BurnerHog>> BURNER_HOG = ENTITIES_REGISTRY.register("burner_hog", () -> EntityType.Builder.of(BurnerHog::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(prefix("burner_hog")));
    public static final Supplier<EntityType<PotSnake>> POT_SNAKE = ENTITIES_REGISTRY.register("pot_snake", () -> EntityType.Builder.of(PotSnake::new, MobCategory.MONSTER).sized(0.6F, 0.6F).clientTrackingRange(8).build(prefix("pot_snake")));
    public static final Supplier<EntityType<Samurai>> SAMURAI = ENTITIES_REGISTRY.register("samurai", () -> EntityType.Builder.of(Samurai::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(prefix("samurai")));

    public static final Supplier<EntityType<HunterBoar>> HUNTER_BOAR = ENTITIES_REGISTRY.register("hunter_boar", () -> EntityType.Builder.of(HunterBoar::new, MobCategory.MONSTER).sized(1.3964844F, 1.4F).clientTrackingRange(8).build(prefix("hunter_boar")));
    public static final Supplier<EntityType<SpinBlade>> SPIN_BLADE = ENTITIES_REGISTRY.register("spin_blade", () -> EntityType.Builder.<SpinBlade>of(SpinBlade::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(20).build(prefix("spin_blade")));


    public static final Supplier<EntityType<SlashAir>> SLASH_AIR = ENTITIES_REGISTRY.register("slash_air", () -> EntityType.Builder.<SlashAir>of(SlashAir::new, MobCategory.MONSTER).sized(1.0F, 1.0F).clientTrackingRange(8).build(prefix("slash_air")));

	@SubscribeEvent
	public static void registerEntityAttribute(EntityAttributeCreationEvent event) {
		event.put(TENGU.get(), Tengu.createAttributes().build());
        event.put(POT_SNAKE.get(), PotSnake.createAttributes().build());
        event.put(SAMURAI.get(), Samurai.createAttributes().build());
		event.put(NINJAR.get(), Ninjar.createAttributes().build());
		event.put(RUDEHOG.get(), RudeHog.createAttributes().build());
        event.put(BURNER_HOG.get(), BurnerHog.createAttributes().build());
		event.put(HUNTER_BOAR.get(), HunterBoar.createAttributes().build());
	}

	@SubscribeEvent
	public static void registerEntityAttribute(SpawnPlacementRegisterEvent event) {
		event.register(TENGU.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(POT_SNAKE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(SAMURAI.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(NINJAR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(RUDEHOG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RudeHog::checkRudeHogSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(BURNER_HOG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
	}


	private static String prefix(String path) {
		return BagusMob.MODID + "." + path;
	}
}