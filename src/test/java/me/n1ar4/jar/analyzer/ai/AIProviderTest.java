package me.n1ar4.jar.analyzer.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIProviderTest {
    @Test
    void deepSeekUsesFlashAsItsDefaultAndFirstOption() {
        assertEquals("deepseek-v4-flash", AIProvider.DEEPSEEK.getDefaultModel());
        assertEquals("deepseek-v4-flash", AIProvider.DEEPSEEK.getModelOptions().get(0));
        assertTrue(AIProvider.DEEPSEEK.getModelOptions().contains("deepseek-v4-pro"));
    }

    @Test
    void glmUsesGlm52AsItsDefaultAndOffersItForSelection() {
        assertEquals("glm-5.2", AIProvider.GLM.getDefaultModel());
        assertEquals("glm-5.2", AIProvider.GLM.getModelOptions().get(0));
        assertTrue(AIProvider.GLM.getModelOptions().contains("glm-5.2"));
    }
}
