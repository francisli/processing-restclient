#!/bin/sh
git diff-index --quiet HEAD -- || { echo "Uncommmitted changes detected- please run this script from a clean working directory."; exit; }
mvn clean package
mkdir -p target/restclient/library
cp library.properties target/restclient
mv target/restclient.jar target/restclient/library
mv target/dependency/* target/restclient/library
cd target
zip -r restclient.zip restclient
