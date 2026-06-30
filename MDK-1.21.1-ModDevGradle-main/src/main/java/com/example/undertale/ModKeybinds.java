package com.example.undertale;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Teclas do mod (lado cliente). A tecla é registrada em {@link ClientSetup}
 * e lida em {@link BoneArmorSansClient}.
 */
public class ModKeybinds {

    // Categoria padrão "Diversos/Miscellaneous" — já existe e é traduzida pelo jogo.
    public static final String CATEGORY = "key.categories.misc";

    // Nome DIRETO (não é chave de tradução). Tecla padrão: G.
    public static final KeyMapping THROW_BONE = new KeyMapping(
            "Jogar Osso do Sans",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY);
}
