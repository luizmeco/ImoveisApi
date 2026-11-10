# Imoveis API

API REST de imóveis desenvolvida com Java 25, Spring Boot, Spring Security e MySQL.

## Executar a API com Docker

O Docker da API é independente do Docker do front. Instale Docker com Docker Compose, abra um terminal nesta pasta (`ImoveisApi`) e execute:

```sh
docker compose up --build
```

Esse Compose inicia somente a API e seu banco MySQL. A API fica disponível em <http://localhost:8081>; o banco fica na rede privada deste Compose e a API o acessa pelo host `db`. O banco persiste no volume `mysql_data` e os arquivos enviados persistem na pasta `uploads`.

### Popular com dados fictícios

O seed de demonstração é opcional e fica desativado por padrão (`APP_SEED_ENABLED=false`). Para ativá-lo ao iniciar a API com Docker Compose:

```powershell
# PowerShell
$env:APP_SEED_ENABLED = "true"
docker compose up --build -d
Remove-Item Env:APP_SEED_ENABLED
```

```sh
# Bash
APP_SEED_ENABLED=true docker compose up --build -d
```

O seed cria, se ainda não existirem, três bairros de Campinas (Cambuí, Taquaral e Barão Geraldo), quatro tipos de imóvel e oito anúncios fictícios de venda e aluguel. Ele é repetível: não duplica registros com os mesmos nomes/títulos e não altera nem exclui os dados que já existem. Enquanto `APP_SEED_ENABLED=true`, a API verifica e completa os dados de demonstração a cada inicialização. Não ative o seed em um banco de produção.

Para executar a API diretamente com Maven, configure `APP_SEED_ENABLED=true` no ambiente antes de iniciar `.\mvnw.cmd spring-boot:run`. Remova/desative a variável depois, se não quiser que o seed verifique os dados em cada inicialização. Os registros já criados permanecem no banco.

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

## Validar autenticação e permissões

O administrador inicial é criado com `ADMIN_EMAIL` e `ADMIN_PASSWORD`, configurados no ambiente local. O login é `POST /public/login` e retorna `token` e `tipo`; use o token nas rotas protegidas com `Authorization: Bearer <token>`.

Fluxo sugerido com Postman:

1. Faça `GET http://localhost:8081/imoveis` sem token: esperado `200`.
2. Faça `POST http://localhost:8081/public/login` com `{"email":"<ADMIN_EMAIL>","password":"<ADMIN_PASSWORD>"}` (substitua pelos valores configurados localmente): esperado `200`, contendo `token` e `tipo`.
3. Use o token do admin em `POST /bairros`, enviando `{"nome":"Bairro teste","cidade":"Campinas","estado":"SP"}`: esperado `201`.
4. Use o token do admin em `POST /users`, enviando `{"nome":"Cliente teste","email":"cliente@example.com","senha":"SenhaTeste123","tipo":"USER"}`: esperado `201`.
5. Faça login com esse cliente e tente `POST /bairros` com o token dele: esperado `403`. Sem token, esperado `401`.
6. Tente alterar um caractere do token e repita uma rota protegida: esperado `401`.
7. Faça logout no front e confirme que ele remove a sessão e volta à vitrine.

A rota `POST /imoveis/sabermais/{id}` é pública; o envio efetivo do e-mail depende de credenciais SMTP válidas. A rota pública de cadastro, quando usada, só aceita o perfil `USER`; criação de administradores segue restrita ao administrador já autenticado.

Não envie tokens de ambientes reais a sites externos para decodificá-los. O payload de um JWT é codificado, não criptografado.
