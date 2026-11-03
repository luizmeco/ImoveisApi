# Imoveis API

API REST de imóveis desenvolvida com Java 25, Spring Boot, Spring Security e MySQL.

## Executar a API com Docker

O Docker da API é independente do Docker do front. Instale Docker com Docker Compose, abra um terminal nesta pasta (`ImoveisApi`) e execute:

```sh
docker compose up --build
```

Esse Compose inicia somente a API e seu banco MySQL. A API fica disponível em <http://localhost:8081>; o banco fica na rede privada deste Compose e a API o acessa pelo host `db`. O banco persiste no volume `mysql_data` e os arquivos enviados persistem na pasta `uploads`.

Para executar em segundo plano e consultar os serviços/logs:

```sh
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

Para encerrar sem apagar os dados:

```sh
docker compose down
```

Para apagar também os dados do banco, use `docker compose down --volumes`. Essa ação é irreversível.

As credenciais e a chave JWT padrão são somente para desenvolvimento local. Podem ser substituídas por variáveis de ambiente ou por um arquivo `.env` nesta pasta: `MYSQL_ROOT_PASSWORD`, `JWT_SECRET` e `CORS_ALLOWED_ORIGINS`. As origens padrão liberadas para leitura pela aplicação local são as portas `8080` (front Docker) e `5173` (Vite). Não reutilize os valores locais em produção. O envio de e-mails também depende da configuração SMTP da aplicação.

## Executar a API e o front juntos

Os projetos são iniciados separadamente. Para usar o front junto com a API, inicie também o Compose do front em outro terminal, seguindo as instruções do README de `ImoveisFront`. A API permanece em <http://localhost:8081> e o front em <http://localhost:8080>. O catálogo do front faz uma consulta à rota pública `GET /imoveis` quando a página carrega. Os dois comandos `docker compose down` também devem ser executados separadamente, em cada pasta, quando quiser encerrar ambos.

## Executar apenas a API fora do Docker

É necessário ter Java 25 e um MySQL disponível. Ajuste `spring.datasource.*` em `src/main/resources/application.properties` para o banco local e, a partir desta pasta, execute:

```powershell
.\mvnw.cmd spring-boot:run
```
