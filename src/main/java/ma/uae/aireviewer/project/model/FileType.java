package ma.uae.aireviewer.project.model;

/** Categories de fichiers reconnues lors de l'import (cahier des charges, section 3.1). */
public enum FileType {
    JAVA,
    TEST,
    CONFIGURATION,
    BUILD_MAVEN,
    BUILD_GRADLE,
    DOCKER,
    DOCUMENTATION,
    SCRIPT,
    RESOURCE,
    OTHER
}
