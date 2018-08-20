#!/bin/bash
pg_restore -d gwcollagedb -U postgres -F t -c gwcollagedb-testdata.sql.tar
