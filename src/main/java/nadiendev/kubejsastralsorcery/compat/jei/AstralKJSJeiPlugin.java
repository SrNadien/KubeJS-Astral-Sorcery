package nadiendev.kubejsastralsorcery.compat.jei;

import hellfirepvp.astralsorcery.common.integration.jei.category.AltarRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.altar.AltarBlockBuilder;
import nadiendev.kubejsastralsorcery.altar.AltarRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class AstralKJSJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return AstralSorceryKJS.id("jei");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (AltarBlockBuilder builder : AltarRegistration.builders()) {
            ItemStack stack = new ItemStack(builder.get());

            if (!stack.isEmpty()) {
                registration.addRecipeCatalyst(stack, AltarRecipeCategory.RECIPE_TYPE);
            }
        }
    }
}
