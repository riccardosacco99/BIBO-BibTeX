# BIBO-BibTex

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
├── src
│   ├── main
│   │   ├── java
│   │   │   └── it
│   │   │       └── riccardosacco
│   │   │           └── bibobibtex
│   │   │               ├── App.java
│   │   │               ├── converter
│   │   │               │   ├── BibTeXToBiboConverter.java
│   │   │               │   ├── BiboToBibTeXConverter.java
│   │   │               │   └── BibliographicConverter.java
│   │   │               └── model
│   │   │                   ├── bibo
│   │   │                   │   └── BiboDocument.java
│   │   │                   └── bibtex
│   │   │                       └── BibTeXEntry.java
│   │   └── resources
│   └── test
│       └── java
│           └── it
│               └── riccardosacco
│                   └── bibobibtex
│                       └── converter
│                           └── BibliographicConverterTest.java
└── .gitignore
```

### Come compilare
```bash
mvn clean package
```
