package baguchan.bagusmob.client.render;

import bagu_chan.bagus_lib.client.layer.CustomArmorLayer;
import baguchan.bagusmob.BagusMob;
import baguchan.bagusmob.client.ModModelLayers;
import baguchan.bagusmob.client.model.NinjarModel;
import baguchan.bagusmob.entity.Ninjar;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NinjarRenderer<T extends Ninjar> extends MobRenderer<T, NinjarModel<T>> {
	private static final ResourceLocation ILLAGER = ResourceLocation.fromNamespaceAndPath(BagusMob.MODID, "textures/entity/ninjar.png");

	public NinjarRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new NinjarModel<>(renderManagerIn.bakeLayer(ModModelLayers.NINJAR)), 0.5F);
		this.addLayer(new CustomArmorLayer<>(this, renderManagerIn));
		this.addLayer(new ItemInHandLayer<>(this, renderManagerIn.getItemInHandRenderer()));
	}

	@Override
	public ResourceLocation getTextureLocation(T p_110775_1_) {
		return ILLAGER;
	}
}