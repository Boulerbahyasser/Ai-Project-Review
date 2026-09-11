package ma.uae.aireviewer.persistence.cache;

import java.util.Optional;

/** Cache cle / valeur utilise par llm et par les analyseurs. */
public interface CacheStore {

    Optional<String> get(String key);

    void put(String key, String value);
}
