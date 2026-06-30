package com.example.undertale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.List;

/**
 * The Real Knife: espada com dano infinito, igual à Sword of the Cosmos (Avaritia).
 * Dá hitkill (mata em um golpe) em QUALQUER ser vivo, incluindo mobs de outros mods,
 * mobs marcados como invulneráveis e bosses com múltiplas partes (ex: Ender Dragon).
 *
 * O dano/velocidade vêm de Item.Properties.attributes(...) no ModItems (no 1.21.1 o
 * SwordItem só recebe o Tier + Properties).
 */
public class TheRealKnifeItem extends SwordItem {

    public TheRealKnifeItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        Level level = player.level();

        // Só roda no servidor para não duplicar a lógica
        if (level.isClientSide()) {
            return false;
        }

        DamageSource source = player.damageSources().playerAttack(player);

        // Bosses/entidades com múltiplas partes (ex: Ender Dragon): acerta cada parte
        if (entity.isMultipartEntity()) {
            for (PartEntity<?> part : entity.getParts()) {
                killEntity(part, source);
            }
        }

        // Mata a entidade principal
        killEntity(entity, source);

        // Retorna true para cancelar o ataque normal — já matamos o alvo
        return true;
    }

    /**
     * Força a morte de qualquer entidade, ignorando resistência a dano,
     * invulnerabilidade temporária e flags de invulnerável de mods.
     */
    private void killEntity(Entity entity, DamageSource source) {
        // Zera o tempo de invulnerabilidade para o dano sempre valer
        entity.invulnerableTime = 0;
        // Remove a flag de invulnerável (ex: mobs com NBT Invulnerable:1)
        entity.setInvulnerable(false);

        if (entity instanceof LivingEntity victim) {
            // 1) Tenta dano infinito normal (dispara loot/eventos quando o mob permite)
            victim.hurt(source, Float.MAX_VALUE);

            // 2) Se algum mod resistiu, força a vida a 0 e mata na marra
            if (victim.isAlive()) {
                victim.setHealth(0.0F);
                victim.die(source);
            }

            // 3) Garantia final: remove do mundo se ainda continuar vivo
            if (victim.isAlive()) {
                victim.kill();
            }
        } else {
            // Entidades não-vivas (partes de boss já tratam o dano no "pai")
            entity.hurt(source, Float.MAX_VALUE);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Dano: ∞").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Component.literal("§4Chara: Você está cheio de DETERMINAÇÃO."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
