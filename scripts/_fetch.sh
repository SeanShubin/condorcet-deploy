#!/usr/bin/env bash

rm -rf generated/

mkdir -p generated/backend
cp ../condorcet-backend/console/target/condorcet-backend-console.jar generated/backend/
cp ../condorcet-backend/data/sample-data.txt generated/backend/backup.txt
cp ../json-console/app/target/json-console-app.jar generated/backend/edit-json.jar
pushd generated/backend
zip -r ../backend.zip .
popd

mkdir -p generated/frontend
cp -r ../condorcet-frontend/build/ generated/frontend
pushd generated/frontend
zip -r ../frontend.zip .
popd

mkdir -p generated/systemd
cp resources/condorcet-backend.service generated/systemd
pushd generated/systemd
zip -r ../systemd.zip .
popd

mkdir -p generated/s3/ec2
cp generated/backend.zip generated/s3/ec2
cp generated/systemd.zip generated/s3/ec2

mkdir -p generated/s3/website
cp -r generated/frontend/ generated/s3/website
