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

### Output Format (Turtle RDF)
Il convertitore genera file **Turtle** (`.ttl`) per default, con pretty-print abilitato per la leggibilità:

```turtle
@prefix bibo: <http://purl.org/ontology/bibo/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

[] a bibo:Article, bibo:Document ;
  dcterms:title "The Mediterranean diet and health: a comprehensive overview" ;
  dcterms:identifier "guasch2021mediterranean" ;
  bibo:authorList (
    [ a foaf:Person ;
      foaf:name "Guasch-Ferré, Marta" ;
      foaf:givenName "Marta" ;
      foaf:familyName "Guasch-Ferré"
    ]
    [ a foaf:Person ;
      foaf:name "Willett, Walter C" ;
      foaf:givenName "Walter C" ;
      foaf:familyName "Willett"
    ]
  ) ;
  dcterms:issued "2021"^^xsd:gYear ;
  dcterms:publisher "Wiley Online Library" ;
  bibo:volume "290" ;
  bibo:issue "3" ;
  bibo:pages "549--566" .
```

**Caratteristiche:**
- Formato **Turtle** (leggibile, compatto)
- **RDF Lists** per l'ordinamento degli autori (non `bibo:sequence`)
- **Unicode** per caratteri accentati (non escape LaTeX)
- Blank nodes per le persone (non necessitano URI globale)
- Supporta anche RDF/XML, JSON-LD via parametro `RDFFormat`

### Dataset del professore
- Gli archivi BibTeX forniti dal professore sono versionati in `test-data/professor-examples/PapersDB_MIUR.bib` e `test-data/professor-examples/PapersDB.bib`.
- Per verifiche manuali o benchmark di regressione è sufficiente eseguire:
  ```bash
  mvn -pl core exec:java \
      -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.SampleConversion \
      -Dexec.args="test-data/professor-examples/PapersDB.bib core/target/papersdb-full"
  ```
  I Turtle generati finiscono sotto `core/target/…` (ignorato da git) e possono essere confrontati con run futuri.