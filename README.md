# sample-bpe-microprofile
Part of the BPe responsible for generating the key and the URL of the QRCode


## Options

Generating the executable jar file

`mvn clean package`

Running integration tests

`mvn clean verify`

Generating the image

`mvn clean package docker:build`

Running the sample


**bpe-api**

`mvn thorntail:run -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280,-Dbpeqrcode.api.url=http://localhost:8180`

or

`docker run -it -p 8180:8080 -e bpechave.api.url=http://localhost:8280 -e bpeqrcode.api.url=http://localhost:8180 --name bpe-api bpe-api:1.0.0-SNAPSHOT`

**bpe-qrcode**

`mvn thorntail:run -Dthorntail.jvmArguments=-Dbpechave.api.url=http://localhost:8280 -Dswarm.port.offset=100`

or

`docker run -it -p 8180:8080 -e bpechave.api.url=http://localhost:8280 --name bpe-qrcode bpe-qrcode:1.0.0-SNAPSHOT`

**bpe-chave**

`mvn thorntail:run -Dswarm.port.offset=200`

or

`docker run -it -p 8280:8080 --name bpe-chave bpe-chave:1.0.0-SNAPSHOT`


## Services


## Interacting with the API

Retrieving a static URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13","cbp": "12345678"}' \
  http://192.168.64.5/api/qrcode
```

Retrieving a dynamic URL

```
curl \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{"ambiente": "2", "uf": "23", "emissao": "20190621","documento": "68830611000","modelo": "63","serie": "001","tipoEmissao": "1","numeroDocumentoFiscal": "13"}' \
  http://192.168.64.5/api/qrcode
```
