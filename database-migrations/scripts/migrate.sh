#!/bin/bash
# migrate.sh
# Entrypoint script for the database-migrations container.
# Runs Flyway migrations for each service sequentially.
# Each service gets its own schema and its own location — no cross-contamination.

set -e  # exit immediately if any command fails

ENV=${ENV:-dev}
CONFIG_FILE="/flyway/conf/flyway-base.conf"
ENV_FILE="/flyway/conf/${ENV}.properties"

echo "=========================================="
echo " Running Flyway migrations"
echo " Environment : ${ENV}"
echo " DB Host     : ${DB_HOST}"
echo " DB Name     : ${DB_NAME}"
echo "=========================================="

# ── another-service migrations ──────────────────────────────────
echo ""
echo "[another-service] Starting migrations..."
flyway \
  -configFiles="${CONFIG_FILE}" \
  -locations="filesystem:/flyway/sql/another-service" \
  -schemas="${ANOTHER_SERVICE_SCHEMA}" \
  -placeholders.SERVICE_SCHEMA="${ANOTHER_SERVICE_SCHEMA}" \
  migrate
echo "[another-service] Migrations complete."

# ── add more services here as they get a DB ─────────────────────
# echo "[ingestor-service] Starting migrations..."
# flyway \
#   -configFiles="${CONFIG_FILE}" \
#   -locations="filesystem:/flyway/sql/ingestor-service" \
#   -schemas="${INGESTOR_SERVICE_SCHEMA}" \
#   migrate
# echo "[ingestor-service] Migrations complete."

echo ""
echo "=========================================="
echo " All migrations completed successfully"
echo "=========================================="