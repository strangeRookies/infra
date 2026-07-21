#!/bin/sh
echo "=== PROD DB TABLES ==="
PGPASSWORD=EWp12345 psql -h smart-safety-db-prod.cleq04iqogz6.ap-northeast-2.rds.amazonaws.com -U postgres -d postgres -c "SELECT count(*) FROM cameras;" 2>&1
PGPASSWORD=EWp12345 psql -h smart-safety-db-prod.cleq04iqogz6.ap-northeast-2.rds.amazonaws.com -U postgres -d postgres -c "SELECT count(*) FROM alert_events;" 2>&1

echo "=== NEW (DEV) DB TABLES ==="
PGPASSWORD=EWp12345 psql -h smart-safety-db-new.cleq04iqogz6.ap-northeast-2.rds.amazonaws.com -U postgres -d postgres -c "SELECT count(*) FROM cameras;" 2>&1
PGPASSWORD=EWp12345 psql -h smart-safety-db-new.cleq04iqogz6.ap-northeast-2.rds.amazonaws.com -U postgres -d postgres -c "SELECT count(*) FROM alert_events;" 2>&1
