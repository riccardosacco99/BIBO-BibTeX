package it.riccardosacco.bibobibtex.vocbench.examples;

import it.riccardosacco.bibobibtex.vocbench.RDF4JRepositoryGateway;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Example showing RDF4J repository queries.
 *
 * Demonstrates:
 * - Connecting to existing repository
 * - Running SPARQL queries
 * - Counting documents
 * - Listing all subjects
 *
 * Usage:
 *   mvn -q exec:java -pl vocbench-plugin \
 *     -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryQueryExample \
 *     -Dexec.args="repository-data"
 */
public class RepositoryQueryExample {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: RepositoryQueryExample <repository-dir>");
            System.out.println("\nExample:");
            System.out.println("  mvn -q exec:java -pl vocbench-plugin \\");
            System.out.println("    -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryQueryExample \\");
            System.out.println("    -Dexec.args=\"repository-data\"");
            System.exit(1);
        }

        String repoDir = args[0];

        System.out.println("=== RDF4J Repository Query Example ===\n");
        System.out.println("Repository: " + repoDir);
        System.out.println();

        // Connect to repository
        RDF4JRepositoryGateway gateway = new RDF4JRepositoryGateway(repoDir);

        try {
            // Count all documents
            System.out.println("=== Document Count ===");
            try (RepositoryConnection conn = gateway.getRepository().getConnection()) {
                String countQuery =
                    "SELECT (COUNT(DISTINCT ?doc) AS ?count) " +
                    "WHERE { ?doc ?p ?o }";

                long count = conn.prepareTupleQuery(QueryLanguage.SPARQL, countQuery)
                    .evaluate()
                    .stream()
                    .findFirst()
                    .map(bindings -> bindings.getValue("count"))
                    .map(value -> Long.parseLong(value.stringValue()))
                    .orElse(0L);

                System.out.println("Total documents: " + count);
            }

            // List all subjects
            System.out.println("\n=== Document Subjects ===");
            try (RepositoryConnection conn = gateway.getRepository().getConnection()) {
                String listQuery =
                    "CONSTRUCT { ?doc ?p ?o } " +
                    "WHERE { ?doc ?p ?o } " +
                    "LIMIT 10";

                GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, listQuery);

                try (GraphQueryResult result = query.evaluate()) {
                    Model model = new org.eclipse.rdf4j.model.impl.LinkedHashModel();
                    result.forEach(model::add);

                    System.out.println("First 10 subjects:");
                    model.subjects().stream()
                        .limit(10)
                        .forEach(subject -> System.out.println("  - " + subject));

                    System.out.println("\nTotal statements in sample: " + model.size());
                }
            }

            // Count by type
            System.out.println("\n=== Documents by Type ===");
            try (RepositoryConnection conn = gateway.getRepository().getConnection()) {
                String typeQuery =
                    "SELECT ?type (COUNT(?doc) AS ?count) " +
                    "WHERE { " +
                    "  ?doc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type " +
                    "} " +
                    "GROUP BY ?type " +
                    "ORDER BY DESC(?count)";

                conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQuery)
                    .evaluate()
                    .forEach(bindings -> {
                        String type = bindings.getValue("type").stringValue();
                        String count = bindings.getValue("count").stringValue();
                        System.out.println("  " + type + ": " + count);
                    });
            }

            System.out.println("\nNote: Full BiboDocument queries available in Phase 7.B");

        } finally {
            gateway.shutdown();
            System.out.println("\nRepository closed successfully");
        }
    }
}
