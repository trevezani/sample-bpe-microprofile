# sample-bpe-microprofile
A sample of the BPe responsible for generating the key and the URL of the QRCode


## General

Generating the executable jar file

`mvn clean package`

Running integration tests

`mvn clean verify`

Generating the image

`mvn clean package docker:build`

Remove the image

`mvn docker:remove`


## Running the sample

**bpe-api**

`mvn thorntail:run -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280,-Dbpeqrcode.api.url=http://localhost:8180`

Debugging

`mvn thorntail:run -Dthorntail.debug.port=5006 -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280,-Dbpeqrcode.api.url=http://localhost:8180`

**bpe-qrcode**

`mvn thorntail:run -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280 -Dswarm.port.offset=100`

**bpe-chave**

`mvn thorntail:run -Dswarm.port.offset=200`

or

```
mvn clean package -f bpe-api/pom.xml docker:build
mvn clean package -f bpe-qrcode/pom.xml docker:build
mvn clean package -f bpe-chave/pom.xml docker:build
```

Inside the extra/test

```
docker-compose up --build
```


**Jaeger Traicing**

[http://localhost:16686/](http://localhost:16686/)

**Kibana**

[http://localhost:5601/](http://localhost:5601/)

**Testing the API**

QRCode

```
curl -H "Content-Type: application/json" -X POST -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13"}' http://localhost:8080/api/qrcode
```

Chave

```
curl -H "Content-Type: application/json" -X POST -d '{"uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13"}' http://localhost:8080/api/chave
```


## Kubernates

**Docker Registry**

`mkdir -p /opt/docker/auth`

`docker run --rm --entrypoint htpasswd registry:2 -Bbn admin admin > /opt/docker/auth/htpasswd`

`docker run -d -p 5000:5000 --restart=always --name registry -e REGISTRY_STORAGE_DELETE_ENABLED=true -v /opt/docker/auth:/auth -e "REGISTRY_AUTH=htpasswd" -e "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm" -e REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd -e REGISTRY_HTTP_ADDR=0.0.0.0:5000 registry:2`

**Images**

Generating the image and push them

`mvn -Ddocker.registry=localhost:5000 -Ddocker.username=admin -Ddocker.password=admin clean package docker:build docker:push`


**Registry UI**

Viewer for the images present in the Registry

[https://github.com/jc21/docker-registry-ui](https://github.com/jc21/docker-registry-ui)

```
docker run --rm -it -p 5001:80 --name registry-ui -e REGISTRY_HOST=$(ipconfig getifaddr en0):5000 -e REGISTRY_STORAGE_DELETE_ENABLED=true -e REGISTRY_SSL=false -e REGISTRY_USER=admin -e REGISTRY_PASS=admin jc21/registry-ui
```

**Minikube/Istio**

Starting the Minikube

```
minikube start --memory=8192 --cpus=4 --vm-driver=hyperkit --kubernetes-version=v1.14.0 --disk-size=30GB --insecure-registry='0.0.0.0/0'
minikube addons list
minikube addons enable heapster
minikube addons enable metrics-server
minikube addons enable ingress
```

Installing the Istio

```
# Istio 1.2.25
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.2.25 sh -
cd istio-1.2.25
export PATH=$PWD/bin:$PATH

# install
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
kubectl apply -f install/kubernetes/istio-demo.yaml

# uninstall
kubectl delete -f install/kubernetes/istio-demo.yaml
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl delete -f $i; done
```

Showing the pods

```
kubectl get pod -n istio-system
kubectl get svc -n istio-system
kubectl --namespace istio-system top pods --containers

istioctl proxy-status
```

Showing the Istio Address

```
echo "Istio Services: $(minikube ip):$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')"
```

Showing the IP on MAC OS

`echo "Local IP: $(ipconfig getifaddr en0)"`


**Deploying the Sample**

Creating an environment variable for dynamic action inside the sample 

```
kubectl create configmap bpe-config --from-literal='ambiente=2' -n bpe
kubectl edit configmap bpe-config -n bpe
kubectl patch deployment bpe-api-1.0.8 -p {\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}" -n bpe
```

Creating a namespace and defining the automatic inject for the Istio

```
kubectl create -f bpe-api/kubernates/namespace-bpe.json

kubectl label namespace bpe istio-injection=enabled --overwrite
```

Defining the user and password to interact with the Registry

```
kubectl create secret docker-registry service-registry --namespace=bpe --docker-server=<local ip>:5000 --docker-username=admin --docker-password=admin
```

Creating the Service, Deployment and Gateway 

```
kubectl create -f bpe-api/kubernates/Deployment-istio.yml
kubectl create -f bpe-api/kubernates/Service.yml
kubectl create -f bpe-api/kubernates/Gateway.yml

kubectl create -f bpe-chave/kubernates/Deployment-istio.yml
kubectl create -f bpe-chave/kubernates/Service.yml

kubectl create -f bpe-qrcode/kubernates/Deployment-istio.yml
kubectl create -f bpe-qrcode/kubernates/Service.yml
```

Showing the pods

`kubectl get pods --namespace=bpe`

Showing the details 

`kubectl describe pod bpe-api-<version> --namespace=bpe`

Testing the pod

```
kubectl exec -it $(kubectl get pod -l app=bpeapi -n bpe -o jsonpath='{.items[0].metadata.name}') -c bpeapi -- curl bpeapi:8080/versao
```


**Istio Tools**

Jaeger

```kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 15032:16686```

[http://localhost:15032](http://localhost:15032) 


Kiali

```kubectl port-forward $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') -n istio-system 20001```

[http://localhost:20001/](http://localhost:20001/) 

admin:admin


Grafana

```kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000```

[http://localhost:3000/dashboard/db/istio-mesh-dashboard](http://localhost:3000/dashboard/db/istio-mesh-dashboard) 


Remove port forward

```killall kubectl``` 


**EFK (Monitoring)**

Deploying the components

```
kubectl create namespace logging

kubectl create -f extra/logging/kubernetes/elastic.yaml -n logging
kubectl create -f extra/logging/kubernetes/kibana.yaml -n logging
kubectl create -f extra/logging/kubernetes/fluentd-rbac.yaml
kubectl create -f extra/logging/kubernetes/fluentd-daemonset.yaml
```

Showing the pods

```
kubectl get pods,service -n logging
kubectl get pods -n kube-system
```

Viewing the log of the fluentd

```
kubectl logs $(kubectl get pods --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' -n kube-system | grep fluentd) -n kube-system
```

Showing the Minikube IP

`echo "Minikube IP: $(minikube ip)"`

Showing the Kibana Port

`kubectl describe svc kibana -n logging | grep NodePort`


## Interacting with the API

Retrieving a static URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13","cbp": "12345678"}' \
  http://<url>/api/qrcode
```

Retrieving a dynamic URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13"}' \
  http://<url>/api/qrcode
```
