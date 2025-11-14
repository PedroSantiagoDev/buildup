# README

## Multi-Tenant SaaS Application

A secure, multi-tenant B2B application with role-based access control and automatic data isolation.

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven
- PostgreSQL (or H2 for development)

### Run Application
```bash
./mvnw spring-boot:run
```

Application starts at: `http://localhost:8080`

---

## 📚 Documentation

- **[API Documentation](docs/API.md)** - All endpoints and examples
- **[Multi-Tenant Guide](docs/MULTI_TENANT.md)** - How multi-tenancy works
- **[User Management](docs/USER_COMPANY_VINCULATION.md)** - User-company workflows

---

## 🎯 Key Features

### Multi-Tenancy
- **Row-level isolation** - Each company sees only their data
- **Automatic filtering** - No manual WHERE clauses needed
- **Thread-safe** - Uses ThreadLocal for tenant context

### Security
- **JWT Authentication** - Secure token-based auth
- **Role-based Access** - USER, ADMIN, SUPER_ADMIN
- **Tenant validation** - Prevents cross-tenant data access

### User Management
- **3 ways to create users:**
  1. Admin creates users in their company
  2. Public registration with company code
  3. Company creation includes admin user

---

## 📋 API Overview

### Authentication
```
POST /api/auth/login      - Login and get JWT token
POST /api/auth/register   - Self-registration (requires companyId)
POST /api/auth/users      - Admin creates user in their company
```

### Companies
```
POST   /api/companies              - Create company (SUPER_ADMIN)
GET    /api/companies              - List all companies (SUPER_ADMIN)
GET    /api/companies/{id}         - Get company details
PUT    /api/companies/{id}         - Update company
PATCH  /api/companies/{id}/activate   - Activate company
PATCH  /api/companies/{id}/deactivate - Deactivate company
```

---

## 🔐 Roles & Permissions

| Role | Description | Permissions |
|------|-------------|-------------|
| **USER** | Regular employee | Access features in their company |
| **ADMIN** | Company administrator | Manage users and company settings |
| **SUPER_ADMIN** | System administrator | Manage all companies and users |

---

## 🏗️ Architecture

### Multi-Tenant Flow
```
Request → SecurityFilter → TenantFilter → TenantInterceptor → Service → Repository
           ↓                ↓               ↓
         Validate JWT    Extract tenant   Enable filter
                         Set context      ↓
                                          SQL: WHERE company_id = ?
```

### Components
- **TenantContext** - ThreadLocal storage for current tenant
- **TenantFilter** - Extracts companyId from JWT
- **TenantInterceptor** - Enables Hibernate filter
- **TenantListener** - Auto-fills companyId on create
- **BaseEntity** - Base class for tenant-aware entities

---

## 💡 Usage Examples

### Create Company + Admin
```bash
curl -X POST http://localhost:8080/api/companies \
  -H "Authorization: Bearer {super_admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Corp",
    "document": "12345678901234",
    "email": "contact@techcorp.com",
    "adminUser": {
      "name": "Admin User",
      "email": "admin@techcorp.com",
      "password": "admin123"
    }
  }'
```

### Admin Creates Employee
```bash
curl -X POST http://localhost:8080/api/auth/users \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@techcorp.com",
    "password": "password123",
    "roles": ["USER"]
  }'
```

### User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@techcorp.com",
    "password": "password123"
  }'
```

---

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=AuthServiceTest
```

---

## 📁 Project Structure

```
src/main/java/com/maistech/buildup/
├── auth/                    # Authentication & Users
│   ├── AuthController.java
│   ├── AuthService.java
│   └── UserEntity.java
├── company/                 # Company Management
│   ├── CompanyController.java
│   ├── CompanyService.java
│   └── CompanyEntity.java
├── role/                    # Roles
│   └── RoleEntity.java
└── shared/
    ├── config/             # Security, JWT, Web
    ├── entity/             # BaseEntity
    ├── tenant/             # Multi-tenant components
    └── exception/          # Exception handlers
```

---

## 🛠️ Configuration

### Application Properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/buildup
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Server
server.port=8080
```

---

## 🔧 Development

### Add New Tenant-Aware Entity
```java
@Entity
@Table(name = "projects")
@Getter
@Setter
public class ProjectEntity extends BaseEntity {
    
    @NotBlank
    private String name;
    
    private String description;
    
    // companyId, createdAt, updatedAt inherited from BaseEntity
    // Automatic tenant filtering configured
}
```

### Add Non-Tenant Entity
```java
@Service
public class GlobalService {
    
    private final TenantHelper tenantHelper;
    private final GlobalRepository repository;
    
    public List<Global> findAll() {
        return tenantHelper.withoutTenantFilter(() ->
            repository.findAll()
        );
    }
}
```

---

## 🚨 Troubleshooting

### Error: "No filter named 'tenantFilter'"
**Solution:** Use `TenantHelper.withoutTenantFilter()` for global entities

### Error: "company_id cannot be null"
**Solution:** Ensure TenantContext is set before creating entities

### Data from other companies appearing
**Solution:** Verify TenantFilter is enabled and running

---

## 📝 License

This project is licensed under the MIT License.

---

## 👥 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

---

## 📞 Support

For issues and questions:
- GitHub Issues: [repository-url/issues]
- Email: support@maistech.com

---

## 🎓 Learn More

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Hibernate Filters](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#filters)
- [Multi-Tenant Architecture](https://docs.microsoft.com/en-us/azure/architecture/guide/multitenant/overview)
