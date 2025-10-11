package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;

/**
 * Placeholder abstraction that will be bound to VocBench services responsible for accessing the
 * project repository. Implementations will be provided once the plugin is wired into the actual
 * platform runtime.
 */
public interface VocBenchRepositoryGateway {
    void store(Model model);

    Optional<BiboDocument> fetchByIdentifier(String identifier);

    List<BiboDocument> listAll();
}
