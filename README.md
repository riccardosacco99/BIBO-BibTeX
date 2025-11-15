# BIBO-BibTeX

## Introduzione
Questo progetto nasce come parte della mia tesi triennale e ha come obiettivo lo sviluppo di un convertitore tra modelli bibliografici.  
I modelli considerati sono:
- Microsoft Word's Bibliography Model  
- BibTeX (formato di LaTeX)  
- BIBO (Bibliographic Ontology)

BIBO verrà usato come **pivot model** per consentire la conversione verso e da altri formati.  
In particolare, il lavoro si concentrerà sulla conversione **BIBO ↔ BibTeX**, tenendo conto delle differenze informative che rendono la conversione non sempre lossless.  

Il progetto è realizzato in **Java**, con due strumenti principali:  
- [JBibTeX](https://github.com/jbibtex/jbibtex) per la gestione del formato BibTeX  
- [RDF4J](https://rdf4j.org/) (versione 5.x) per la gestione del modello BIBO in RDF  

L’obiettivo finale è integrare il convertitore come **plugin per la piattaforma VocBench**, affrontando le sfide di modellazione derivanti dalle differenze tra i vari formati bibliografici.
## Struttura del progetto
```
BIBO-BibTeX/
├── pom.xml
├── bibtex-to-rdf.sh              # Script per conversione BibTeX → RDF
├── test-roundtrip.sh             # Script per test roundtrip completo
├── convert-between-rdf-formats.sh # Script conversione Turtle ↔ RDF/XML
├── core/
│   ├── pom.xml
│   └── src
│       ├── main/java/it/riccardosacco/bibobibtex
│       │   ├── App.java
│       │   ├── converter/
│       │   │   ├── BibTeXBibliographicConverter.java
│       │   │   └── BibliographicConverter.java
│       │   ├── examples/
│       │   │   ├── BatchConversion.java       # Conversione batch BibTeX→RDF (ottimizzato)
│       │   │   ├── RDFConverter.java         # Conversione tra formati RDF
│       │   │   ├── ReverseConversion.java    # Conversione RDF→BibTeX
│       │   │   └── SampleConversion.java     # Conversione BibTeX→RDF
│       │   └── model/bibo/
│       │       ├── BiboContributor*.java
│       │       ├── BiboDocument.java
│       │       ├── BiboDocumentType.java
│       │       ├── BiboIdentifier*.java
│       │       ├── BiboPersonName.java
│       │       ├── BiboPublicationDate.java
│       │       └── BiboVocabulary.java
│       └── test/java/it/riccardosacco/bibobibtex
│           ├── converter/
│           │   ├── BibTeXBibliographicConverterDetailedTest.java
│           │   └── BibliographicConverterTest.java
│           └── model/bibo/BiboDocumentTest.java
├── vocbench-plugin/
│   ├── pom.xml
│   └── src
│       ├── main/java/it/riccardosacco/bibobibtex/vocbench/
│       │   ├── VocBenchPluginBootstrap.java
│       │   ├── VocBenchPluginLifecycle.java
│       │   └── VocBenchRepositoryGateway.java
│       └── test/java/it/riccardosacco/bibobibtex/vocbench/
│           └── VocBenchPluginLifecycleTest.java
├── test-data/
│   └── bibtex/              # File BibTeX originali (input - versionati in git)
│       # Le altre directory (bibo/, bibtex-roundtrip/, bibo-roundtrip/)
│       # vengono generate automaticamente dagli script di conversione
└── README.md
```

### Moduli Principali

- **`core`**: modulo principale con i convertitori e i modelli BIBO rappresentati in RDF4J
  - `converter/`: logica di conversione bidirezionale BibTeX ↔ BIBO
  - `model/bibo/`: modello Java per rappresentare documenti bibliografici BIBO
  - `examples/`: utility per conversione batch e testing
- **`vocbench-plugin`**: scheletro del plugin VocBench, pronto per l'integrazione con i servizi di repository della piattaforma
- **`test-data/`**: directory per file di test e validazione roundtrip

### Come compilare
```bash
mvn clean package
```

## Flusso di Conversione

### Panoramica Directory

Il progetto mantiene solo `test-data/bibtex/` versionato in git (punto di partenza). Le altre directory vengono **generate automaticamente** dagli script:

```
test-data/
├── bibtex/              # [VERSIONATO] File BibTeX originali
├── bibo/                # [GENERATO] RDF convertiti da BibTeX
├── bibtex-roundtrip/    # [GENERATO] BibTeX da conversione inversa RDF→BibTeX
└── bibo-roundtrip/      # [GENERATO] RDF da BibTeX roundtrip (validazione)
```

**Importante:** Gli script processano **automaticamente tutti i file** presenti in `test-data/bibtex/`, indipendentemente dal numero o dimensione. Non serve modificare gli script quando si aggiungono nuovi file.

### Flusso di Conversione Completo

```
┌─────────┐     Script 1      ┌──────┐     Script 2      ┌──────────────────┐
│ BibTeX  │ ──────────────→   │ RDF  │ ─────────────→    │ BibTeX roundtrip │
│ (input) │   bibtex-to-rdf   │(BIBO)│  test-roundtrip   │                  │
└─────────┘                   └──────┘                   └──────────────────┘
    ↑                             ↓                                ↓
  bibtex/                       bibo/                       bibtex-roundtrip/
                                  │                                │
                                  │        Script 2                ↓
                                  └─────  (continua)  ──→    bibo-roundtrip/
                                        test-roundtrip        (validazione)
```

### Script di Conversione

#### 1. BibTeX → RDF (Conversione Base)

**Script:** `./bibtex-to-rdf.sh`

Converte tutti i file `.bib` in `test-data/bibtex/` in formato RDF Turtle.

```bash
./bibtex-to-rdf.sh
```

**Output:**
- Crea `test-data/bibo/` (se non esiste)
- Genera un file `.ttl` per ogni file `.bib` (stesso nome)
- Esempio: `PapersDB.bib` → `PapersDB.ttl`

**Quando usarlo:**
- Prima conversione dopo clonazione repo
- Dopo aggiunta/modifica di file BibTeX
- Prima di eseguire il test roundtrip

#### 2. Test Roundtrip Completo (Validazione Bidirezionale)

**Script:** `./test-roundtrip.sh`

Esegue il ciclo completo per validare la conversione bidirezionale:

```bash
./test-roundtrip.sh
```

**Flusso interno:**
1. **Step 1:** RDF → BibTeX
   `test-data/bibo/*.ttl` → `test-data/bibtex-roundtrip/*.bib`

2. **Step 2:** BibTeX → RDF (roundtrip)
   `test-data/bibtex-roundtrip/*.bib` → `test-data/bibo-roundtrip/*.ttl`

3. **Output:** Statistiche su file processati ed entries convertite

**Validazione manuale:**

Confronta RDF originale vs roundtrip per verificare perdite di informazione:

```bash
# File specifico
diff test-data/bibo/PapersDB.ttl test-data/bibo-roundtrip/PapersDB.ttl

# Tutti i file (panoramica)
for file in test-data/bibo/*.ttl; do
    base=$(basename "$file")
    echo "=== $base ==="
    diff "$file" "test-data/bibo-roundtrip/$base" | head -10
done
```

#### 3. Conversione tra Formati RDF (Utility)

Converte tra Turtle (`.ttl`) e RDF/XML (`.rdf`):

```bash
./convert-between-rdf-formats.sh
```

### Dataset Test

Il repository include file BibTeX di test in `test-data/bibtex/`:
- `PapersDB.bib` (~325KB, 528 entries)
- `PapersDB_MIUR.bib` (~18KB, 16 entries)
- File singoli per test unitari (artificialIntelligenceMedicine, cyclingPerformance, etc.)

### Conversione Manuale (Avanzata)

Per eseguire le conversioni senza gli script:

```bash
# BibTeX → RDF (batch - consigliato)
mvn -q exec:java -pl core \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.BatchConversion \
    -Dexec.args="test-data/bibtex test-data/bibo"

# BibTeX → RDF (singolo file)
mvn -q exec:java -pl core \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
    -Dexec.args="test-data/bibtex/PapersDB.bib test-data/bibo"

# RDF → BibTeX (batch)
mvn -q exec:java -pl core \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.ReverseConversion \
    -Dexec.args="test-data/bibo test-data/bibtex-roundtrip"
```

**Nota:** `BatchConversion` processa tutti i file in una directory con un'unica esecuzione Maven, generando un file `.ttl` per ogni `.bib`. Molto più efficiente per dataset multipli.

## Testing e Coverage

### Eseguire i Test

```bash
# Esegui tutti i test
mvn test

# Esegui solo i test del modulo core
mvn test -pl core
```

### Report di Coverage (JaCoCo)

Il progetto usa JaCoCo per il test coverage. Per generare il report:

```bash
# Genera report di coverage
mvn clean test jacoco:report -pl core
```

Il report HTML viene generato in:
```
core/target/site/jacoco/index.html
```

Aprilo nel browser:
```bash
open core/target/site/jacoco/index.html
```

**Coverage attuale:**
- **Converter package**: 92% instruction coverage
- **Model package**: 89% instruction coverage
- **Overall core logic**: >70% coverage target raggiunto

**Note:**
- La cartella `target/` è nel `.gitignore` (contiene file generati)
- Dopo aver clonato il progetto, esegui `mvn test jacoco:report` per rigenerare il report
- Il package `examples/` è escluso dal coverage (utility, non core logic)

### Test Inclusi

Il progetto include 223 test:
- **Unit tests**: `BibliographicConverterTest`, `BiboDocumentTest`
- **Detailed tests**: `BibTeXBibliographicConverterDetailedTest` (mapping campi)
- **Validation tests**: `ValidationExceptionTest` (27 test)
- **Unicode tests**: `BibTeXUnicodeConverterTest` (133 test, 100+ escape sequences)
- **Edge case tests**: `BibTeXBibliographicConverterEdgeCaseTest` (42 test)
  - Name parsing (single tokens, particles, unicode, special chars)
  - Date parsing (invalid months, leap years, edge dates)
  - Large entries (10k+ chars, 100+ authors)
  - Minimal/maximal configurations

## Esempio Output RDF

Il progetto genera RDF in formato Turtle con la struttura BIBO. Ecco un esempio di articolo convertito che mostra come vengono modellati gli autori usando RDF Lists:

```turtle
@prefix bibo: <http://purl.org/ontology/bibo/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

_:article1 a bibo:Document, bibo:Article;
  dcterms:title "Improving cycling performance: how should we spend our time and money";
  dcterms:identifier "jeukendrup2001improving";
  bibo:authorList _:authorList1;
  dcterms:issued "2001"^^xsd:gYear;
  bibo:volume "31";
  bibo:pages "559--569" .

# Gli autori sono memorizzati in RDF List per preservare l'ordine
_:authorList1 a rdf:List;
  rdf:first _:author1;
  rdf:rest _:authorList2 .

_:authorList2 rdf:first _:author2;
  rdf:rest rdf:nil .

_:author1 a foaf:Person;
  foaf:name "Jeukendrup, Asker E";
  foaf:givenName "Asker E";
  foaf:familyName "Jeukendrup" .

_:author2 a foaf:Person;
  foaf:name "Martin, James";
  foaf:givenName "James";
  foaf:familyName "Martin" .
```

**Caratteristiche chiave:**
- ✅ Utilizza `bibo:authorList` (standard BIBO)
- ✅ RDF Lists (`rdf:first`, `rdf:rest`, `rdf:nil`) preservano l'ordine degli autori
- ✅ Caratteri Unicode (accenti, simboli) correttamente convertiti da escape sequences BibTeX
- ✅ Formato Turtle con pretty-print e blank nodes inlinati

## Limitazioni e Gap Informativo

La conversione tra BIBO e BibTeX è **intrinsecamente lossy** in alcune direzioni a causa delle differenze di espressività tra i due modelli:

- **BibTeX → BIBO**: Conversione generalmente lossless. BIBO è più espressivo e può rappresentare tutti i campi BibTeX.
- **BIBO → BibTeX**: Conversione con perdita di informazione. BibTeX non supporta metadati ricchi come affiliazioni autori, organizzatori di conferenze strutturati, identificatori multipli dello stesso tipo, date complete (giorno/mese/anno).

### Limitazioni Note

- **Citation keys**: Rigenerati durante export BIBO→BibTeX se non presenti come proprietà esplicita
- **Cross-references** (`crossref`): Ignorati, le entry devono essere completamente espanse
- **Metadati organizzativi**: Campi come `organization` su `@inproceedings` possono essere persi
- **Campi informali**: `howpublished` su `@misc` omessi (BIBO non li modella)

### Documentazione Dettagliata

Per un'analisi completa del gap informativo tra BIBO e BibTeX, inclusi:
- 5 scenari dettagliati di perdita informativa con esempi RDF Turtle e BibTeX
- Strategie euristiche implementate per minimizzare la perdita
- Tabella comparativa di espressività BIBO vs BibTeX
- Best practices per utenti e sviluppatori
- Materiale di tesi (italiano) sull'analisi del gap

**Consulta:** [docs/LIMITATIONS.md](docs/LIMITATIONS.md)
