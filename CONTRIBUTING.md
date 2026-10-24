# Contributing to Advanced PMMR Storage Engine

We welcome contributions, bug reports, and optimizations!

## Development Guidelines
1. **JDK Requirement**: Java 11 or higher.
2. **Code Style**: Standard Java naming conventions, 4 spaces indentation, no wildcard imports.
3. **Zero Dependencies**: Core cryptographic and data structure logic must remain dependency-free (standard library only).
4. **Testing**: Run `./run.sh test` or `run.bat test` before submitting changes. All tests and invariant checks must pass.

## Submitting Pull Requests
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/my-optimization`).
3. Commit your changes with clear semantic messages (`feat: optimize peak calculation`).
4. Push to your branch and open a Pull Request.
