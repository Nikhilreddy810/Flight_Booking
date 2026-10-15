#!/usr/bin/env bash

check_disk_usage() {
    local USAGE="$1"

    if [ "$USAGE" -ge 90 ]; then
        echo "CRITICAL: Disk usage is ${USAGE}%"
        return 2
    elif [ "$USAGE" -ge 70 ]; then
        echo "WARNING: Disk usage is ${USAGE}%"
        return 1
    else
        echo "OK: Disk usage is ${USAGE}%"
        return 0
    fi
}

check_disk_usage "$1"
exit $?
