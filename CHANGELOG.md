# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Fixed
- **Database Constraint Violation** (2025-12-21)
  - Fixed `DataIntegrityViolationException: value too long for type character varying(5000)` error
  - Added `truncateString()` helper method to `FakePacketService` and `PacketErrorLogService`
  - Applied truncation to all string fields before database persistence:
    - `payload` (5000 chars max)
    - `result` (5000 chars max)
    - `errorMessage` (255 chars max)
    - `endpoint` (2000 chars max)
    - `requestMethod` (1000 chars max)
    - `additionalContext` (5000 chars max)
  - Fixed truncation logic bug (incorrect suffix length calculation)
  - Documented Docker build cache issues and solutions
  - See [docs/8-troubleshooting/DATABASE-CONSTRAINT-FIX.md](docs/8-troubleshooting/DATABASE-CONSTRAINT-FIX.md) for details

### Documentation
- Added comprehensive troubleshooting guide for database constraint violations
- Added quick reference guide for common fixes
- Updated troubleshooting index with new documentation links

---

## [0.0.1-SNAPSHOT] - 2025-12-21

### Added
- Initial project setup
- Spring Boot application with PostgreSQL database
- Fake packet processing API with CPU and RAM intensity simulation
- Prometheus metrics integration
- Grafana dashboard provisioning
- Docker and Docker Compose configuration
- GitHub Actions CI/CD pipeline
- Comprehensive documentation structure
- Server control API (open/close server)
- Error logging system with database persistence
- Health check endpoints
- Test suite with integration tests

### Features
- **Packet Processing**
  - CPU-intensive workload simulation (prime number calculation, hash computation)
  - RAM-intensive workload simulation (large array allocation)
  - Configurable intensity levels (1-10)
  - Database logging of all packet operations
  - Error handling and logging

- **Monitoring**
  - Prometheus metrics export
  - Custom metrics for packet processing
  - Grafana dashboard with visualizations
  - Health check endpoints

- **Server Control**
  - API to open/close server
  - State persistence
  - Request rejection when server is closed

- **Deployment**
  - Multi-environment Docker Compose configurations (dev, prod, minimal)
  - GitHub Actions workflows for CI/CD
  - Automated deployment to multiple servers
  - Health check verification

### Documentation
- Getting started guides
- Server setup guides
- Deployment guides
- API documentation
- Monitoring guides
- Troubleshooting guides
- Architecture documentation

---

## Release Notes

### Version Numbering
- **Major.Minor.Patch-SNAPSHOT** format
- SNAPSHOT indicates development version
- Versions follow semantic versioning

### Categories
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security fixes
- **Documentation**: Documentation changes

---

**Project**: CS4445 Sub Server  
**Repository**: https://github.com/your-org/CS4445-Sub-Server  
**Maintained by**: Development Team


