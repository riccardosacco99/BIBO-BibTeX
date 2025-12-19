# Confronto tra BIBO e FaBIO

Questo documento presenta un confronto tra le due principali ontologie bibliografiche disponibili per la rappresentazione di dati bibliografici in RDF: BIBO (Bibliographic Ontology) e FaBIO (FRBR-aligned Bibliographic Ontology). L'analisi giustifica la scelta di BIBO come modello pivot per il progetto BIBO-BibTeX.

## 1. Introduzione

La conversione bidirezionale tra BibTeX e RDF richiede l'adozione di un'ontologia bibliografica che funga da modello intermedio. Le due ontologie più rilevanti in questo dominio sono BIBO, sviluppata nel 2008, e FaBIO, parte dell'ecosistema SPAR sviluppato dal 2010.

L'obiettivo di questo confronto è analizzare le caratteristiche di entrambe le ontologie per giustificare la scelta di BIBO come formato pivot per questo progetto, considerando i requisiti specifici dell'integrazione con VocBench.

## 2. Panoramica delle Ontologie

### 2.1 BIBO (Bibliographic Ontology)

| Caratteristica | Valore |
|---------------|--------|
| **Sviluppatori** | Bruce D'Arcus e Frédérick Giasson |
| **Anno di rilascio** | 2008 |
| **Versione attuale** | 1.3 |
| **Namespace** | `http://purl.org/ontology/bibo/` |
| **Standard** | OWL Full |
| **Numero di classi** | 69 |
| **Object properties** | 52 |
| **Data properties** | 54 |

BIBO è stata la prima ontologia OWL dedicata alla descrizione di entità bibliografiche. Adotta una struttura "piatta" dove la maggior parte delle classi (38) sono sottoclassi dirette di `bibo:Document`. Integra nativamente Dublin Core Terms (DC Terms), PRISM e FOAF per la rappresentazione di metadati e persone.

**Caratteristiche principali:**
- Struttura semplice e intuitiva
- Mappatura diretta con i formati bibliografici tradizionali
- Ampia adozione nella comunità Linked Data
- Supporto per identificatori (DOI, ISBN, ISSN, Handle)
- Proprietà per liste ordinate di contributori (`bibo:authorList`, `bibo:editorList`)

### 2.2 FaBIO (FRBR-aligned Bibliographic Ontology)

| Caratteristica | Valore |
|---------------|--------|
| **Sviluppatori** | David Shotton e Silvio Peroni (progetto SPAR) |
| **Anno di rilascio** | 2010 |
| **Versione attuale** | 2.1 |
| **Namespace** | `http://purl.org/spar/fabio/` |
| **Standard** | OWL 2 DL |
| **Numero di classi** | 211 |
| **Object properties** | 69 |
| **Data properties** | 45 |

FaBIO è strutturata secondo il modello concettuale FRBR (Functional Requirements for Bibliographic Records), che distingue quattro livelli di astrazione:

1. **Work** (Opera): la creazione intellettuale astratta
2. **Expression** (Espressione): la realizzazione specifica di un'opera
3. **Manifestation** (Manifestazione): l'incarnazione fisica o digitale
4. **Item** (Esemplare): la singola copia fisica

FaBIO fa parte dell'ecosistema SPAR (Semantic Publishing and Referencing Ontologies) che include:
- **CiTO**: Citation Typing Ontology (32 tipologie di citazione)
- **DoCO**: Document Components Ontology
- **PRO**: Publishing Roles Ontology
- **PSO**: Publishing Status Ontology

## 3. Confronto Dettagliato

### 3.1 Struttura e Modello Concettuale

| Aspetto | BIBO | FaBIO |
|---------|------|-------|
| **Struttura** | Piatta (38 sottoclassi di `bibo:Document`) | Gerarchica (FRBR a 4 livelli) |
| **Complessità** | Bassa | Alta |
| **Curva di apprendimento** | Rapida | Ripida |
| **Granularità** | Orientata al documento | Orientata all'entità concettuale |
| **Flessibilità** | Pragmatica | Teoricamente rigorosa |

**BIBO** tratta ogni risorsa bibliografica come un documento con proprietà associate. Questa struttura riflette direttamente il modo in cui BibTeX e altri formati bibliografici organizzano i dati.

**FaBIO** richiede la distinzione tra l'opera intellettuale e le sue varie realizzazioni. Ad esempio, un articolo scientifico sarebbe rappresentato come:
- Un `fabio:Work` per il concetto intellettuale
- Una `fabio:Expression` per la versione preprint
- Una `fabio:Expression` diversa per la versione pubblicata
- Diverse `fabio:Manifestation` per le versioni PDF, HTML, ecc.

### 3.2 Copertura del Dominio

| Dominio | BIBO | FaBIO |
|---------|------|-------|
| **Pubblicazioni accademiche** | Buona | Eccellente |
| **Documenti legali** | Eccellente (bills, briefs, court reporters) | Limitata |
| **Dataset e software** | Limitata (estensibile) | Nativa |
| **Comunicazioni web** | Limitata | Buona (blog, tweet, web page) |
| **Eventi sociali** | Presente | Assente |
| **Finanziamenti/Grant** | Assente | Presente |

### 3.3 Conformità agli Standard OWL

| Aspetto | BIBO | FaBIO |
|---------|------|-------|
| **Specifica OWL** | OWL Full | OWL 2 DL |
| **Compatibilità reasoner** | Limitata | Completa |
| **Costrutti problematici** | `rdf:List`, `rdf:Seq`, `rdfs:Resource` | Nessuno |

BIBO utilizza costrutti RDF (`rdf:List`, `rdf:Seq`) che non sono compatibili con OWL 2 DL. Questo limita l'uso di reasoner semantici per inferenze automatiche. FaBIO, essendo conforme a OWL 2 DL, supporta pienamente il ragionamento automatico.

### 3.4 Integrazione con Altri Sistemi

**BIBO:**
- Ontologia standalone
- Integra: DC Terms, PRISM, FOAF
- Nessuna dipendenza esterna obbligatoria

**FaBIO:**
- Parte dell'ecosistema SPAR
- Richiede altre ontologie per funzionalità complete:
  - CiTO per le citazioni
  - PRO per i ruoli
  - PSO per gli stati di pubblicazione
- Maggiore espressività a costo di maggiore complessità

### 3.5 Mappatura con BibTeX

La tabella seguente confronta come i campi BibTeX comuni vengono mappati nelle due ontologie:

| Campo BibTeX | BIBO | FaBIO |
|--------------|------|-------|
| `@type` | `rdf:type` → sottoclasse di `bibo:Document` | `rdf:type` → sottoclasse di `fabio:Expression` |
| `title` | `dcterms:title` | `dcterms:title` |
| `author` | `bibo:authorList` (lista RDF ordinata) | `dcterms:creator` + PRO per ruoli |
| `year` | `dcterms:issued` | `fabio:hasPublicationYear` o `dcterms:issued` |
| `journal` | `dcterms:isPartOf` → container | `frbr:partOf` → `fabio:Journal` |
| `volume` | `bibo:volume` | `prism:volume` |
| `pages` | `bibo:pages`, `bibo:pageStart`, `bibo:pageEnd` | `prism:startingPage`, `prism:endingPage` |
| `doi` | `bibo:doi` | `datacite:doi` o `prism:doi` |
| `isbn` | `bibo:isbn10`, `bibo:isbn13` | `prism:isbn` |
| `abstract` | `dcterms:abstract` | `dcterms:abstract` |
| `keywords` | `dcterms:subject` | `prism:keyword` |

## 4. Analisi Critica

### 4.1 Vantaggi di BIBO per questo Progetto

1. **Semplicità strutturale**: La struttura piatta di BIBO consente una mappatura quasi diretta con le entry BibTeX. Ogni tipo BibTeX corrisponde a una classe BIBO senza necessità di creare entità intermedie.

2. **Maturità e stabilità**: BIBO è un'ontologia consolidata dal 2008 con specifiche stabili. Questo garantisce compatibilità a lungo termine.

3. **Ampia adozione**: BIBO è utilizzata in numerosi progetti di Linked Data bibliografici, garantendo interoperabilità con dataset esistenti.

4. **Compatibilità con RDF4J**: L'integrazione con VocBench tramite RDF4J è diretta, senza necessità di gestire la complessità del modello FRBR.

5. **Conversione bidirezionale efficiente**: La struttura semplice minimizza la perdita di informazioni nel ciclo BibTeX → BIBO → BibTeX.

6. **Estensibilità controllata**: Il namespace `bibo-ext` (definito in questo progetto) permette di estendere BIBO con proprietà aggiuntive mantenendo la compatibilità.

### 4.2 Svantaggi di FaBIO per questo Progetto

1. **Complessità FRBR**: La distinzione Work/Expression/Manifestation/Item introduce un overhead significativo per la conversione da BibTeX, che non distingue questi livelli.

2. **Dipendenze multiple**: L'uso efficace di FaBIO richiede l'adozione di altre ontologie SPAR (CiTO, PRO, PSO), aumentando la complessità del sistema.

3. **Conversione lossy nel ritorno**: La ricchezza semantica di FaBIO non può essere preservata completamente nella conversione verso BibTeX, causando perdita di informazioni maggiore rispetto a BIBO.

4. **Curva di apprendimento**: La comprensione del modello FRBR richiede formazione specifica per gli utenti finali.

### 4.3 Criticità Note di FaBIO

La letteratura ha evidenziato alcune criticità nell'implementazione di FaBIO:

- **Incoerenza FRBR**: Non tutte le 211 classi seguono coerentemente il modello FRBR. Alcune confondono le categorie Work/Expression con i tipi di pubblicazione (articolo, libro, ecc.).

- **Complessità per casi semplici**: Per descrivere una semplice citazione bibliografica, FaBIO richiede la creazione di multiple entità interconnesse.

## 5. Giustificazione della Scelta di BIBO

### 5.1 Requisiti del Progetto

Il progetto BIBO-BibTeX ha i seguenti requisiti:

| Requisito | Priorità |
|-----------|----------|
| Conversione bidirezionale BibTeX ↔ RDF | Alta |
| Integrazione con VocBench | Alta |
| Mantenimento dell'ordine degli autori | Alta |
| Supporto per identificatori multipli (DOI, ISBN, ISSN) | Media |
| Minimizzazione della perdita di informazioni | Alta |
| Semplicità di manutenzione | Media |

### 5.2 Come BIBO Soddisfa i Requisiti

| Requisito | Soluzione BIBO |
|-----------|----------------|
| **Conversione bidirezionale** | Mappatura 1:1 tra tipi BibTeX e classi BIBO |
| **Integrazione VocBench** | Compatibilità nativa con RDF4J |
| **Ordine autori** | `bibo:authorList` e `bibo:editorList` con liste RDF |
| **Identificatori** | Proprietà dedicate (`bibo:doi`, `bibo:isbn10`, `bibo:issn`) |
| **Minimizzare perdite** | Estensione `bibo-ext` per proprietà non standard |
| **Manutenibilità** | Struttura semplice, documentazione chiara |

### 5.3 Implementazione nel Progetto

L'implementazione attuale utilizza:

- **Namespace BIBO standard**: `http://purl.org/ontology/bibo/`
- **Estensione BIBO-ext**: `http://purl.org/ontology/bibo-ext/` per proprietà aggiuntive (ORCID, arXiv ID, PMID, ecc.)
- **Vocabolari complementari**: DC Terms, FOAF, PRISM per metadati standard

Le classi principali mappate includono:
- `bibo:Article` ← `@article`
- `bibo:Book` ← `@book`
- `bibo:Chapter` ← `@inbook`, `@incollection`
- `bibo:ConferencePaper` ← `@inproceedings`, `@conference`
- `bibo:Thesis` ← `@mastersthesis`, `@phdthesis`
- `bibo:Report` ← `@techreport`
- `bibo:Manual` ← `@manual`
- `bibo:Proceedings` ← `@proceedings`

## 6. Conclusioni

La scelta di BIBO come ontologia pivot per il progetto BIBO-BibTeX è motivata dalla sua semplicità strutturale, maturità e allineamento naturale con il formato BibTeX. Mentre FaBIO offre una rappresentazione semanticamente più ricca grazie al modello FRBR, la sua complessità introduce un overhead non giustificato per un convertitore bidirezionale.

BIBO rappresenta il compromesso ottimale tra espressività e praticità per questo caso d'uso. L'estensione `bibo-ext` permette di gestire proprietà non standard senza compromettere la compatibilità con il vocabolario base.

Per progetti che richiedano una descrizione più dettagliata del ciclo di vita delle pubblicazioni o l'integrazione con sistemi di analisi citazionale avanzati, FaBIO e l'ecosistema SPAR rappresentano un'alternativa valida. Un mapping BIBO → SPAR potrebbe essere implementato in futuro per garantire interoperabilità con tali sistemi.

## 7. Riferimenti

1. D'Arcus, B., Giasson, F. (2009). *Bibliographic Ontology Specification*. http://bibliontology.com/

2. Peroni, S., Shotton, D. (2012). FaBiO and CiTO: ontologies for describing bibliographic resources and citations. *Journal of Web Semantics*, 17: 33-43. https://doi.org/10.1016/j.websem.2012.08.001

3. OpenCitations. (2011). *Comparison of BIBO and FaBiO*. https://opencitations.wordpress.com/2011/06/29/comparison-of-bibo-and-fabio/

4. OpenCitations. (2011). *BIBO2SPAR: an RDF Mapping of BIBO to the SPAR Ontologies*. https://opencitations.wordpress.com/2011/06/29/bibo2spar-an-rdf-mapping-of-bibo-to-the-spar-ontologies/

5. IFLA Study Group. (1998). *Functional Requirements for Bibliographic Records*. München: K.G. Saur.

6. SPAR Ontologies. (2024). *FaBiO, the FRBR-aligned Bibliographic Ontology*. https://sparontologies.github.io/fabio/current/fabio.html
