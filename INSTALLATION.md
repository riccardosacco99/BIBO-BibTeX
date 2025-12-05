# BIBO-BibTeX Converter - Installation Guide

This guide explains how to install and configure the BIBO-BibTeX Converter plugin for VocBench.

## Prerequisites

- **Java 21** or later
- **VocBench 13.x** or compatible version
- **Maven 3.8+** (for building from source)

## Quick Installation

### Using Installation Scripts

**Linux/macOS:**
```bash
# Build the project
mvn clean package -DskipTests

# Run the installer
chmod +x install.sh
./install.sh /path/to/vocbench
```

**Windows:**
```cmd
REM Build the project
mvn clean package -DskipTests

REM Run the installer
install.bat C:\path\to\vocbench
```

### Manual Installation

1. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Locate the uber-JAR:**
   ```
   vocbench-plugin/target/vocbench-plugin-0.1.0-SNAPSHOT-all.jar
   ```

3. **Copy to VocBench plugins directory:**
   ```bash
   cp vocbench-plugin/target/vocbench-plugin-*-all.jar $VOCBENCH_HOME/plugins/
   ```

4. **Create configuration file** (optional):
   ```bash
   cp vocbench-plugin/src/main/resources/plugin.properties $VOCBENCH_HOME/config/bibo-bibtex.properties
   ```

5. **Restart VocBench**

## Configuration

The plugin can be configured via `$VOCBENCH_HOME/config/bibo-bibtex.properties`:

```properties
# Repository settings
repository.type=native
repository.dataDir=${vocbench.data}/bibo-bibtex

# Namespace for generated document IRIs
namespace.prefix=http://example.org/bibo/

# Field mapping mode
# true = only map known BibTeX fields
# false = allow custom fields (recommended)
field.mapping.strict=false

# Citation key generation strategy
# Options: author-year, author-title, hash
identifier.strategy=author-year

# Duplicate detection settings
duplicate.detection.enabled=true
duplicate.similarity.threshold=0.85
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `repository.type` | `native` | Repository type: `native`, `memory` |
| `repository.dataDir` | `${vocbench.data}/bibo-bibtex` | Directory for native repository |
| `namespace.prefix` | `http://example.org/bibo/` | Base IRI for document identifiers |
| `field.mapping.strict` | `false` | Strict field mapping mode |
| `identifier.strategy` | `author-year` | Citation key generation strategy |
| `duplicate.detection.enabled` | `true` | Enable duplicate detection |
| `duplicate.similarity.threshold` | `0.85` | Similarity threshold (0.0-1.0) |

### Environment Variable Overrides

All configuration properties can be overridden via environment variables:

```bash
export BIBO_REPOSITORY_TYPE=memory
export BIBO_NAMESPACE_PREFIX=http://my.org/bibo/
```

## Verification

After installation, verify the plugin is working:

1. Start VocBench
2. Navigate to the plugin management section
3. Confirm "BIBO-BibTeX Converter" appears in the list
4. Check the plugin status is "Active"

## Troubleshooting

### Plugin not appearing

1. Verify the JAR is in the plugins directory:
   ```bash
   ls -la $VOCBENCH_HOME/plugins/vocbench-plugin-*-all.jar
   ```

2. Check VocBench logs for errors:
   ```bash
   tail -f $VOCBENCH_HOME/logs/vocbench.log
   ```

3. Ensure Java 21+ is being used:
   ```bash
   java -version
   ```

### Configuration not loading

1. Verify the configuration file exists:
   ```bash
   cat $VOCBENCH_HOME/config/bibo-bibtex.properties
   ```

2. Check file permissions:
   ```bash
   chmod 644 $VOCBENCH_HOME/config/bibo-bibtex.properties
   ```

### Repository errors

1. Ensure the data directory is writable:
   ```bash
   mkdir -p $VOCBENCH_HOME/data/bibo-bibtex
   chmod 755 $VOCBENCH_HOME/data/bibo-bibtex
   ```

2. Check disk space availability

## Uninstallation

1. Stop VocBench
2. Remove the plugin JAR:
   ```bash
   rm $VOCBENCH_HOME/plugins/vocbench-plugin-*-all.jar
   ```
3. Optionally remove configuration:
   ```bash
   rm $VOCBENCH_HOME/config/bibo-bibtex.properties
   ```
4. Optionally remove data:
   ```bash
   rm -rf $VOCBENCH_HOME/data/bibo-bibtex
   ```

## Support

For issues and feature requests, please visit:
https://github.com/riccardosacco/bibo-bibtex/issues
