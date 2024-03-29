package de.melanx.vanillaaiots.items;

import de.melanx.vanillaaiots.compat.LibCompat;
import de.melanx.vanillaaiots.data.AIOTTags;
import de.melanx.vanillaaiots.tools.ToolMaterials;
import de.melanx.vanillaaiots.util.ComponentUtil;
import de.melanx.vanillaaiots.util.ToolUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BaseAiot extends DiggerItem {

    private final boolean isVanilla;

    public BaseAiot(float attackDamageModifier, float attackSpeedModifier, Tier tier, Properties properties) {
        super(attackDamageModifier, attackSpeedModifier, tier, AIOTTags.MINEABLE_WITH_AIOT, properties);
        this.isVanilla = tier == ToolMaterials.WOODEN
                || tier == ToolMaterials.STONE
                || tier == ToolMaterials.IRON
                || tier == ToolMaterials.GOLDEN
                || tier == ToolMaterials.DIAMOND
                || tier == ToolMaterials.NETHERITE;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        ItemStack item = context.getItemInHand();
        boolean hoemode = isHoemode(item);

        InteractionResult result = InteractionResult.PASS;
        for (ToolAction action : ToolActions.DEFAULT_AXE_ACTIONS) {
            if (result != InteractionResult.PASS) {
                break;
            }

            result = ToolUtil.toolUse(context, action);
        }

        if (result == InteractionResult.PASS) {
            if (hoemode) {
                result = ToolUtil.toolUse(context, ToolActions.HOE_TILL);
            } else {
                result = ToolUtil.toolUse(context, ToolActions.SHOVEL_FLATTEN);
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isCrouching()) {
            Style dark_blue = Style.EMPTY.withColor(ChatFormatting.DARK_BLUE);
            Style aqua = Style.EMPTY.withColor(ChatFormatting.AQUA);
            MutableComponent text = ComponentUtil.getTooltip("toggleMode").append(": ").withStyle(dark_blue);
            MutableComponent pathMode = ComponentUtil.getTooltip("pathMode", Blocks.DIRT_PATH.getName().getString()).withStyle(aqua);
            MutableComponent hoeMode = ComponentUtil.getTooltip("hoeMode").withStyle(aqua);

            if (isHoemode(stack)) {
                setHoemode(stack, false);
                text = text.append(pathMode);
            } else {
                setHoemode(stack, true);
                text = text.append(hoeMode);
            }

            player.displayClientMessage(text, true);

            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public int getBurnTime(@Nonnull ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return this.getTier() == ToolMaterials.WOODEN ? 400 : 0;
    }

    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        if (state.is(Blocks.COBWEB)) {
            return 15.0F;
        }

        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolUtil.DEFAULT_AIOT_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return (this.getTier() != ToolMaterials.SLIME || enchantment != Enchantments.KNOCKBACK)
                && (enchantment.category == EnchantmentCategory.WEAPON || super.canApplyAtEnchantingTable(stack, enchantment));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag isAdvanced) {
        if (LibCompat.isMoreVanillaLibLoaded()) {
            LibCompat.editHoverText(this, stack, level, tooltip, isAdvanced);
        }

        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    private static void setHoemode(ItemStack stack, boolean b) {
        stack.getOrCreateTag().putBoolean("hoemode", b);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.KNOCKBACK && stack.getItem() instanceof BaseAiot item && item.getTier() == ToolMaterials.SLIME) {
            return 3;
        }

        return super.getEnchantmentLevel(stack, enchantment);
    }

    private static boolean isHoemode(ItemStack stack) {
        return stack.isEmpty() || !stack.getOrCreateTag().contains("hoemode") || stack.getOrCreateTag().getBoolean("hoemode");
    }
}
