package ma.uae.aireviewer.persistence.cache;

import java.nio.file.Path;
import java.util.Optional;

/** Cache sur disque (un fichier par cle). */
public final class FileSystemCacheStore implements CacheStore {

    private final Path directory;

    public FileSystemCacheStore(Path directory) {
        this.directory = directory;
    }

    @Override
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO");
    }
}
