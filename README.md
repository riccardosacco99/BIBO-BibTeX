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
├── core/
│   ├── pom.xml
│   └── src
│       ├── main/java/it/riccardosacco/bibobibtex
│       │   ├── App.java
│       │   ├── converter/
│       │   │   ├── BibTeXBibliographicConverter.java
│       │   │   └── BibliographicConverter.java
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
└── README.md
```

- `core`: modulo principale con i convertitori e i modelli BIBO rappresentati in RDF4J.
- `vocbench-plugin`: scheletro del plugin VocBench, pronto per l'integrazione con i servizi di repository della piattaforma.

### Come compilare
```bash
mvn clean package
```

### Dataset del professore
- Gli archivi BibTeX forniti dal professore sono versionati in `test-data/professor-examples/PapersDB_MIUR.bib` e `test-data/professor-examples/PapersDB.bib`.
- Per verifiche manuali o benchmark di regressione è sufficiente eseguire:
```bash
mvn -pl core exec:java \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
    -Dexec.args="test-data/professor-examples/PapersDB.bib core/target/papersdb-full"
```
Per il dataset specifico MIUR basta cambiare l'input e (facoltativamente) la cartella di output:
```bash
mvn -pl core exec:java \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
    -Dexec.args="test-data/professor-examples/PapersDB_MIUR.bib core/target/papersdb-miur"
```
I Turtle generati finiscono sotto `core/target/…` (ignorato da git) e possono essere confrontati con run futuri.

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
