# sample-bpe-microprofile
Part of the BPe responsible for generating the key and the URL of the QRCode


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

**bpe-qrcode**

`mvn thorntail:run -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280 -Dswarm.port.offset=100`

**bpe-chave**

`mvn thorntail:run -Dswarm.port.offset=200`

or

`docker-compose up`


**Jaeger Traicing**

[http://localhost:16686/](http://localhost:16686/)


## Kubernates

**Docker Registry**

`mkdir -p /opt/docker/auth`

`docker run --entrypoint htpasswd registry:2 -Bbn admin admin > /opt/docker/auth/htpasswd`

`docker run -d -p 5000:5000 --restart=always --name registry -e REGISTRY_STORAGE_DELETE_ENABLED=true -v /opt/docker/auth:/auth -e "REGISTRY_AUTH=htpasswd" -e "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm" -e REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd -e REGISTRY_HTTP_ADDR=0.0.0.0:5000 registry:2`

**Images**

Generating the image and push them

`mvn -Ddocker.registry=localhost:5000 -Ddocker.username=admin -Ddocker.password=admin clean package docker:build docker:push`


**Minikube/Istio**

```
echo "Local IP: $(ipconfig getifaddr en0)"

minikube start --memory=8192 --cpus=4 --vm-driver=hyperkit --kubernetes-version=v1.14.0 --disk-size=30GB --insecure-registry='0.0.0.0/0'
minikube addons list
minikube addons enable heapster
minikube addons enable metrics-server
minikube addons enable ingress
```

```
# Istio 1.2.25
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.2.25 sh -
cd istio-1.2.25
export PATH=$PWD/bin:$PATH

# install
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
kubectl apply -f install/kubernetes/istio-demo.yaml

# delete
kubectl delete -f install/kubernetes/istio-demo.yaml
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl delete -f $i; done

---

kubectl get pod -n istio-system
kubectl get svc -n istio-system
kubectl --namespace istio-system top pods --containers

istioctl proxy-status

echo "Istio Services: $(minikube ip):$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')"
```

`kubectl create -f bpe-api/kubernates/namespace-bpe.json`

`kubectl label namespace bpe istio-injection=enabled --overwrite`

`kubectl create secret docker-registry service-registry --namespace=bpe --docker-server=<local ip>:5000 --docker-username=admin --docker-password=admin`

```
kubectl create -f bpe-api/kubernates/Deployment-istio.yml
kubectl create -f bpe-api/kubernates/Service.yml
kubectl create -f bpe-api/kubernates/Gateway.yml

kubectl create -f bpe-chave/kubernates/Deployment-istio.yml
kubectl create -f bpe-chave/kubernates/Service.yml

kubectl create -f bpe-qrcode/kubernates/Deployment-istio.yml
kubectl create -f bpe-qrcode/kubernates/Service.yml
```

`kubectl get pods --namespace=bpe`

`kubectl describe pod bpe-api-1.0.0 --namespace=bpe`


**Tools**

Jaeger

`kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 15032:16686`

[http://localhost:15032](http://localhost:15032) 


Kiali

`kubectl port-forward $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') -n istio-system 20001`

[http://localhost:20001/](http://localhost:20001/) 

admin:admin


Grafana

`kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000`

[http://localhost:3000/](http://localhost:3000/) 


Remove port forward

`killall kubectl` 


## Interacting with the API

Retrieving a static URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13","cbp": "12345678"}' \
  http://localhost:8080/api/qrcode
```

Retrieving a dynamic URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13"}' \
  http://localhost:8080/api/qrcode
```
