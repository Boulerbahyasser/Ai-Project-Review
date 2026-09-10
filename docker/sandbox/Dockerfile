# Conteneur d'execution du projet ANALYSE (code non fiable).
# Architecture recommandee par le cahier des charges : VM -> Docker -> Projet evalue.
FROM eclipse-temurin:21-jdk-alpine

# Principe de moindre privilege : utilisateur non root
RUN addgroup -g 1000 sandbox && adduser -D -u 1000 -G sandbox sandbox

# Le projet est monte en lecture seule sur /workspace par DockerSandboxRunner
WORKDIR /workspace
USER 1000:1000

# Aucun ENTRYPOINT privilegie : la commande est fournie par SandboxSpec
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["echo 'no command provided'"]

# Contraintes appliquees au run (cf. SandboxLimits) :
#   --network none --read-only --cap-drop ALL --security-opt no-new-privileges
#   --cpus --memory --pids-limit --rm  + timeout applique cote Java
