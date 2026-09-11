package ma.uae.aireviewer.persistence.cache;

import java.nio.file.Path;
import java.util.Optional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Cache sur disque (un fichier par cle). */
public final class FileSystemCacheStore implements CacheStore {

    private final Path directory;

    public FileSystemCacheStore(Path directory) {
        this.directory = directory;
    }

    @Override
    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        try {
            Path file = directory.resolve(safeName(key));
            return Files.isRegularFile(file)
                    ? Optional.of(Files.readString(file, StandardCharsets.UTF_8)) : Optional.empty();
        } catch (IOException failure) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Cle de cache vide");
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(safeName(key));
            Files.writeString(target, value == null ? "" : value, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new IllegalStateException("Ecriture du cache impossible", failure);
        }
    }

    private String safeName(String key) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8))) + ".cache";
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
