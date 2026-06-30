package com.example.undertale;

import net.minecraft.client.Minecraft;

/**
 * Ponte client-only do Indicador de LV. Fica numa classe separada (e só é tocada
 * via DistExecutor a partir de {@link IndicadorLvItem#use}) para que nada de cliente
 * seja carregado num servidor dedicado.
 */
public class IndicadorLvClient {

    public static void open() {
        Minecraft.getInstance().setScreen(new IndicadorLvScreen());
    }
}
