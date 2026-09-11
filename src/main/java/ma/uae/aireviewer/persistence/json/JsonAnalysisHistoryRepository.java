package ma.uae.aireviewer.persistence.json;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import ma.uae.aireviewer.persistence.AnalysisHistoryRepository;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Historique persiste en fichiers JSON. */
public final class JsonAnalysisHistoryRepository implements AnalysisHistoryRepository {

    private final Path directory;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public JsonAnalysisHistoryRepository(Path directory) {
        this.directory = directory;
    }

    @Override
    public void save(AnalysisRecord record) {
        if (record == null || record.analysisId() == null || record.analysisId().isBlank()) {
            throw new IllegalArgumentException("Enregistrement invalide");
        }
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(record.analysisId() + ".json").normalize();
            if (!target.startsWith(directory.toAbsolutePath().normalize())) {
                throw new IllegalArgumentException("Identifiant invalide");
            }
            Files.writeString(target, mapper.writeValueAsString(record), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new IllegalStateException("Persistance de l'historique impossible", failure);
        }
    }

    @Override
    public List<AnalysisRecord> findAll() {
        if (!Files.isDirectory(directory)) return List.of();
        try (var files = Files.list(directory)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::read)
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.comparing(AnalysisRecord::date,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } catch (IOException failure) {
            throw new IllegalStateException("Lecture de l'historique impossible", failure);
        }
    }

    @Override
    public Optional<AnalysisRecord> findById(String analysisId) {
        if (analysisId == null || analysisId.isBlank()) return Optional.empty();
        Path file = directory.resolve(analysisId + ".json");
        return Files.isRegularFile(file) ? Optional.ofNullable(read(file)) : Optional.empty();
    }

    private AnalysisRecord read(Path file) {
        try {
            return mapper.readValue(Files.readString(file, StandardCharsets.UTF_8), AnalysisRecord.class);
        } catch (IOException invalid) {
            return null;
        }
    }
}
