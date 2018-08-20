To tear down and repopulate production database:

mvn -P production -DskipTests=false test

To tear down and repopulate test database:

mvn -P test -DskipTests=false test
