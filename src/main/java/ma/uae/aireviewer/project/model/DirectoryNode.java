package ma.uae.aireviewer.project.model;

import java.nio.file.Path;
import java.util.List;

/** Noeud composite : un repertoire et ses enfants. */
public record DirectoryNode(String name, Path path, List<ProjectNode> children) implements ProjectNode {

    public DirectoryNode {
        children = List.copyOf(children);
    }

    @Override
    public long sizeInBytes() {
        return children.stream().mapToLong(ProjectNode::sizeInBytes).sum();
    }

    @Override
    public <R> R accept(ProjectVisitor<R> visitor) {
        return visitor.visitDirectory(this);
    }
}
