# Come Testare il Progetto

## Scripts Disponibili

### 1. Conversione BibTeX → RDF (senza repository)
```bash
./bibtex-to-rdf.sh
```
Converte tutti i `.bib` in `test-data/bibtex/` → `.ttl` in `test-data/bibo/`

### 2. Test Repository (NUOVO!)
```bash
./test-repository.sh
```
Import BibTeX in repository RDF4J + query

### 3. Test Roundtrip
```bash
./test-roundtrip.sh
```
BibTeX → RDF → BibTeX → RDF (verifica integrità)

## Esempi Java

### Conversione rapida (no file)
```bash
mvn -q exec:java -pl core \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.RepositoryExample \
  -Dexec.args="test-data/bibtex/PapersDB.bib"
```

### Import in repository
```bash
mvn -q exec:java -pl vocbench-plugin \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryImportExample \
  -Dexec.args="test-data/bibtex/PapersDB.bib repository-data"
```

### Query repository
```bash
mvn -q exec:java -pl vocbench-plugin \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryQueryExample \
  -Dexec.args="repository-data"
```

## Plugin VocBench

Il plugin ora usa **RDF4J Native Store** per persistenza:

```java
// Setup
RDF4JRepositoryGateway gateway = new RDF4JRepositoryGateway("repo-dir");
VocBenchPluginLifecycle lifecycle = new VocBenchPluginLifecycle(
    new VocBenchPluginBootstrap(), 
    gateway
);

// Import
BibTeXEntry entry = ...;
Optional<BiboDocument> doc = lifecycle.importEntry(entry);
// → Convertito + salvato in repository

// Export (disponibile in Fase 7.B)
Optional<BibTeXEntry> exported = lifecycle.exportDocument("doc-id");

// Cleanup
gateway.shutdown();
```

## Note

- **Repository persiste** su disco (rm -rf per reset)
- **Fase 2**: store funziona, fetch/listAll placeholder (Fase 7.B)
- Vedi `TESTING.md` per dettagli completi
