GROUP=ErikFanderson
ARTIFACT=scan_generator
SCALA_VERSION="2.11"
VERSION="0.0.1"
JAR="../../chisel/scan_generator/out/scan_generator/assembly/dest/out.jar"
mvn install:install-file -DgroupId=$GROUP -DartifactId=${ARTIFACT}_${SCALA_VERSION} -Dversion=$VERSION -Dfile=$JAR -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=. -DcreateChecksum=true
