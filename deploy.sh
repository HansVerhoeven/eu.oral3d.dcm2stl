# Go to worker dir
cd worker

# Build application
mvn clean
mvn package -Dmaven.test.skip=true

# Build docker image
docker build -t eu.gcr.io/${PROJECT_NAME}/${APP_NAME}:$CIRCLE_SHA1 .
docker tag eu.gcr.io/${PROJECT_NAME}/${APP_NAME}:$CIRCLE_SHA1 eu.gcr.io/${PROJECT_NAME}/${APP_NAME}:latest

# Push docker images
sudo /opt/google-cloud-sdk/bin/gcloud docker -- push eu.gcr.io/${PROJECT_NAME}/${APP_NAME}

# Fix permissions
sudo chown -R ubuntu:ubuntu /home/ubuntu/.kube

# Write deployment file
sed "s=<IMAGE>=eu.gcr.io/${PROJECT_NAME}/${APP_NAME}:$CIRCLE_SHA1=g; s=<APP-NAME>=${APP_NAME}=g" < deployment-template.yml > deployment.yml
# Write service file
sed "s=<APP-NAME>=${APP_NAME}=g" < service-template.yml > service.yml

# Execute deployment
kubectl apply -f deployment.yml --record

# Expose service
kubectl apply -f service.yml --record