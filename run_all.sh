#!/usr/bin/env bash
exec fish "$(cd "$(dirname "$0")" && pwd)/run_all.fish" "$@"

