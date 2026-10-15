# Bank - Microservices Banking Application

Educational banking application built with Java 26 and Spring Boot 4.1.1.

This project does not handle real money or connect to real banking systems.

## Architecture

- API Gateway: single entry point for external API requests.
- Auth Service: users, authentication, and authorization.
- Account Service: bank accounts and account lifecycle.
- Transfer Service: transfer orchestration and idempotency.
- Ledger Service: immutable double-entry accounting records.

## Technology Stack

- Java 26
- Spring Boot 4.1.1
- Maven
- Spring Security
- PostgreSQL
- RabbitMQ
- Docker and Docker Compose
- Kubernetes
- Git and GitHub Actions

## Architecture Principles

- Independent deployable services.
- Separate database ownership per service.
- REST APIs for synchronous communication.
- Asynchronous messaging for distributed workflows.
- Automated tests and CI/CD.
- No real financial transactions.

## Project Status

Phase 1: repository and development environment setup.
