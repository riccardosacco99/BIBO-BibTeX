# BIBO-BibTeX Converter & VocBench Plugin

**A robust, bidirectional converter between BibTeX and the Bibliographic Ontology (BIBO), designed for the Semantic Web.**

This project provides a comprehensive solution for managing bibliographic data in RDF, featuring a Java core library, a VocBench plugin, and a specialized ontology extension (`bibo-ext`) to bridge the gap between BibTeX's flat model and BIBO's semantic richness.

## 📋 Project Overview

The **BIBO-BibTeX Converter** was developed to facilitate the integration of traditional bibliographic workflows (BibTeX) with modern Semantic Web technologies. It serves two main purposes:
1.  **Data Migration:** Converting existing BibTeX libraries into structured RDF (BIBO) for use in Knowledge Graphs.
2.  **Interoperability:** Allowing VocBench users to import BibTeX data and export BIBO resources back to BibTeX format.

### Key Features
*   **Bidirectional Conversion:** Full round-trip support (BibTeX → BIBO → BibTeX).
*   **VocBench Integration:** Implements a plugin for VocBench 12+ (using PF4J) for direct Import/Export capabilities.
*   **Extended Ontology:** Includes `bibo-ext.owl`, an extension to standard BIBO that adds support for critical BibTeX fields (e.g., ORCID, eISSN, arXiv ID) not present in the original ontology.
*   **Semantic Integrity:** Preserves complex metadata like author order (via RDF Lists) and structured dates.
*   **Custom Forms:** Provides PEARL configurations for VocBench to allow user-friendly editing of bibliographic resources.



## 🏗️ Architecture

The project is organized as a multi-module Maven project:

*   **`core/`**: The heart of the application. Contains the conversion logic, the Java data model (`BiboDocument`), and unit tests. It relies on **RDF4J** for RDF handling and **JBibTeX** for BibTeX parsing.
*   **`vocbench-plugin/`**: The integration layer. Implements the **RDFLifter** and **ReformattingExporter** interfaces for Semantic Turkey/VocBench.
*   **`ontology/`**: Contains the `bibo-ext.owl` ontology file.
*   **`vocbench-config/`**: Contains custom configuration files for VocBench (Custom Forms, SPARQL queries).



## 🚀 Getting Started

### Prerequisites
*   **Java Development Kit (JDK) 17** or higher.
*   **Maven 3.8+**.
*   **VocBench 12.0+** (for plugin usage).

### Building the Project

To build the entire project (Core + Plugin), run the following command from the root directory:

```bash
mvn clean package
```

This will generate:
1.  **Core Library:** `core/target/bibo-bibtex-core-0.1.0-SNAPSHOT.jar`
2.  **VocBench Plugin:** `vocbench-plugin/target/vocbench-plugin-0.1.0-SNAPSHOT.jar` (and a shaded version).



## 🔌 Installing the Plugin in VocBench

1.  Locate the generated plugin JAR: `vocbench-plugin/target/vocbench-plugin-0.1.0-SNAPSHOT-all.jar`.
2.  Start your **VocBench** instance.
3.  Navigate to **System** (gear icon) > **Plugins**.
4.  Click on **Deploy Plugin**.
5.  Upload the `.jar` file.
6.  Once deployed, the converters "BibTeX to BIBO" and "BIBO to BibTeX" will be available in the Import/Export menus.



## 📖 Usage Guide

### Using the Shell Scripts (CLI)

The project includes several utility scripts for batch processing and testing:

*   **`./bibtex-to-rdf.sh`**: Converts all `.bib` files found in `test-data/bibtex/` into Turtle (`.ttl`) format in `test-data/bibo/`.
*   **`./test-roundtrip.sh`**: Performs a full regression test:
    1.  Converts BibTeX → RDF.
    2.  Converts that RDF back to BibTeX.
    3.  Verifies the integrity of the data.

### Using in VocBench

1.  **Importing Data:**
    *   Go to **Global Data Management** > **IO** > **Load Data**.
    *   Select your source BibTeX file.
    *   Choose **BibTeX** as the input format.
    *   The plugin will convert the entries into BIBO individuals in your project.

2.  **Exporting Data:**
    *   Go to **Global Data Management** > **IO** > **Export Data**.
    *   Select the graphs/resources you want to export.
    *   Choose **BibTeX** as the output format.
    *   The plugin will generate a `.bib` file for download.



## 📚 Ontology & Data Model

The project uses **BIBO (Bibliographic Ontology)** as the pivot model. Since standard BIBO lacks some properties commonly used in academia, we developed the **BIBO Extension (`bibo-ext`)**.

### Namespace
`http://purl.org/ontology/bibo-ext/` (prefix: `bibo-ext`)

### Key Extensions
| Property | Description |
| :--- | :--- |
| `bibo-ext:orcid` | ORCID identifier for authors. |
| `bibo-ext:eissn` | Electronic ISSN. |
| `bibo-ext:pmid` | PubMed ID. |
| `bibo-ext:arxivId` | arXiv identifier. |
| `bibo-ext:affiliation` | Links a `foaf:Person` to a `foaf:Organization`. |

For a full list of extensions, see `ontology/bibo-ext.owl`.



## ⚠️ Limitations

Converting between a graph model (RDF) and a flat key-value model (BibTeX) involves inherent trade-offs.

*   **Structure vs. Strings:** BIBO models entities (Publishers, Conferences) as separate resources. BibTeX treats them as strings. During conversion, some structured data might be flattened into string fields or notes.
*   **Information Loss:** While we strive for 100% data preservation, some specific non-standard BibTeX fields might be moved to the `note` field to avoid losing them.

For a detailed analysis, please refer to **[docs/LIMITATIONS.md](docs/LIMITATIONS.md)**.

For a comparison between BIBO and FaBIO, see **[docs/BIBO_VS_FABIO.md](docs/BIBO_VS_FABIO.md)**.



## 🛠️ Development

### Project Structure
```
.
├── core/                   # Java Core Logic
├── vocbench-plugin/        # PF4J Plugin for VocBench
├── ontology/               # OWL Extension files
├── vocbench-config/        # Custom Forms & SPARQL
├── test-data/              # Sample BibTeX files
└── docs/                   # Technical Documentation
```

### Running Tests
To run the unit tests for the core logic:

```bash
mvn test -pl core
```



## 👥 Authors
**Riccardo Sacco**