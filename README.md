# sample-bpe-microprofile
Part of the BPe responsible for generating the key and the URL of the QRCode


## Options

**General**

Generating the executable jar file

`mvn clean package`

Running integration tests

`mvn clean verify`

Generating the image

`mvn clean package docker:build`

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

http://localhost:16686/


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
