# sample-bpe-microprofile
Part of the BPe responsible for generating the key and the URL of the QRCode

## Dependences

`docker volume create vm-etcd`

`
docker run -d -p 2379:2379 \
   --name etcd \
   --volume=/tmp/etcd-data:/etcd-data \
   quay.io/coreos/etcd:latest \
   /usr/local/bin/etcd \
   --name my-etcd-1 \
   --data-dir /etcd-data \
   --listen-client-urls http://0.0.0.0:2379 \
   --advertise-client-urls http://0.0.0.0:2379 \
   --listen-peer-urls http://0.0.0.0:2380 \
   --initial-advertise-peer-urls http://0.0.0.0:2380 \
   --initial-cluster my-etcd-1=http://0.0.0.0:2380 \
   --initial-cluster-token my-etcd-token \
   --initial-cluster-state new \
   --auto-compaction-retention 1 \
   -cors="*"
`


## Options

Generating the executable jar file

`mvn clean package`

Running integration tests

`mvn clean verify`

Generating the image

`mvn clean package docker:build`

Running the sample

**bpe-qrcode**

`mvn thorntail:run -Dswarm.port.offset=100`

or

`docker run -it -p 8180:8080 --name bpe-qrcode bpe-qrcode:1.0.0-SNAPSHOT`

**bpe-chave**

`mvn thorntail:run -Dswarm.port.offset=200`

or

`docker run -it -p 8280:8080 --name bpe-chave bpe-chave:1.0.0-SNAPSHOT`


## Services


## Interacting with the API

**bpe-chave**

`
curl
  -X GET
  http://localhost:8280/chave/23/20190621/68830611000/63/001/1/13/12345678
`

`
curl 
  -H "Content-Type: application/json" 
  -X POST 
  -d '{"uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13","cbp": "12345678"}'
  http://localhost:8280/chave/bean
`
