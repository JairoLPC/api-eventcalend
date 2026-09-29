# 📅 EventCalend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Flyway-Migration-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/Insomnia-Tested-5849BE?style=for-the-badge&logo=insomnia&logoColor=white" alt="Insomnia" />
</p>

## 📌 Sobre o Projeto

O **EventCalend API** é uma API RESTful desenvolvida com **Java** e **Spring Boot 3** voltada para o gerenciamento de usuários e prestadores de serviços do ecossistema de eventos (artistas, músicos, bandas, produtores, organizadores e cerimonialistas).

Este projeto foi construído para estudo prático e consolidação das melhores práticas de desenvolvimento backend moderno, adotando boas práticas como:
- **DTOs com Java Records** para transferência de dados segura e imutável.
- **Validações robustas** com Jakarta Bean Validation e expressões regulares (Regex).
- **Controle de versão do banco de dados** com Flyway Migrations.
- **Soft Delete (Exclusão Lógica)** mantendo a integridade histórica dos dados.
- **Paginação e ordenação nativas** com Spring Data JPA.
- **Mapeamento de Objetos de Valor (Value Objects)** utilizando `@Embeddable` e `@Embedded` para o endereço.
- **Testes de integração e validação de endpoints** realizados via **Insomnia**.

---

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3
- **Persistência & ORM:** Spring Data JPA / Hibernate
- **Banco de Dados:** MySQL
- **Controle de Migrações:** Flyway DB
- **Validação de Dados:** Jakarta Validation (`spring-boot-starter-validation`)
- **Produtividade:** Lombok
- **Gerenciador de Dependências:** Maven
- **Testes de API:** Insomnia

---

## 🏛️ Estrutura do Projeto

```
src/main/java/com/eventcalend/api/
├── controller/
│   └── UsuarioController.java               # Endpoints REST (CRUD de usuários)
├── endereco/
│   ├── DadosEndereco.java                   # DTO Record com validações de endereço
│   └── Endereco.java                        # Embeddable Value Object do JPA
└── usuario/
    ├── DadosCadastroUsuario.java            # DTO Record de entrada para criação
    ├── DadosCadastroUsuarioAtualizado.java  # DTO Record para atualização
    ├── DadosListagemUsuario.java            # DTO Record de saída simplificado
    ├── TipoDeUsuario.java                   # Enum com os perfis de usuários
    ├── Usuario.java                         # Entidade JPA principal
    └── UsuarioRepository.java               # Interface Spring Data JPA
```

---

## 🗄️ Banco de Dados & Migrations (Flyway)

As tabelas e evoluções do esquema são controladas automaticamente pelo Flyway:

1. **`V1__create-table-usuarios.sql`**: Criação da tabela `usuarios` e colunas de endereço embutido.
2. **`V2__alter-table-usuario-add-colun-telefone.sql`**: Adiciona coluna de telefone.
3. **`V3__alter-table-usuario-add-colun-ativo.sql`**: Adiciona a coluna `ativo` para suporte a *soft delete*.

---

## 🛣️ Endpoints da API

Base URL: `http://localhost:8080`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Cadastra um novo usuário no sistema |
| `GET` | `/usuarios` | Lista usuários ativos com paginação e ordenação |
| `PUT` | `/usuarios` | Atualiza os dados de um usuário existente |
| `DELETE` | `/usuarios/{id}` | Realiza a exclusão lógica (desativação) do usuário |

---

## 🧪 Exemplos de Requisições (Testadas no Insomnia)

### 1. Cadastrar Usuário (`POST /usuarios`)

**Headers:** `Content-Type: application/json`

**Body:**
```json
{
  "nome": "Carlos Silva",
  "email": "carlos.silva@email.com",
  "telefone": "11987654321",
  "tipodeusuario": "ARTISTA",
  "endereco": {
    "logradouro": "Avenida Paulista",
    "numero": "1578",
    "complemento": "Apto 42",
    "bairro": "Bela Vista",
    "cidade": "São Paulo",
    "estado": "SP",
    "cep": "01310-200"
  }
}
```

> **Tipos de Usuário disponíveis (`tipodeusuario`):**
> `ARTISTA`, `BANDA`, `MUSICO`, `PRODUTOR`, `ORGANIZADORES`, `CEREMONIALISTAS`

---

### 2. Listar Usuários Ativos com Paginação (`GET /usuarios`)

**Query Params suportados:**
- `page` (número da página, padrão `0`)
- `size` (quantidade por página, padrão `20`)
- `sort` (campo e direção, ex: `nome,asc` ou `email,desc`)

**Exemplo de URL:** `http://localhost:8080/usuarios?size=10&page=0&sort=nome,asc`

**Exemplo de Resposta (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Carlos Silva",
      "email": "carlos.silva@email.com",
      "telefone": "11987654321",
      "tipodeusuario": "ARTISTA"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 3. Atualizar Usuário (`PUT /usuarios`)

**Headers:** `Content-Type: application/json`

**Body:**
```json
{
  "id": 1,
  "nome": "Carlos Silva Jr.",
  "email": "carlos.junior@email.com",
  "telefone": "11999998888",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "100",
    "complemento": "Bloco B",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP",
    "cep": "01001-000"
  }
}
```

---

### 4. Exclusão Lógica (`DELETE /usuarios/{id}`)

**Exemplo:** `DELETE http://localhost:8080/usuarios/1`

> Esta operação não remove a linha do banco de dados fisicamente, mas altera a coluna `ativo = 0`, garantindo que o usuário não seja mais retornado nas listagens ativas.

---

## ⚙️ Como Executar o Projeto Localmente

### Pré-requisitos
- **Java JDK 17+** instalado e configurado no `PATH`
- **Maven 3.8+** (ou utilize o Maven Wrapper `./mvnw` incluso)
- **MySQL 8.0+** em execução

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/SEU_USUARIO/eventcalend-api.git
   cd eventcalend-api
   ```

2. **Crie o banco de dados no MySQL:**
   ```sql
   CREATE DATABASE eventcalend_api;
   ```

3. **Configure as credenciais do banco:**
   Verifique ou altere o arquivo `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost/eventcalend_api
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha
   ```

4. **Execute a aplicação:**
   ```bash
   # Utilizando o Maven Wrapper (Linux/macOS)
   ./mvnw spring-boot:run

   # Utilizando o Maven Wrapper (Windows)
   .\mvnw.cmd spring-boot:run
   ```

5. A API estará pronta para receber requisições em `http://localhost:8080`.
   As tabelas e migrações do banco serão criadas automaticamente pelo Flyway na inicialização!

---

## 👨‍💻 Autor

Desenvolvido por **Jairo** durante estudos práticos de desenvolvimento de APIs RESTful com Spring Boot 3.

---
Feito com ☕ e código!
