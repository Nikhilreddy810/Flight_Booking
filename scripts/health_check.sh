#!/usr/bin/env bash

set -uo pipefail

REPORT="health_report.txt"
WARNINGS=0
CRITICALS=0

check_disk_usage() {
    local USAGE="$1"
    local MOUNT="$2"

    if [ "$USAGE" -ge 90 ]; then
        echo "CRITICAL: $MOUNT is ${USAGE}% full"
        CRITICALS=$((CRITICALS + 1))
    elif [ "$USAGE" -ge 70 ]; then
        echo "WARNING: $MOUNT is ${USAGE}% full"
        WARNINGS=$((WARNINGS + 1))
    else
        echo "OK: $MOUNT is ${USAGE}% full"
    fi
}

{
    echo "=== DEVOPS HEALTH REPORT ==="
    echo "Date: $(date)"
    echo "Host: $(hostname)"
    echo

    echo "--- Disk Usage ---"

    if command -v df >/dev/null 2>&1; then
        while read -r USAGE MOUNT; do
            check_disk_usage "$USAGE" "$MOUNT"
        done < <(
            df -P / | awk 'NR > 1 {
                usage = $(NF-1)
                mount = $NF
                gsub(/%/, "", usage)
                print usage, mount
            }'
        )
    else
        echo "CRITICAL: df command is missing"
        CRITICALS=$((CRITICALS + 1))
    fi

    echo
    echo "--- Required Commands ---"

    for CMD in bash df grep awk; do
        if command -v "$CMD" >/dev/null 2>&1; then
            echo "OK: $CMD is available"
        else
            echo "CRITICAL: $CMD is missing"
            CRITICALS=$((CRITICALS + 1))
        fi
    done

    echo
    echo "--- Summary ---"
    echo "Warnings: $WARNINGS"
    echo "Critical issues: $CRITICALS"

    if [ "$CRITICALS" -gt 0 ]; then
        echo "Overall status: CRITICAL"
    elif [ "$WARNINGS" -gt 0 ]; then
        echo "Overall status: WARNING"
    else
        echo "Overall status: HEALTHY"
    fi
} > "$REPORT" 2>&1

cat "$REPORT"

if [ "$CRITICALS" -gt 0 ]; then
    exit 2
elif [ "$WARNINGS" -gt 0 ]; then
    exit 1
else
    exit 0
fi
