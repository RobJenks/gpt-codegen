JACOCO_MODULE=$1

if [ -z $JACOCO_MODULE ]
then
  echo "Provide path to Jacoco module as script argument"
  exit 1
fi

awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print "Instructions covered: ", covered, "/", instructions; print "Test coverage: ", 100*covered/instructions, "%" }' "${JACOCO_MODULE}"/target/site/jacoco-aggregate/jacoco.csv
