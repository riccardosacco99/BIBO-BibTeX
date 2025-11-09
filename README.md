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
│   ├── bibtex/              # File BibTeX originali
│   ├── bibo/                # File RDF convertiti
│   ├── bibtex-roundtrip/    # BibTeX dopo roundtrip
│   └── bibo-roundtrip/      # RDF dopo roundtrip
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

## Flusso di Conversione e Testing

### Struttura Directory Test Data
```
test-data/
├── bibtex/              # File BibTeX originali (input)
├── bibo/                # File RDF convertiti da BibTeX
├── bibtex-roundtrip/    # File BibTeX ottenuti da conversione RDF→BibTeX (roundtrip)
└── bibo-roundtrip/      # File RDF ottenuti da BibTeX roundtrip (per validazione)
```

### Flusso Completo di Conversione
```
BibTeX  →   RDF  →     BibTeX        →     RDF
   ↓         ↓           ↓                  ↓
bibtex      bibo   bibtex-roundtrip     bibo-roundtrip 
```

Questo flusso permette di testare la qualità della conversione bidirezionale e identificare eventuali perdite di informazione.

### Script Disponibili

#### 1. Conversione BibTeX → RDF
Converte tutti i file BibTeX in `test-data/bibtex/` in formato RDF (Turtle):
```bash
./bibtex-to-rdf.sh
```
Output: file `.ttl` in `test-data/bibo/`

#### 2. Test Roundtrip Completo
Esegue il ciclo completo di conversione per validare la qualità del convertitore:
```bash
./test-roundtrip.sh
```
Questo script:
1. Converte RDF → BibTeX (`test-data/bibo/` → `test-data/bibtex-roundtrip/`)
2. Converte BibTeX → RDF (`test-data/bibtex-roundtrip/` → `test-data/bibo-roundtrip/`)
3. Mostra statistiche sui file processati

Per confrontare i risultati originali vs roundtrip:
```bash
# Confronta file RDF specifico
diff test-data/bibo/holmes2004artificial.ttl test-data/bibo-roundtrip/holmes2004artificial.ttl

# Confronta tutti i file (attenzione: output lungo!)
for file in test-data/bibo/*.ttl; do
    base=$(basename "$file")
    if [ -f "test-data/bibo-roundtrip/$base" ]; then
        echo "=== $base ==="
        diff "$file" "test-data/bibo-roundtrip/$base" | head -20
    fi
done
```

#### 3. Conversione tra Formati RDF
Converte tra Turtle (`.ttl`) e RDF/XML (`.rdf`):
```bash
./convert-between-rdf-formats.sh
```

### Dataset del Professore
Gli archivi BibTeX forniti dal professore sono versionati in `test-data/bibtex/`:
- `PapersDB.bib` (~325KB, database completo)
- `PapersDB_MIUR.bib` (~18KB, subset MIUR)

Questi file vengono processati automaticamente dagli script di conversione. Per eseguire manualmente la conversione di un singolo file:
```bash
# Conversione batch (consigliato - più veloce)
mvn -q exec:java -pl core \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.BatchConversion \
    -Dexec.args="test-data/bibtex test-data/bibo"

# Conversione singolo file
mvn -q exec:java -pl core \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
    -Dexec.args="test-data/bibtex/PapersDB.bib test-data/bibo"
```

**Nota:** La classe `BatchConversion` processa tutti i file `.bib` in una directory con un'unica esecuzione Maven, creando un file `.ttl` per ogni file `.bib` (mantenendo la struttura originale). Questo approccio è molto più veloce rispetto a chiamate Maven separate per ogni file.

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
