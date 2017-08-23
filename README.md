# Diolkos

Spring Cloud Stream apps using using k8s custom resources

## Prepare

Install RabbitMQ using Helm:

    helm install --name diolkos --set rabbitmqPassword=rabbit stable/rabbitmq

## Install stream-controller

    kubectl apply -f config/stream-controller-deployment.yaml

## Create CRDs

    kubectl create -f config/streamapp-resource.yaml
    kubectl create -f config/stream-resource.yaml

## Create StreamApp resources

    kubectl apply -f samples/app-time-source.yaml
    kubectl apply -f samples/app-log-sink.yaml

## Create `ticktock` Stream resource

    kubectl create -f samples/ticktock-stream.yaml

## Delete `ticktock` Stream resource

    kubectl delete stream ticktock

## Ancient  Greece

The Diolkos was a paved trackway near Corinth in Ancient Greece which enabled boats to be moved overland across the Isthmus of Corinth. (https://en.wikipedia.org/wiki/Diolkos)

https://www.youtube.com/watch?v=mG-8uaCxzq8
https://www.youtube.com/watch?v=CMppjc12M_M

