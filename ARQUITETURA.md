# Arquitetura e Funcionamento do Buildup

## Visão Geral

**Buildup** é uma aplicação SaaS multi-tenant para gerenciamento de projetos de construção civil. O sistema permite que empresas construtoras gerenciem seus projetos, tarefas, cronogramas, despesas e equipes com total isolamento de dados entre clientes.

## Stack Tecnológica

### Backend
- **Spring Boot 3.5.7** - Framework principal
- **Java 21** - Linguagem de programação
- **Spring Data JPA** - Persistência e ORM
- **Spring Security** - Autenticação e autorização
- **PostgreSQL** - Banco de dados
- **Flyway** - Controle de versão do banco
- **Lombok** - Redução de código boilerplate
- **Auth0 JWT** - Tokens de autenticação

### Qualidade e Observabilidade
- **Spring Boot Actuator** - Métricas e health checks (`/actuator`)
- **SpringDoc OpenAPI** - Documentação interativa da API (`/docs`)
- **Testcontainers** - Testes de integração com PostgreSQL real
- **JUnit 5 + AssertJ** - Framework de testes

## Arquitetura Multi-Tenant

A característica mais importante do sistema é o **isolamento de dados entre empresas** (multi-tenancy) usando filtros no nível de linha do banco de dados.

### Como Funciona o Multi-Tenancy

#### 1. Fluxo de uma Requisição

```
Requisição HTTP com JWT
    ↓
SecurityFilter (valida JWT e extrai dados do usuário)
    ↓
TenantFilter (extrai company_id do token JWT)
    ↓
TenantContext.setCurrentTenant(companyId) - armazena em ThreadLocal
    ↓
TenantInterceptor (ativa filtro Hibernate antes das queries)
    ↓
Controller → Service → Repository
    ↓
Hibernate adiciona automaticamente: WHERE company_id = :tenantId
    ↓
Resultado retorna apenas dados da empresa do usuário
    ↓
TenantFilter limpa o contexto (finally block)
```

#### 2. Componentes do Multi-Tenancy

**TenantContext** (`tenant/TenantContext.java`)
- Armazena o `companyId` atual em um `ThreadLocal`
- Garante que cada thread tenha seu próprio contexto isolado
- Métodos: `setCurrentTenant()`, `getCurrentTenant()`, `clear()`

**TenantFilter** (`tenant/TenantFilter.java`)
- Filtro Servlet executado após a autenticação (Order 2)
- Extrai o `companyId` do JWT token
- Define o contexto do tenant para a requisição
- Sempre limpa o contexto ao final (bloco finally)

**TenantInterceptor** (`tenant/TenantInterceptor.java`)
- Interceptor Hibernate que ativa o filtro de tenant
- Executa antes de cada query ao banco
- Define o parâmetro `tenantId` no filtro

**BaseEntity** (`shared/entity/BaseEntity.java`)
- Classe abstrata que todas as entidades multi-tenant estendem
- Define o filtro Hibernate: `@Filter(name = "tenantFilter", condition = "company_id = :tenantId")`
- Contém campos comuns: `id`, `companyId`, `createdAt`, `updatedAt`

**TenantListener** (`shared/multitenancy/TenantListener.java`)
- JPA Entity Listener que intercepta operações no banco
- **@PrePersist**: Preenche automaticamente o `companyId` ao criar uma entidade
- **@PreUpdate**: Valida que o `companyId` não mudou e pertence ao tenant atual
- Evita modificação acidental de dados de outras empresas

**TenantHelper** (`tenant/TenantHelper.java`)
- Utilitário para desabilitar temporariamente o filtro de tenant
- Usado em operações globais como autenticação e gerenciamento de empresas
- Método principal: `executeWithoutTenantFilter(Runnable action)`

### 3. Garantias de Segurança

✅ **Impossível acessar dados de outras empresas** - O filtro Hibernate adiciona automaticamente `WHERE company_id = :tenantId` em TODAS as queries

✅ **Proteção contra modificação cruzada** - O `TenantListener` valida que o `companyId` nunca muda e pertence ao tenant atual

✅ **Thread-safe** - Cada requisição tem seu próprio `ThreadLocal` isolado

✅ **Limpeza automática** - O contexto é sempre limpo ao final da requisição (finally block)

## Modelo de Domínio

### Entidades Principais

#### 1. Company (Empresa/Tenant)
**Arquivo**: `tenant/CompanyEntity.java`

Representa as empresas clientes do sistema (os tenants).

```java
- name: Nome da empresa
- document: CNPJ
- email: Email de contato
- phone: Telefone
- address: Endereço completo
- logoUrl: URL do logotipo
- isMaster: Se é a empresa master (administradora do sistema)
- isActive: Se está ativa
```

**Regras de negócio**:
- Apenas SUPER_ADMIN pode criar empresas
- Existe uma empresa "master" que administra o sistema
- Empresas podem ser ativadas/desativadas

#### 2. User (Usuário)
**Arquivo**: `auth/UserEntity.java`

Usuários que acessam o sistema.

```java
- name: Nome completo
- email: Email (único)
- password: Senha criptografada (BCrypt)
- profilePhoto: URL da foto de perfil
- company: Empresa a que pertence (Many-to-One)
- roles: Papéis/permissões (Many-to-Many)
- isActive: Se está ativo
```

**3 formas de criar usuários**:
1. Admin cria usuário na sua empresa (`POST /auth/users`)
2. Auto-registro com código da empresa (`POST /auth/register`)
3. Criação automática ao criar empresa (admin inicial)

#### 3. Role (Papel/Permissão)
**Arquivo**: `role/RoleEntity.java`

Sistema de controle de acesso baseado em papéis (RBAC).

**Níveis de acesso**:
- **SUPER_ADMIN**: Administrador do sistema
  - Gerencia todas as empresas
  - Acessa dados de qualquer empresa
  - Cria e ativa/desativa empresas

- **ADMIN**: Administrador da empresa
  - Gerencia configurações da empresa
  - Cria/remove usuários
  - Acesso total aos projetos da empresa

- **MANAGER**: Gerente de projetos
  - Cria e gerencia projetos
  - Cria e atribui tarefas
  - Gerencia membros dos projetos

- **USER**: Usuário comum
  - Visualiza projetos atribuídos
  - Atualiza tarefas atribuídas
  - Acesso limitado

#### 4. RefreshToken (Token de Atualização)
**Arquivo**: `auth/RefreshTokenEntity.java`

Gerencia tokens de refresh para rotação de JWT.

```java
- token: Token de refresh (UUID)
- user: Usuário dono do token
- expiresAt: Data de expiração
- revokedAt: Data de revogação (logout)
- isRevoked: Se foi revogado
```

**Fluxo de autenticação**:
1. Login → Retorna access token (1h) + refresh token
2. Access token expira → Use refresh token para gerar novo access token
3. Logout → Revoga todos os refresh tokens do usuário

#### 5. Project (Projeto)
**Arquivo**: `project/ProjectEntity.java`

Projetos de construção gerenciados pelas empresas.

```java
- name: Nome do projeto
- clientName: Nome do cliente
- description: Descrição
- startDate: Data de início
- dueDate: Data de conclusão prevista
- contractValue: Valor do contrato
- downPayment: Valor de entrada
- coverImageUrl: Imagem de capa
- status: Status (IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)
- members: Membros da equipe (One-to-Many)
```

**Métodos de domínio**:
- `isOverdue()`: Verifica se está atrasado
- `canBeCompleted()`: Valida se pode ser marcado como concluído
- `getRemainingPayment()`: Calcula valor restante do contrato

**Regras**:
- Apenas ADMIN/MANAGER podem criar projetos
- Membros têm permissões específicas (canEdit)
- Rastreamento financeiro (entrada + pagamento restante)

#### 6. Task (Tarefa)
**Arquivo**: `task/TaskEntity.java`

Tarefas dentro dos projetos (WBS - Work Breakdown Structure).

```java
- name: Nome da tarefa
- description: Descrição detalhada
- project: Projeto a que pertence
- phase: Fase do cronograma (opcional)
- startDate: Data de início
- endDate: Data de término
- durationDays: Duração em dias
- status: Status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- priority: Prioridade (LOW, MEDIUM, HIGH, URGENT)
- progressPercentage: Progresso (0-100%)
- assignedTo: Usuário responsável
- orderIndex: Ordem de exibição
```

**Dependências de tarefas**:
- BLOCKS: Tarefa bloqueia outra
- DEPENDS_ON: Tarefa depende de outra
- RELATED: Tarefas relacionadas

**Métodos de domínio**:
- `isOverdue()`: Verifica atraso
- `hasBlockingDependencies()`: Verifica dependências bloqueantes
- `calculateDurationFromDates()`: Calcula duração automaticamente

**Fluxo de trabalho**:
1. PENDING → `PATCH /tasks/{id}/start` → IN_PROGRESS
2. IN_PROGRESS → Atualiza progresso → `PATCH /tasks/{id}/progress`
3. IN_PROGRESS → `PATCH /tasks/{id}/complete` → COMPLETED

#### 7. Schedule (Cronograma)
**Arquivo**: `schedule/ScheduleEntity.java`

Cronograma geral do projeto com dados agregados.

```java
- project: Projeto (One-to-One)
- startDate: Data de início planejada
- endDate: Data de término planejada
- actualStartDate: Data real de início
- actualEndDate: Data real de término
- totalDurationDays: Duração total em dias
- completedPercentage: Percentual concluído
- totalTasks: Total de tarefas
- completedTasks: Tarefas concluídas
- overdueTasks: Tarefas atrasadas
- status: Status do cronograma
- isOnTrack: Se está dentro do prazo
```

**Cálculo automático**:
- Percentual de conclusão baseado nas tarefas
- Contagem de tarefas atrasadas
- Verificação se está no prazo
- Atualização automática quando tarefas mudam

#### 8. Phase (Fase)
**Arquivo**: `schedule/PhaseEntity.java`

Fases do cronograma para organizar tarefas.

```java
- name: Nome da fase (ex: "Fundação", "Estrutura")
- description: Descrição
- schedule: Cronograma a que pertence
- startDate: Data de início
- endDate: Data de término
- actualStartDate: Data real de início
- actualEndDate: Data real de término
- status: Status (PENDING, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED)
- orderIndex: Ordem das fases
- completionPercentage: Percentual concluído
- durationDays: Duração em dias
- tasks: Tarefas da fase (One-to-Many)
```

**Métodos de domínio**:
- `isOverdue()`: Verifica atraso
- `isActive()`: Verifica se está em andamento
- `start()`: Inicia a fase
- `complete()`: Completa a fase

**Fluxo**:
1. Criar fase no cronograma
2. Atribuir tarefas à fase
3. Iniciar fase → IN_PROGRESS
4. Completar tarefas → Atualiza percentual
5. Completar fase → COMPLETED

#### 9. Milestone (Marco/Entrega)
**Arquivo**: `schedule/MilestoneEntity.java`

Marcos importantes no cronograma do projeto.

```java
- name: Nome do marco
- description: Descrição
- plannedDate: Data planejada
- actualDate: Data real de conclusão
- status: Status (PENDING, COMPLETED, DELAYED, CANCELLED)
- type: Tipo (GENERAL, PHASE_START, PHASE_END, PAYMENT)
- completionPercentage: Percentual de conclusão
- orderIndex: Ordem de exibição
```

**Tipos de marcos**:
- **GENERAL**: Marco geral do projeto
- **PHASE_START**: Início de uma fase
- **PHASE_END**: Fim de uma fase
- **PAYMENT**: Marco de pagamento

#### 10. Expense (Despesa)
**Arquivo**: `financial/ExpenseEntity.java`

Controle financeiro de despesas dos projetos.

```java
- project: Projeto relacionado
- category: Categoria da despesa
- description: Descrição
- amount: Valor total
- dueDate: Data de vencimento
- paidDate: Data de pagamento
- status: Status (PENDING, PAID, OVERDUE, CANCELLED)
- paymentMethod: Forma de pagamento
- supplier: Fornecedor
- invoiceNumber: Número da nota fiscal
- invoiceUrl: URL do documento
- hasInstallments: Se tem parcelamento
- installments: Parcelas (One-to-Many)
```

**Métodos de domínio**:
- `isOverdue()`: Verifica se está vencida
- `markAsPaid()`: Marca como paga
- `getTotalPaid()`: Total pago até agora
- `getRemainingAmount()`: Valor restante

**Suporte a parcelamento**:
- Despesas podem ter múltiplas parcelas
- Cada parcela rastreia status independente
- Cálculo automático de totais

## API Endpoints

### Autenticação (`/auth`)

```
POST /auth/login
- Login do usuário
- Body: { email, password }
- Retorna: { accessToken, refreshToken, user }

POST /auth/register
- Auto-registro com código da empresa
- Body: { name, email, password, companyCode }
- Retorna: { accessToken, refreshToken, user }

POST /auth/users
- Admin cria usuário (requer ADMIN/SUPER_ADMIN)
- Body: { name, email, password, roleIds }
- Retorna: UserResponse

POST /auth/refresh
- Atualiza access token
- Body: { refreshToken }
- Retorna: { accessToken, refreshToken }

POST /auth/logout
- Logout e revoga refresh tokens
- Retorna: 204 No Content
```

### Empresas (`/companies`)

```
POST /companies (SUPER_ADMIN)
- Cria nova empresa
- Cria usuário admin inicial

GET /companies (SUPER_ADMIN/ADMIN)
- Lista todas as empresas
- Paginado

GET /companies/{id}
- Detalhes da empresa

PUT /companies/{id} (SUPER_ADMIN/ADMIN)
- Atualiza empresa

PATCH /companies/{id}/activate (SUPER_ADMIN)
PATCH /companies/{id}/deactivate (SUPER_ADMIN)
- Ativa/desativa empresa
```

### Projetos (`/projects`)

```
POST /projects (ADMIN/MANAGER)
- Cria projeto

GET /projects
- Lista projetos da empresa (paginado)

GET /projects/my-projects
- Projetos do usuário

GET /projects/{id}
- Detalhes do projeto

PUT /projects/{id} (ADMIN/MANAGER)
- Atualiza projeto

DELETE /projects/{id} (ADMIN/MANAGER)
- Remove projeto

POST /projects/{id}/members (ADMIN/MANAGER)
- Adiciona membro

GET /projects/{id}/members
- Lista membros

DELETE /projects/{id}/members/{userId} (ADMIN/MANAGER)
- Remove membro
```

### Tarefas (`/projects/{projectId}/tasks`)

```
POST /projects/{projectId}/tasks (ADMIN/MANAGER)
- Cria tarefa

GET /projects/{projectId}/tasks
- Lista tarefas do projeto

GET /projects/{projectId}/tasks/my-tasks
- Tarefas atribuídas ao usuário

GET /projects/{projectId}/tasks/overdue
- Tarefas atrasadas

GET /projects/{projectId}/tasks/{id}
- Detalhes da tarefa

PUT /projects/{projectId}/tasks/{id} (ADMIN/MANAGER)
- Atualiza tarefa

DELETE /projects/{projectId}/tasks/{id} (ADMIN/MANAGER)
- Remove tarefa

PATCH /projects/{projectId}/tasks/{id}/start
- Inicia tarefa (muda para IN_PROGRESS)

PATCH /projects/{projectId}/tasks/{id}/complete
- Completa tarefa (muda para COMPLETED)

PATCH /projects/{projectId}/tasks/{id}/progress
- Atualiza progresso (0-100%)

POST /projects/{projectId}/tasks/{id}/dependencies
- Adiciona dependência entre tarefas

DELETE /projects/{projectId}/tasks/{id}/dependencies/{dependsOnId}
- Remove dependência
```

### Cronogramas (`/schedules`)

```
POST /schedules/project/{projectId}
- Gera cronograma do projeto

GET /schedules/project/{projectId}
- Cronograma do projeto

PUT /schedules/project/{projectId}
- Atualiza cronograma

POST /schedules/recalculate/{projectId}
- Recalcula cronograma baseado nas tarefas

GET /schedules
- Lista cronogramas da empresa

GET /schedules/delayed
- Cronogramas atrasados

POST /schedules/project/{projectId}/milestones
- Cria marco

PUT /schedules/project/{projectId}/milestones/{id}
- Atualiza marco

DELETE /schedules/project/{projectId}/milestones/{id}
- Remove marco

GET /schedules/project/{projectId}/milestones
- Lista marcos

GET /schedules/milestones/overdue
- Marcos atrasados

GET /schedules/milestones/upcoming
- Próximos marcos
```

### Fases (`/schedules/{scheduleId}/phases`)

```
POST /schedules/{scheduleId}/phases
- Cria fase no cronograma

GET /schedules/{scheduleId}/phases
- Lista fases do cronograma

GET /schedules/{scheduleId}/phases/{id}
- Detalhes da fase

PUT /schedules/{scheduleId}/phases/{id}
- Atualiza fase

DELETE /schedules/{scheduleId}/phases/{id}
- Remove fase

PATCH /schedules/{scheduleId}/phases/{id}/start
- Inicia fase

PATCH /schedules/{scheduleId}/phases/{id}/complete
- Completa fase
```

### Despesas (`/projects/{projectId}/expenses`)

```
POST /projects/{projectId}/expenses
- Cria despesa

GET /projects/{projectId}/expenses
- Lista despesas do projeto

GET /projects/{projectId}/expenses/{id}
- Detalhes da despesa

PUT /projects/{projectId}/expenses/{id}
- Atualiza despesa

DELETE /projects/{projectId}/expenses/{id}
- Remove despesa

PATCH /projects/{projectId}/expenses/{id}/pay
- Marca como paga

GET /projects/{projectId}/expenses/overdue
- Despesas vencidas
```

## Segurança

### JWT Authentication

**Configuração**: `shared/config/SecurityConfig.java`

**Dados no token**:
```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "companyId": "uuid",
  "roles": ["ADMIN"],
  "isMasterCompany": false
}
```

**Fluxo de segurança**:
1. Cliente envia credenciais → `/auth/login`
2. Sistema valida credenciais
3. Gera access token (JWT, 1 hora) + refresh token
4. Cliente usa access token no header: `Authorization: Bearer {token}`
5. SecurityFilter valida token em cada requisição
6. Token expira → Cliente usa refresh token → Novo access token

**Proteções**:
- ✅ Senhas criptografadas com BCrypt
- ✅ Tokens JWT assinados
- ✅ Refresh token rotation (segurança contra roubo de token)
- ✅ Logout revoga todos os refresh tokens
- ✅ Validação de autorização por papel (RBAC)
- ✅ Isolamento multi-tenant automático

### Endpoints Públicos

Não requerem autenticação:
- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/refresh`
- `GET /docs/**` (documentação API)
- `GET /actuator/**` (métricas)

Todos os outros endpoints requerem autenticação.

## Banco de Dados

### Migrações Flyway

**Localização**: `src/main/resources/db/migration/`

```
V1  - Cria tabela users
V2  - Cria tabela companies
V3  - Adiciona relação company em users
V4  - Cria tabela roles
V5  - Cria tabela user_roles (Many-to-Many)
V6  - Insere empresa master e super admin inicial
V7  - Cria tabela projects
V8  - Cria tabela project_members
V9  - Cria tabela tasks
V10 - Cria tabela task_dependencies
V11 - Cria tabela expenses
V12 - Cria tabelas installments e milestones
V13 - Cria tabelas schedules e milestones
V14 - Adiciona índices de performance
V15 - Adiciona company_id em tasks e expenses
V16 - Cria tabela refresh_tokens
V17 - Cria tabela phases
```

### Características do Banco

**IDs**: UUID em todas as tabelas (melhor para sistemas distribuídos)

**Índices**: Criados em colunas frequentemente consultadas:
- `company_id` (essencial para multi-tenancy)
- `status`, `priority`
- Datas (`start_date`, `due_date`, `created_at`)
- Foreign keys

**Constraints**:
- Foreign keys com `ON DELETE CASCADE` apropriados
- Check constraints (ex: percentuais entre 0-100)
- Unique constraints (ex: email de usuários)

**Timestamps**: Todas as entidades rastreiam `created_at` e `updated_at`

## Estrutura do Projeto

```
src/main/java/com/maistech/buildup/
│
├── auth/                           # Autenticação e Usuários
│   ├── domain/                     # Services e Repositories
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── RefreshTokenService.java
│   │   └── repositories/
│   ├── dto/                        # Request/Response DTOs
│   ├── exception/                  # Exceções específicas
│   ├── AuthController.java
│   ├── UserEntity.java
│   └── RefreshTokenEntity.java
│
├── company/                        # Gerenciamento de Empresas
│   ├── domain/
│   │   ├── CompanyService.java
│   │   └── CompanyRepository.java
│   ├── dto/
│   ├── CompanyController.java
│   └── exception/
│
├── project/                        # Gerenciamento de Projetos
│   ├── domain/
│   │   ├── ProjectService.java
│   │   ├── ProjectMemberService.java
│   │   └── repositories/
│   ├── dto/
│   ├── ProjectController.java
│   ├── ProjectEntity.java
│   └── ProjectMemberEntity.java
│
├── task/                          # Gerenciamento de Tarefas
│   ├── domain/
│   │   ├── TaskService.java
│   │   ├── TaskDependencyService.java
│   │   └── repositories/
│   ├── dto/
│   ├── TaskController.java
│   ├── TaskEntity.java
│   └── TaskDependencyEntity.java
│
├── schedule/                      # Cronogramas e Fases
│   ├── domain/
│   │   ├── ScheduleService.java
│   │   ├── PhaseService.java
│   │   ├── MilestoneService.java
│   │   └── repositories/
│   ├── dto/
│   ├── ScheduleController.java
│   ├── PhaseController.java
│   ├── ScheduleEntity.java
│   ├── PhaseEntity.java
│   └── MilestoneEntity.java
│
├── financial/                     # Gestão Financeira
│   ├── domain/
│   │   ├── ExpenseService.java
│   │   └── repositories/
│   ├── dto/
│   ├── ExpenseController.java
│   ├── ExpenseEntity.java
│   └── ExpenseInstallmentEntity.java
│
├── role/                          # Papéis e Permissões
│   ├── RoleEntity.java
│   ├── RoleEnum.java
│   └── RoleRepository.java
│
├── tenant/                        # Infraestrutura Multi-Tenant
│   ├── CompanyEntity.java
│   ├── CompanyRepository.java
│   ├── TenantContext.java         # ThreadLocal do tenant
│   ├── TenantFilter.java          # Servlet Filter
│   ├── TenantInterceptor.java     # Hibernate Interceptor
│   └── TenantHelper.java          # Utilitários
│
└── shared/                        # Infraestrutura Compartilhada
    ├── config/
    │   ├── SecurityConfig.java    # Spring Security
    │   ├── CorsConfig.java        # CORS
    │   ├── OpenApiConfig.java     # API Docs
    │   └── WebConfig.java         # Configurações Web
    ├── entity/
    │   └── BaseEntity.java        # Entidade base
    ├── multitenancy/
    │   ├── TenantListener.java    # JPA Listener
    │   └── TenantAware.java       # Interface
    ├── security/
    │   └── JWTUserData.java       # Dados do JWT
    └── exception/
        └── GlobalExceptionHandler.java
```

## Padrões de Design Utilizados

1. **Repository Pattern**: Abstração de acesso a dados
2. **Service Layer Pattern**: Lógica de negócio separada
3. **DTO Pattern**: Separação entre entidades e API
4. **Builder Pattern**: Construção de entidades (Lombok @Builder)
5. **Template Method**: BaseEntity para comportamento comum
6. **Strategy Pattern**: Diferentes estratégias de criação de usuário
7. **Dependency Injection**: Injeção por construtor em todo o código
8. **Single Responsibility**: Cada service/controller tem uma responsabilidade
9. **Open/Closed**: Extensão via herança (BaseEntity)
10. **Interface Segregation**: Interface TenantAware

## Testes

### Estratégia de Testes

**Localização**: `src/test/java/com/maistech/buildup/`

#### Testes Unitários
- Testam a camada de service com mocks
- Exemplos: `AuthServiceTest`, `ProjectServiceTest`, `TaskServiceTest`
- Framework: JUnit 5 + Mockito + AssertJ

#### Testes de Integração
- Testam toda a stack (Controller → Service → Repository → Banco)
- Usam Testcontainers para PostgreSQL real
- Exemplos: `LoginIntegrationTest`, `CompanyIntegrationTest`, `ProjectIntegrationTest`
- Garantem que multi-tenancy funciona corretamente

### Executando Testes

```bash
# Todos os testes
./mvnw clean test

# Apenas testes unitários
./mvnw test -Dtest=*Test

# Apenas testes de integração
./mvnw test -Dtest=*IntegrationTest

# Teste específico
./mvnw test -Dtest=TaskServiceTest
```

## Configuração

### application.properties

**Principais configurações**:

```properties
# Banco de Dados
spring.datasource.url=jdbc:postgresql://127.0.0.1:5433/buildup
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

# JWT
app.jwt.secret=${JWT_SECRET:change-this-secret-in-production}
app.jwt.expiration-seconds=3600

# API Docs
scalar.path=/docs
scalar.theme=deepSpace

# Logging
logging.level.com.maistech.buildup=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Variáveis de Ambiente

**Produção** - Defina estas variáveis:
- `JWT_SECRET`: Segredo para assinar tokens JWT (OBRIGATÓRIO)
- `SPRING_DATASOURCE_URL`: URL do PostgreSQL
- `SPRING_DATASOURCE_USERNAME`: Usuário do banco
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco

## Funcionalidades Principais

### 1. Gerenciamento de Projetos
- ✅ Criar, editar, remover projetos
- ✅ Adicionar/remover membros da equipe
- ✅ Rastrear status do projeto
- ✅ Controle financeiro (valor contrato, entrada, saldo)
- ✅ Detecção de atrasos
- ✅ Cálculo de dias restantes

### 2. Gerenciamento de Tarefas
- ✅ Criar tarefas com datas e responsáveis
- ✅ Workflow de status (Pendente → Em Progresso → Concluída)
- ✅ Prioridades (Baixa, Média, Alta, Urgente)
- ✅ Progresso percentual (0-100%)
- ✅ Dependências entre tarefas (bloqueia, depende de, relacionada)
- ✅ Organização por fases
- ✅ Detecção de dependências bloqueantes
- ✅ Cálculo automático de duração

### 3. Cronogramas e Fases
- ✅ Geração automática de cronograma baseado em tarefas
- ✅ Organização em fases (Fundação, Estrutura, etc.)
- ✅ Cálculo de percentual de conclusão
- ✅ Rastreamento de atrasos
- ✅ Status "no prazo" vs "atrasado"
- ✅ Marcos (milestones) com tipos variados
- ✅ Recálculo automático quando tarefas mudam

### 4. Gestão Financeira
- ✅ Registro de despesas por projeto
- ✅ Categorização de despesas
- ✅ Rastreamento de status de pagamento
- ✅ Suporte a parcelamento
- ✅ Anexo de notas fiscais
- ✅ Informações de fornecedor
- ✅ Detecção de pagamentos vencidos
- ✅ Cálculo de totais pagos e pendentes

### 5. Controle de Acesso
- ✅ 4 níveis de permissão (SUPER_ADMIN, ADMIN, MANAGER, USER)
- ✅ Isolamento total de dados entre empresas
- ✅ Validação de permissões em todas as operações
- ✅ Gestão de membros por projeto com permissões específicas

### 6. Autenticação Robusta
- ✅ JWT com refresh token rotation
- ✅ Logout com revogação de tokens
- ✅ Senhas criptografadas (BCrypt)
- ✅ Sessões stateless (escalável)
- ✅ Auto-registro com código da empresa

## Observabilidade

### Spring Boot Actuator

Endpoints disponíveis em `/actuator`:

```
/actuator/health        - Status da aplicação e banco
/actuator/metrics       - Métricas de performance
/actuator/info          - Informações da aplicação
/actuator/env           - Variáveis de ambiente
```

**Configuração** para produção:
```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

### Documentação da API

Acessível em: `http://localhost:8080/docs`

- Interface interativa (Scalar UI tema "deepSpace")
- Documentação automática via SpringDoc OpenAPI
- Teste de endpoints direto do browser
- Schemas de request/response
- Exemplos de uso

## Melhorias Futuras

Com base em placeholders no código, estas funcionalidades estão planejadas:

1. **Cálculo de caminho crítico** (critical path) nos cronogramas
2. **Relatórios e analytics avançados**
3. **Upload de arquivos** para notas fiscais e documentos do projeto
4. **Notificações por email** para tarefas e marcos atrasados
5. **Visualização Gantt** dos cronogramas
6. **Rastreamento de alocação de recursos**
7. **Timesheet** para registro de horas por tarefa
8. **Análise orçado vs realizado** nas despesas
9. **Dashboard com gráficos e indicadores**
10. **Exportação de relatórios** (PDF, Excel)

## Como Executar

### Pré-requisitos
- Java 21
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional, para Testcontainers)

### Banco de Dados

```bash
# Criar banco
createdb buildup

# Ou via Docker
docker run --name buildup-postgres -p 5433:5432 \
  -e POSTGRES_DB=buildup \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -d postgres:15
```

### Executar Aplicação

```bash
# Compilar
./mvnw clean compile

# Executar
./mvnw spring-boot:run

# Ou compilar e executar o JAR
./mvnw clean package
java -jar target/buildup-0.0.1-SNAPSHOT.jar
```

### Acessar

- **API**: http://localhost:8080
- **Documentação**: http://localhost:8080/docs
- **Health Check**: http://localhost:8080/actuator/health

### Primeiro Acesso

O sistema cria automaticamente (via migração V6):
- Empresa master: "Buildup Master"
- Super Admin: email `admin@buildup.com`, senha `admin123`

⚠️ **IMPORTANTE**: Mude essa senha em produção!

## Conclusão

Buildup é uma aplicação robusta, segura e escalável para gerenciamento de projetos de construção. Os principais diferenciais são:

1. **Isolamento multi-tenant robusto** com filtros Hibernate
2. **Segurança em camadas** (JWT, RBAC, validações)
3. **Arquitetura limpa** com separação clara de responsabilidades
4. **Testes abrangentes** (unitários + integração)
5. **Observabilidade** com Actuator e documentação interativa
6. **Pronto para produção** com boas práticas Spring Boot

O código segue princípios SOLID, usa padrões de design consolidados e está preparado para crescimento e manutenção de longo prazo.
