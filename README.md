# Banking-application
A modular, microservice-based banking application build in which each service is responsible for 
its own area of functionality.

## Table of Content
- [Overview](#overview)
- [Service](#service)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Future Improvements](#future-improvements)

## Overview
This project demonstrate backend engineering patterns including:
- Clean microservice separation with independently deployable services
- JWT based authentication and role-based access control (RBAC)
- Eureka-based service discovery
- Per-service PostgreSQL databases (Database Per Service Pattern)
- Event-driven communication via Apache Kafka with transactional outbox and idempotent consumers

## Service
| Service       | Port | Responsibility |
|---------------|----|---|
|**config-service**|8888|Centralized configuration (Spring Cloud Config)|
|**api-gateway**|8281|API Gateway for routing requests.|
|**discovery-service**|8761|Service registry/discovery (Netflix Eureka)|
|**auth-service**|8081|User authentication and authorization service.|
|**user-service**|8082|Legacy user profile service (superseded by customer-service).|
|**customer-service**|8083|Customer profile, addresses, identity documents and KYC.|

## Tech Stack
- Java 21+
- Spring Boot 4.1.0
- Maven
- PostgresSQL
- Spring Cloud (Eureka, Gateway, Feign)
- Lombok, MapStruct, SLF4J
## Getting Started
### Prerequisites
- JDK 21+
- Maven 
- Docker (for PostgreSQL and Kafka)
### Clone the repository
```bash
git clone https://github.com/arsen-R/banking-application.git
cd banking-application
```
### Start infrastructure
```bash
docker compose up -d
```
This starts PostgreSQL on port 3678 (databases `auth_service`, `user_service`, `customer_service`) and Kafka on port 9092.
### Startup Order
1) Run Eureka Server
2) Run Config Server
3) Run Gateway
4) Run other services (auth-service, customer-service, etc.)

## Registration flow (Kafka saga)
```text
POST /api/v1/auth/register
auth-service      -> User(PENDING_VERIFICATION, disabled) + outbox UserRegistered  -> auth.user.events
customer-service  -> Customer(PENDING_KYC) + outbox CustomerCreated | CustomerCreationFailed -> customer.events
auth-service      -> CustomerCreated: user ACTIVE / CustomerCreationFailed: user DISABLED
```
- Events are written to an `outbox_events` table in the same transaction as the entity change and relayed to Kafka by a scheduler, keyed by aggregate id.
- Consumers store handled event ids in `processed_events` to ignore redeliveries; failed messages go to `<topic>-dlt` after 3 retries.
- auth-service owns credentials and roles only; customer-service owns personal data, contacts, addresses, identity documents and KYC state. Other domains should reference `customerId`.

### Customer API
| Method | Path | Access |
|---|---|---|
| GET/PUT | `/api/v1/customers/me` | authenticated customer |
| POST | `/api/v1/customers/me/addresses` | authenticated customer |
| POST | `/api/v1/customers/me/documents` | authenticated customer |
| GET | `/api/v1/customers/{customerId}` | ADMIN, TELLER |
| POST | `/api/v1/customers/{customerId}/kyc/approve` | ADMIN, TELLER |
| POST | `/api/v1/customers/{customerId}/kyc/reject` | ADMIN, TELLER |

## Future Improvements
- Containerize application with Docker
- Setting up Jenkins for CI/CD
- Distributed tracing with Zipkin
- Metrics and monitoring with Prometheus
- Deploy into AWS