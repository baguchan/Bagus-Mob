package baguchan.bagusmob.entity.goal;

import baguchan.bagusmob.entity.Tengu;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class JumpTheSkyGoal extends Goal {
	public final Tengu tengu;
	private int cooldownTime;

	public int tick;
	private static final UniformInt TIME_BETWEEN_JUMP = UniformInt.of(200, 400);

	public JumpTheSkyGoal(Tengu tengu) {
		this.tengu = tengu;
	}

	@Override
	public boolean canUse() {
		if(--this.cooldownTime <= 0){
			if (this.tengu.onGround() && this.tengu.isAggressive()) {
				this.cooldownTime = TIME_BETWEEN_JUMP.sample(this.tengu.getRandom());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.tick < 6;
	}

	@Override
	public void start() {
		super.start();
		this.tick = 0;
		Vec3 vec3 = this.tengu.getDeltaMovement();
		this.tengu.setDeltaMovement(vec3.x, vec3.y + 1.0F, vec3.z);
	}

	@Override
	public void tick() {
		super.tick();

		if(this.tick++ == 3) {
			this.tengu.startFallFlying();
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
