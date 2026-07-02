#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE user_db;
    CREATE DATABASE report_db;
    CREATE DATABASE analysis_db;
    CREATE DATABASE notification_db;
EOSQL

echo "Databases created: user_db, report_db, analysis_db, notification_db"
