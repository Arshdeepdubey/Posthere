# Java 21 Upgrade Guide for PostHere Microservice

## 📊 Executive Summary

**Current State**: Java 17, Spring Boot 3.2.2
**Target State**: Java 21 LTS
**Migration Complexity**: LOW (no breaking changes expected)
**Estimated Time**: 1-2 hours

### Compatibility Matrix

| Component | Current | Java 21 Support | Migration Effort |
|-----------|---------|-----------------|------------------|
| Spring Boot | 3.2.2 | ✅ Full Support | None - Already compatible |
| jjwt | 0.12.3 | ✅ Full Support | None - Uses new API |
| AWS SDK v2 | 2.24.1 | ✅ Full Support | None |
| Lombok | 1.18.40 | ✅ Full Support | Minor warning only |
| Zip4j | 2.11.5 | ✅ Full Support | None |
| JPA/Hibernate | Spring default | ✅ Full Support | None |
| Spring Mail | 3.x | ✅ Full Support | None |
| H2 Database | Embedded | ✅ Full Support | None |

## 🔄 Migration Steps

### Phase 1: Local Testing (30 minutes)

#### Step 1.1: Install Java 21
```bash
# macOS with Homebrew
brew install openjdk@21

# Verify installation
java -version
# Expected: openjdk version "21.x.x" 2023-09-xx

# Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

#### Step 1.2: Update pom.xml
```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

#### Step 1.3: Clean Build
```bash
cd /Users/arshdeepdubey/Documents/github/Posthere

# Clear old build artifacts
mvn clean

# Run full build with Java 21
mvn install -B -V 2>&1 | tee build_java21.log

# Expected output: "BUILD SUCCESS"
```

#### Step 1.4: Analyze Deprecation Warnings
```bash
# Recompile with deprecation warnings
mvn compile -Xlint:deprecation 2>&1 | grep -i "deprecat" | tee deprecation_warnings.log

# Expected: Lombok Unsafe usage warning (acceptable)
# WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
```

### Phase 2: Run Test Suite (20 minutes)

#### Step 2.1: Execute All Tests
```bash
mvn test -B 2>&1 | tee test_java21.log

# Expected: Tests run: 16, Failures: 0, Errors: 0
```

#### Step 2.2: Verify Test Report
```bash
# Check test results
cat target/surefire-reports/testSuite.xml | grep -E "(tests|failures|errors)"

# View detailed results
ls -la target/surefire-reports/
```

#### Step 2.3: Check Coverage
```bash
# Install JaCoCo if not present
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS
```

### Phase 3: Docker Integration (20 minutes)

#### Step 3.1: Update Dockerfile
```dockerfile
# Change FROM line from:
# FROM eclipse-temurin:17-jre-alpine
# To:
FROM eclipse-temurin:21-jre-alpine

# Update builder image
FROM maven:3.9-eclipse-temurin-21 AS builder
```

#### Step 3.2: Build Docker Image
```bash
# Build image with Java 21
docker build -t posthere-microservice:java21 .

# Verify image size (should be similar or smaller than Java 17)
docker images | grep posthere

# Test container startup
docker run --rm -it posthere-microservice:java21
```

#### Step 3.3: Validate Container
```bash
# In separate terminal, test health endpoint
sleep 5
curl http://localhost:8080/api/health 2>/dev/null || echo "Server starting..."

# Expected response: HTTP 200 or 404 (depending on endpoint)
```

### Phase 4: GitHub Actions Update (10 minutes)

#### Step 4.1: Update CI Workflow
File: `.github/workflows/ci.yml`

```yaml
- name: Set up Java 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'eclipse-temurin'  # or 'oracle' or 'zulu'
    cache: maven
```

#### Step 4.2: Update CD Workflow
File: `.github/workflows/cd.yml`

```yaml
- name: Set up Java 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'eclipse-temurin'
    cache: maven
```

#### Step 4.3: Commit and Push
```bash
git add -A
git commit -m "chore: upgrade to Java 21 LTS"
git push origin main

# Monitor GitHub Actions tab for workflow execution
```

## 🆕 Java 21 Features to Consider

### Virtual Threads (Project Loom)
**Status**: Enabled by default in Java 21

**Current Impact**: Low - Would require ExecutorService changes
**Future Optimization**: Could improve concurrency for file uploads

```java
// Current approach (traditional threads)
ExecutorService executor = Executors.newFixedThreadPool(10);

// Java 21 virtual threads (optional enhancement)
ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
```

### Record Patterns
**Status**: Preview/available

**Possible Use Case**: Could replace DTO classes
```java
// Current DTO
@Data
public class TokenRequest {
    private String username;
}

// Potential Java 21 alternative
record TokenRequest(String username) {}
```

**Recommendation**: Keep DTOs as-is for now (Lombok works fine)

### Pattern Matching
**Status**: Available for instanceof and switch

**Possible Use Case**: Simplify status checks
```java
// Current approach
if (fileUpload instanceof FileUpload) {
    FileUpload file = (FileUpload) fileUpload;
    String status = file.getUploadStatus().toString();
}

// Java 21 pattern matching
if (fileUpload instanceof FileUpload file) {
    String status = file.getUploadStatus().toString();
}
```

## ⚠️ Known Issues & Workarounds

### Issue 1: Lombok and Unsafe
**Symptom**: Warning about `sun.misc.Unsafe`
**Impact**: None (just a warning)
**Workaround**: Already handled by using Lombok 1.18.40+

### Issue 2: Spring Boot Test Context
**Symptom**: Mockito issues with Spring context
**Impact**: Already worked around by using unit tests only
**Workaround**: Continue with current unit test approach

### Issue 3: JAR Size
**Expectation**: Might increase slightly with Java 21 dependencies
**Actual**: Typically same or slightly smaller

## 📋 Verification Checklist

- [ ] Java 21 installed locally: `java -version` shows 21.x
- [ ] pom.xml updated with `<java.version>21</java.version>`
- [ ] Local build succeeds: `mvn clean install`
- [ ] All 16 tests pass: `mvn test` shows 16/16 passed
- [ ] Deprecation warnings reviewed (Lombok warning acceptable)
- [ ] Dockerfile updated to use Java 21 base image
- [ ] Docker image builds successfully
- [ ] Docker container starts without errors
- [ ] GitHub Actions workflows updated
- [ ] CI/CD pipeline tests pass on GitHub
- [ ] Code coverage maintained > 80%
- [ ] No new security vulnerabilities detected

## 🔙 Rollback Plan

If issues arise after upgrading to Java 21:

### Step 1: Identify Issue
```bash
# Check logs for errors
tail -100f docker logs <container-id> 2>&1 | grep -i error
```

### Step 2: Rollback Changes
```bash
# Revert pom.xml
git checkout HEAD -- pom.xml

# Update Dockerfile
sed -i 's/21-jre/17-jre/g' Dockerfile

# Update workflows
git checkout HEAD -- .github/workflows/

# Rebuild locally
mvn clean install
```

### Step 3: Verify Rollback
```bash
# Confirm Java 17 build
mvn -version | grep "Java version"

# Run tests
mvn test

# Rebuild Docker
docker build -t posthere-microservice:java17 .
```

### Step 4: Communicate
```bash
# Create issue documenting the problem
git push origin rollback-java21

# Create PR with issue details
# Tag relevant team members
```

## 📊 Performance Comparison

### Expected Improvements with Java 21

| Metric | Java 17 | Java 21 | Expected Change |
|--------|---------|---------|-----------------|
| Startup Time | ~2.5s | ~2.3s | -8% (minor) |
| Memory Usage | 256-512MB | 240-480MB | -6% (minor) |
| Throughput | Baseline | +5-10% | +5-10% |
| Garbage Collection | Standard | Improved | Better pauses |

### Build Time
- Local build: No significant change (still ~2-3s)
- Docker build: Might be 5-10% faster (better caching)
- CI/CD pipeline: No significant change

## 🔒 Security Considerations

### Java 21 Security Updates
- Regular security patches available
- LTS support until September 2031
- Recommended for production use

### Module System (Java 9+)
- Currently not using Java modules
- No changes needed
- Can be adopted later for better isolation

## 📚 Additional Resources

- [Java 21 Release Notes](https://jdk.java.net/21/)
- [Spring Boot 3.2 Java 21 Support](https://spring.io/blog/2023/09/09/spring-boot-3-2-goes-native)
- [Migration Guide](https://docs.oracle.com/en/java/javase/21/migrate/)
- [Lombook 1.18 Java 21 Compatibility](https://github.com/projectlombok/lombok/releases/tag/v1.18.30)

## ✅ Final Checklist

### Before Upgrade
- [ ] Backup current code: `git tag backup-java17`
- [ ] Create feature branch: `git checkout -b upgrade/java21`
- [ ] Document current metrics
- [ ] Notify team members

### During Upgrade
- [ ] Follow all steps in Phase 1-4
- [ ] Document any issues encountered
- [ ] Keep rollback option ready

### After Upgrade
- [ ] Run full regression tests
- [ ] Monitor for issues (24 hours)
- [ ] Document lessons learned
- [ ] Update team documentation
- [ ] Merge to main branch
- [ ] Tag release: `git tag v0.0.2-java21`

---

**Last Updated**: June 2024
**Next Review**: December 2024
**Status**: Ready for Migration
