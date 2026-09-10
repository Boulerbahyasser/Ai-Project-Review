package ma.uae.aireviewer.llm.prompt;

import java.util.ArrayList;
import java.util.List;
import ma.uae.aireviewer.security.prompt.PromptInjectionGuard;

/**
 * Pattern Builder : assemble progressivement un prompt structure
 * (role du modele, critere evalue, elements fournis, format attendu -- section 4.3).
 * Tout contenu marque untrusted traverse obligatoirement le PromptInjectionGuard.
 */
public final class PromptBuilder {

    private final PromptInjectionGuard guard;
    private final List<PromptSection> sections = new ArrayList<>();
    private String role = "";
    private String expectedFormat = "";

    public PromptBuilder(PromptInjectionGuard guard) {
        this.guard = guard;
    }

    public PromptBuilder role(String role) {
        this.role = role;
        return this;
    }

    public PromptBuilder section(PromptSection section) {
        sections.add(section);
        return this;
    }

    public PromptBuilder expectedFormat(String expectedFormat) {
        this.expectedFormat = expectedFormat;
        return this;
    }

    public String build() {
        throw new UnsupportedOperationException(
                "TODO : concatener role + sections (guard.neutralize sur untrusted) + format attendu");
    }
}
