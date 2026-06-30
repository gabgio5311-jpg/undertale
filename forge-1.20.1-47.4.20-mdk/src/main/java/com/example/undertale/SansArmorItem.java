package com.example.undertale;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Armadura do Sans — peça do set "Cosmos/Infinity-like".
 *
 * Esta classe é só um MARCADOR (igual ao InfinityArmorItem do Avaritia): a lógica
 * dos bônus de set (voo, invencibilidade, sem fome, etc.) fica em {@link SansArmorHandler},
 * que detecta as peças via {@code instanceof SansArmorItem}.
 *
 * Aqui só replicamos os traços de item da armadura do Avaritia:
 *  - Indestrutível (não perde durabilidade, sem barra de dano)
 *  - Resistente ao fogo (não queima dropada na lava)
 *  - Não irrita Endermen quando você olha pra eles (como uma máscara de abóbora)
 *  - Deixa os Piglins neutros
 */
public class SansArmorItem extends ArmorItem {

    public SansArmorItem(ArmorItem.Type type) {
        // Mantém o material SANS para preservar as texturas no corpo
        // (assets/undertale/textures/models/armor/sans_layer_1.png / _2.png).
        super(ModArmorMaterials.SANS, type,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    // Indestrutível: nunca recebe dano de durabilidade.
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    // Sem barra de durabilidade no inventário.
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    // Usar capacete não deixa o Enderman agressivo ao ser encarado.
    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return true;
    }

    // Mantém Piglins neutros, como a armadura de ouro / Infinity Armor.
    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return true;
    }
}
