#!/bin/bash

rootFolder=$(pwd)

echo "--------------------------"
echo "Cleanup                   "
echo "--------------------------"

cp build-*.sh ../
rm -r *
cp ../build-*.sh .
rm ../build-*.sh


echo "--------------------------"
echo "Building WAR      "
echo "--------------------------"

cd $rootFolder
cd ..
mvn -Pbuild clean package


echo "--------------------------"
echo "Copying dist folder       "
echo "--------------------------"

cd target
rm activiti-admin.war
cp -rf activiti-admin/dist .
rm -rf activiti-admin/dist
cp -rf dist/* activiti-admin/




echo "--------------------------------"
echo "Zip it                          "
echo "--------------------------------"

cd $rootFolder
cd ../target/activiti-admin
zip -r activiti-admin.war *
cp activiti-admin.war ..

echo "All Done."








