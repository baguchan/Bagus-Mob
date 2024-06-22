package baguchan.bagusmob.client.render;

import bagu_chan.bagus_lib.client.layer.CustomArmorLayer;
import baguchan.bagusmob.BagusMob;
import baguchan.bagusmob.client.ModModelLayers;
import baguchan.bagusmob.client.model.RudeHogModel;
import baguchan.bagusmob.entity.RudeHog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RudeHogRenderer<T extends RudeHog> extends MobRenderer<T, RudeHogModel<T>> {
    private static final ResourceLocation RUDEHOG = ResourceLocation.fromNamespaceAndPath(BagusMob.MODID, "textures/entity/piglin/rudehog.png");

    public RudeHogRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new RudeHogModel<>(renderManagerIn.bakeLayer(ModModelLayers.RUDEHOG)), 0.5F);
        this.addLayer(new CustomArmorLayer<>(this, renderManagerIn));
        this.addLayer(new ItemInHandLayer<>(this, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer()));
    }

    protected boolean isShaking(T p_114864_) {
        return super.isShaking(p_114864_) || p_114864_.isConverting();
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return RUDEHOG;
    }
}
