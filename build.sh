GROUP=ErikFanderson
ARTIFACT=scan_generator
VERSION=0.0.1
JAR="../scan_generator-0.0.1.jar"
mvn install:install-file -DgroupId=$GROUP -DartifactId=$ARTIFACT -Dversion=$VERSION -Dfile=$JAR -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=. -DcreateChecksum=true
