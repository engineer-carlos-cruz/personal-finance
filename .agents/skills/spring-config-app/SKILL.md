---
name: spring-config-app
description: Generate, review, refactor, validate, and explain secure Spring Boot configuration files using modern production-grade best practices
license: MIT
compatibility: opencode
metadata:
  category:
    - backend
    - spring
    - configuration
    - devops
  stack:
    - Spring Boot 3+
    - Java 17+
  audience:
    - backend developers
    - platform engineers
    - devops engineers
  workflow:
    - local-development
    - testing
    - staging
    - production
    - docker
    - kubernetes
    - ci-cd
---

# spring-config-app

## What I do

- Generate modern `application.yaml` and `application.properties` structures
- Design environment-aware configuration layouts for dev/test/staging/prod
- Refactor legacy Spring Boot configuration into maintainable profile-based setups
- Validate Spring configuration against current Spring Boot 3 best practices
- Enforce secure secret management and externalized configuration patterns
- Recommend production-safe defaults and operational hardening
- Improve observability, logging, actuator, and datasource configuration
- Optimize configuration for Docker, Kubernetes, and cloud-native deployments
- Explain Spring profile activation, override precedence, and config loading behavior
- Detect risky configuration patterns such as hardcoded credentials or duplicated keys

## When to use me

Use this skill when working with:

- `application.yaml`
- `application.properties`
- Spring profiles
- environment variables
- Spring Boot deployment configuration
- secure secrets handling
- Docker/Kubernetes Spring configuration
- Spring Cloud Config
- production-ready Spring Boot applications
- externalized configuration strategies
- observability and actuator setup
- datasource and connection pool configuration
- CI/CD configuration injection

Activate this skill when requests include terms such as:

- spring config
- spring boot configuration
- application yaml
- application properties
- spring profiles
- spring environment variables
- spring secrets
- configure spring boot
- production spring config
- secure spring configuration
- docker spring config
- kubernetes spring config
- spring cloud config
- spring datasource config
- spring logging config
- spring actuator config
- spring boot best practices
- env variables spring
- spring boot profiles
- spring externalized configuration

Also use this skill when the user asks about:

- profile separation or overrides
- environment setup (dev/test/staging/prod)
- production readiness
- deployment-oriented configuration
- YAML organization and readability
- cloud/container compatibility
- secrets management patterns
- migration from older Spring Boot 2.x configurations

Ask clarifying questions if:
- the deployment target is unclear
- the environment strategy is undefined
- the user does not specify YAML vs properties preferences
- the infrastructure model (VMs, containers, Kubernetes, cloud) affects the configuration design

## Operating principles

- Default to `application.yaml` unless there is a strong reason to prefer `.properties`
- Explain trade-offs when recommending `.properties`
- Prefer hierarchical YAML with minimal duplication
- Keep examples aligned with Spring Boot 3 conventions
- Never embed real secrets or credentials
- Treat configuration as deployable architecture, not simple key-value storage
- Favor explicit, secure, and maintainable configuration structures
- Keep production overrides minimal and intentional
- Separate sensitive and non-sensitive configuration clearly

## Responsibilities

This skill enforces and teaches best practices for:

1. Configuration organization
2. Spring profile separation
3. Environment-specific overrides
4. Secure secret management
5. Environment variable usage
6. Production-safe defaults
7. Cloud/container compatibility
8. Observability and logging
9. Datasource configuration
10. Externalized configuration
11. Configuration validation
12. Maintainability and readability

## Required outputs

For generation or refactor tasks, typically produce:

- `application.yaml`
- `application-dev.yaml`
- `application-test.yaml`
- `application-staging.yaml`
- `application-prod.yaml`

When relevant, also include guidance for:

- Docker and Docker Compose
- Kubernetes ConfigMaps and Secrets
- CI/CD pipelines
- cloud-native deployments
- ephemeral environments
- secret injection strategies

## Spring profile and loading guidance

Always explain and correctly apply:

- `spring.profiles.active`
- profile activation through CLI and environment variables
- `spring.config.activate.on-profile`
- configuration override precedence
- config loading order
- external configuration locations
- inheritance via shared base configuration

Preferred structure:

- shared non-sensitive defaults in `application.yaml`
- profile-specific overrides only where values differ
- explicit production configuration without dev/test leakage

## Secrets management policy

Explicitly discourage:

- hardcoded passwords
- committed secrets
- API keys in repositories
- plaintext production credentials

Prefer secret management through:

1. Environment variables
2. Docker Secrets
3. Kubernetes Secrets
4. Vault systems
5. Cloud secret managers
6. External secret providers/controllers

Always demonstrate secrets as placeholders:

```yaml
password: ${DB_PASSWORD}
secret: ${JWT_SECRET}
api-key: ${API_KEY}
