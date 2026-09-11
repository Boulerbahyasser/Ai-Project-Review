# Conteneur d'extraction d'archives ZIP non fiables (upload utilisateur, section 8).
# Meme principe que le sandbox d'execution : jamais d'extraction directe sur l'hote.
FROM alpine:3.20

# Principe de moindre privilege : utilisateur non root
RUN apk add --no-cache unzip \
    && addgroup -g 1000 unzip \
    && adduser -D -u 1000 -G unzip unzip

# /work recoit le tmpfs d'extraction (taille bornee au lancement, cf. UnzipConfig).
# L'archive est montee en lecture seule sur /input, la sortie en ecriture sur /output.
WORKDIR /work
USER 1000:1000

# Aucun ENTRYPOINT privilegie : le script d'extraction est fourni par DockerArchiveExtractor
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["echo 'no command provided'"]

# Contraintes appliquees au run (cf. UnzipConfig) :
#   --network none --read-only --cap-drop ALL --security-opt no-new-privileges
#   --memory --pids-limit --rm --tmpfs /work:size=<maxExtractedSizeMb>m
#   -v <archive>:/input/archive.zip:ro   -v <destination>:/output:rw
#   + delai maximal applique cote Java (cf. DockerArchiveExtractor)
