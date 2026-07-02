package com.example.undertale;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Triggers de advancement customizados do mod.
 *
 * No NeoForge 1.21.1 os triggers são registrados no registro
 * {@link Registries#TRIGGER_TYPE} via {@link DeferredRegister} (em vez do antigo
 * {@code CriteriaTriggers.register}). O {@link DeferredRegister} é ligado ao bus do mod
 * em {@link UndertaleMod}.
 */
public final class ModCriteria {

    private ModCriteria() {}

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, UndertaleMod.MOD_ID);

    /** Dispara ao alcançar um LV mínimo (usado pelas conquistas de LV). */
    public static final DeferredHolder<CriterionTrigger<?>, ReachLvTrigger> REACH_LV =
            TRIGGERS.register("reach_lv", ReachLvTrigger::new);
}
