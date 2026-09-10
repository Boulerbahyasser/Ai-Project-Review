package ma.uae.aireviewer.llm.context;

import java.nio.file.Path;

/** Fragment de code destine a une requete. Contenu non fiable par construction. */
public record CodeChunk(Path source, String content, int startLine, int endLine) {

    public int length() {
        return content.length();
    }
}
