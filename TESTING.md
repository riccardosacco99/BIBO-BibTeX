# Testing Guide

## Overview

Il progetto fornisce diversi modi per testare la conversione BibTeX↔BIBO e il repository RDF4J.

## 1. Script di Conversione (Senza Repository)

### Converti BibTeX → RDF
```bash
./bibtex-to-rdf.sh
```
Converte tutti i file in `test-data/bibtex/` → `test-data/bibo/` (formato Turtle)

### Test Roundtrip (RDF → BibTeX → RDF)
```bash
./test-roundtrip.sh
```
Verifica integrità conversione bidirezionale

### Conversione tra formati RDF
```bash
./convert-between-rdf-formats.sh
```
Converte Turtle ↔ RDF/XML

## 2. Repository Testing (Con RDF4J Native Store)

### Quick Test
```bash
./test-repository.sh
```
Importa `PapersDB.bib` in repository e fa query

### Import personalizzato
```bash
./test-repository.sh test-data/bibtex/esempio.bib my-repo
```

### Esempi Java

#### Import BibTeX in Repository
```bash
mvn -q exec:java -pl vocbench-plugin \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryImportExample \
  -Dexec.args="test-data/bibtex/PapersDB.bib repository-data"
```

#### Query Repository
```bash
mvn -q exec:java -pl vocbench-plugin \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryQueryExample \
  -Dexec.args="repository-data"
```

## 3. Conversione Singola (Senza Repository)

### Converti singolo file
```bash
mvn -q exec:java -pl core \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
  -Dexec.args="test-data/bibtex/PapersDB.bib test-data/bibo"
```

### Verifica conversione (no file output)
```bash
mvn -q exec:java -pl core \
  -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.RepositoryExample \
  -Dexec.args="test-data/bibtex/PapersDB.bib"
```

## 4. Unit Tests

```bash
# Tutti i test
mvn test

# Solo test core
mvn test -pl core

# Solo test repository
mvn test -pl vocbench-plugin -Dtest=RDF4JRepositoryGatewayTest
```

## 5. Plugin VocBench

Il plugin è scheletrato in `vocbench-plugin/`:
- `VocBenchPluginLifecycle`: orchestrazione import/export
- `RDF4JRepositoryGateway`: storage RDF con Native Store
- `VocBenchPluginBootstrap`: wrapper converter

### Uso programmatico
```java
// Create repository
RDF4JRepositoryGateway gateway = new RDF4JRepositoryGateway("repo-dir");

// Convert & store
BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
Optional<BiboDocument> doc = converter.convertToBibo(bibTexEntry);
doc.ifPresent(d -> gateway.store(d.rdfModel()));

// Cleanup
gateway.shutdown();
```

## Note Importanti

### Repository Persistence
- Il repository RDF4J **persiste** i dati su disco
- Successive esecuzioni **aggiungono** documenti (non sovrascrivono)
- Per reset: `rm -rf repository-data`

### Limitazioni Fase 2
- ✅ `store(Model)`: funziona completamente
- ⚠️ `fetchByIdentifier()`: ritorna `Optional.empty()` (placeholder fino Fase 7.B)
- ⚠️ `listAll()`: ritorna `List.of()` (placeholder fino Fase 7.B)
- ℹ️ Query SPARQL dirette funzionano (vedi `RepositoryQueryExample`)

### Conversione RDF → BiboDocument
La conversione completa RDF → BiboDocument sarà implementata in **Fase 7.B**.
Per ora usa:
- Query SPARQL dirette (vedi esempi)
- `ReverseConversion` (usa logica custom, non repository)

## Workflow Tipico

1. **Sviluppo converter**: usa `RepositoryExample` (veloce, no persistenza)
2. **Test repository**: usa `test-repository.sh` (importa + query)
3. **Test roundtrip**: usa `test-roundtrip.sh` (verifica integrità)
4. **CI/CD**: usa `mvn test` (tutti i test automatici)
