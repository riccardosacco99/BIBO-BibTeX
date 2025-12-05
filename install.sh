#!/bin/bash
#
# BIBO-BibTeX VocBench Plugin Installer
#
# This script installs the BIBO-BibTeX converter plugin into VocBench.
#
# Usage: ./install.sh [VOCBENCH_HOME]
#
# If VOCBENCH_HOME is not provided, the script will attempt to detect it
# or prompt for the installation directory.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_JAR="vocbench-plugin/target/vocbench-plugin-*-all.jar"
CONFIG_FILE="vocbench-plugin/src/main/resources/plugin.properties"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if the uber-JAR exists
check_jar() {
    local jar_files=($SCRIPT_DIR/$PLUGIN_JAR)
    if [ ! -f "${jar_files[0]}" ]; then
        echo_error "Plugin JAR not found. Please build the project first:"
        echo "    mvn clean package -DskipTests"
        exit 1
    fi
    PLUGIN_JAR_PATH="${jar_files[0]}"
    echo_info "Found plugin JAR: $(basename $PLUGIN_JAR_PATH)"
}

# Detect VocBench installation
detect_vocbench() {
    if [ -n "$1" ]; then
        VOCBENCH_HOME="$1"
    elif [ -n "$VOCBENCH_HOME" ]; then
        : # Use environment variable
    else
        # Common installation paths
        local common_paths=(
            "/opt/vocbench"
            "/usr/local/vocbench"
            "$HOME/vocbench"
            "$HOME/VocBench"
        )

        for path in "${common_paths[@]}"; do
            if [ -d "$path" ]; then
                VOCBENCH_HOME="$path"
                break
            fi
        done
    fi

    if [ -z "$VOCBENCH_HOME" ]; then
        echo_warn "VocBench installation not found automatically."
        read -p "Enter VocBench installation directory: " VOCBENCH_HOME
    fi

    if [ ! -d "$VOCBENCH_HOME" ]; then
        echo_error "Directory does not exist: $VOCBENCH_HOME"
        exit 1
    fi

    echo_info "VocBench home: $VOCBENCH_HOME"
}

# Create plugins directory if needed
setup_plugins_dir() {
    PLUGINS_DIR="$VOCBENCH_HOME/plugins"

    if [ ! -d "$PLUGINS_DIR" ]; then
        echo_info "Creating plugins directory..."
        mkdir -p "$PLUGINS_DIR"
    fi

    echo_info "Plugins directory: $PLUGINS_DIR"
}

# Install the plugin
install_plugin() {
    local dest="$PLUGINS_DIR/$(basename $PLUGIN_JAR_PATH)"

    echo_info "Installing plugin..."
    cp "$PLUGIN_JAR_PATH" "$dest"
    chmod 644 "$dest"

    echo_info "Plugin installed: $dest"
}

# Create default configuration
create_config() {
    local config_dir="$VOCBENCH_HOME/config"
    local config_dest="$config_dir/bibo-bibtex.properties"

    if [ ! -d "$config_dir" ]; then
        mkdir -p "$config_dir"
    fi

    if [ -f "$config_dest" ]; then
        echo_warn "Configuration file already exists: $config_dest"
        read -p "Overwrite? [y/N] " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo_info "Keeping existing configuration."
            return
        fi
    fi

    cat > "$config_dest" << 'EOF'
# BIBO-BibTeX Converter Plugin Configuration
#
# Repository settings
repository.type=native
repository.dataDir=${vocbench.data}/bibo-bibtex

# Namespace settings
namespace.prefix=http://example.org/bibo/

# Field mapping
field.mapping.strict=false

# Identifier generation strategy: author-year, author-title, hash
identifier.strategy=author-year

# Duplicate detection
duplicate.detection.enabled=true
duplicate.similarity.threshold=0.85
EOF

    chmod 644 "$config_dest"
    echo_info "Configuration created: $config_dest"
}

# Main installation
main() {
    echo ""
    echo "=========================================="
    echo " BIBO-BibTeX VocBench Plugin Installer"
    echo "=========================================="
    echo ""

    check_jar
    detect_vocbench "$1"
    setup_plugins_dir
    install_plugin
    create_config

    echo ""
    echo_info "Installation complete!"
    echo ""
    echo "Next steps:"
    echo "  1. Restart VocBench if it's running"
    echo "  2. The plugin will appear in the plugin list"
    echo "  3. Configure settings in: $VOCBENCH_HOME/config/bibo-bibtex.properties"
    echo ""
}

main "$@"
