package ma.uae.aireviewer.project.classification;

import java.nio.file.Path;
import java.util.List;
import ma.uae.aireviewer.project.model.FileType;

/** Classifieur par liste de regles ordonnee (premiere regle satisfaite). */
public final class RuleBasedFileClassifier implements FileClassifier {

    private final List<ClassificationRule> rules;

    public RuleBasedFileClassifier(List<ClassificationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public FileType classify(Path path) {
        return rules.stream()
                .filter(rule -> rule.matches(path))
                .map(ClassificationRule::type)
                .findFirst()
                .orElse(FileType.OTHER);
    }
}
