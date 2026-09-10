package ma.uae.aireviewer.llm.prompt;

/**
 * Fragment de prompt. Le drapeau untrusted marque le contenu provenant du projet
 * analyse : il sera encadre et neutralise avant envoi (section 8.2).
 */
public record PromptSection(String title, String body, boolean untrusted) {

    public static PromptSection trusted(String title, String body) {
        return new PromptSection(title, body, false);
    }

    public static PromptSection untrusted(String title, String body) {
        return new PromptSection(title, body, true);
    }
}
