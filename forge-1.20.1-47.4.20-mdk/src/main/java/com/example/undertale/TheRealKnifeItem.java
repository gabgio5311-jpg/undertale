package com.example.undertale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Real Knife: espada com dano infinito, igual à Sword of the Cosmos (Avaritia).
 * Dá hitkill (mata em um golpe) em QUALQUER ser vivo, incluindo mobs de outros mods,
 * mobs marcados como invulneráveis e bosses com múltiplas partes (ex: Ender Dragon).
 *
 * Em vez de confiar só no hurt() (que muitos mods sobrescrevem para capar/ignorar dano),
 * forçamos a vida a 0 e chamamos die() diretamente — assim a morte acontece de qualquer jeito.
 */
public class TheRealKnifeItem extends SwordItem {

    public TheRealKnifeItem(Properties properties) {
        // Tier Netherite, +900 de dano base e velocidade de espada (-2.4), igual ao valor da Avaritia.
        super(Tiers.NETHERITE, 900, -2.4F, properties);
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
    public boolean isFireResistant() {
        // Não queima no lava/fogo, como um item especial
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Dano: ∞").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Component.literal("§4Chara: Você está cheio de DETERMINAÇÃO."));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
