# Kafka Examples

A proposta desse repositório é desenvolver uma aplicação java com spring boot que é produtora e consumidora de mensagens no Kafka. 

Para facilitar a execução dos exemplos, utilize os arquivos docker-compose que estão em ./docker. Os dois arquivos subirão uma instância do Zookeeper, uma (ou três) instância(s) do Kafka e uma instância do Manager CMAK (que poderá ser acessado em http://localhost:9000), conforme indicado pelo nome do arquivo.

Para executar o docker-compose, execute o arquivo:

> cd docker  
> docker-compose -f zk-single-kafka-single-cmak.yml up -d

## Cenários

Os cenários estudados estão divididos em profiles do spring boot. 

### Cenário 1 - default

É o cenário executado com o profile default (application.yml). Nesse cenário, apenas o tópico example.topic.1 é criado pela app, porém, com valores default. O principal a notar é que o número total de partições do tópico será *1*. Na classe de configuração, é criado um consumerFactory e um error handler.

> mvn spring-boot:run

### Cenário 2 - Kafka Admin

Nesse cenário, um bean do Kafka Admin é criado, sendo possível criar tópicos programaticamente, com os valores de partições e fator de réplica definidos na criação de beans do tipo NewTopic.
Na classe de configuração, serão criados os tópicos *example.topic.2*, *example.topic.3* e *example.topic.4*.

>  mvn spring-boot:run -Dspring.profiles.active=cenario-com-admin

### Cenário 3 - Json

Nesse cenário, utilizamos a mesma classe de configuração do cenário 2. O controller, agora, produz mensagens no formato Json. O consumer também consegue ler mensagens nesse formato, utilizando um delegator, que tratará qualquer erro de deserialização. Dessa forma, a mensagem não ficará em loop no tópico quando tiver algum erro. 

> mvn spring-boot:run -Dspring.profiles.active=cenario-com-json

### Cenário 4 - Lag

Nesse cenário, podemos simular um problema conhecido: Lag. O lag ocorre quando a produção de mensagens é maior do que a leitura, causando uma taxa de lag onde as mensagens podem ficar represadas, esperando por processamento. A simulação pode ser feita atrás do script k6 que produz mensagens constantes (2 rps), enquanto o consumer está com sleep de 1s. Duas soluções são possíveis para esse cenário:
* Aumentar o número de concorrência usando novas instâncias de consumidores
* Aumentar o número de concorrência usando threads

#### Solução 1 - novas instâncias de consumidores

Primeiro, suba a aplicação no profile cenario-com-lag. Utilizaremos aqui o tópico examples.topic.2, com 10 partições, criado programaticamente.

> mvn spring-boot:run -Dspring.profiles.active=cenario-com-lag

Execute o script k6:

> cd k6
> k6 run producer.js

Acompanhe através do CMAK que o a produção das mensagens irá gerar lag no tópico. 

Para a primeira solução, interrompa a execução do script k6, e suba mais uma instância da aplicação numa outra porta:

> mvn spring-boot:run -Dspring.profiles.active=cenario-com-lag -Dspring-boot.run.arguments=--server.port=8081

Desse forma teremos uma app na porta 8080 e outra na porta 8081, para o mesmo consumer-group, o que irá forçar o rebalanceamento das partições entre as duas apps, cada uma ficando responsável pelo consumo de 5 partições cada.
Ao executar o script k6 novamente, veremos que o lag ou não existe, ou está muito baixo.

#### Solução 2 - threads

Primeiro, encerre a execução do script k6 e todas as instâncias da app. Depois, suba a aplicação no profile cenario-com-lag-threads. Utilizaremos novamente o tópico examples.topic.2, com 10 partições, criado programaticamente.

> mvn spring-boot:run -Dspring.profiles.active=cenario-com-lag-threads

Veja no CMAK, que agora, apesar de termos apenas 1 instância da app, as 10 partições do tópico examples.topic.2 são distribuídos entre duas threads. 

Ao executar o script k6 novamente, veremos que o lag ou não existe, ou está muito baixo.
