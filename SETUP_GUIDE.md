# Posthere Microservice - Implementation Setup Guide

## Overview
This guide explains how to set up and configure the new microservice features including S3 integration, JWT authentication, email delivery, and file extraction.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- AWS S3 Account (for production)
- Gmail or SMTP server (for email)
- H2 Database (already included)

---

## Step 1: Install Dependencies

Run Maven to download all dependencies:

```bash
cd /Users/arshdeepdubey/Documents/github/Posthere
mvn clean install
```

---

## Step 2: Configure JWT Secret

Update `src/main/resources/application.properties`:

```properties
# Change the default JWT secret (min 32 characters)
jwt.secret=YourVerySecureJWTSecretKeyWith32CharactersMinimum1234567890
jwt.expiration=86400000
```

---

## Step 3: Configure AWS S3

### Option A: Using Dummy Values (Development)
Current setup uses dummy values. For testing without actual S3:

```properties
aws.s3.access-key=dummy_access_key_12345
aws.s3.secret-key=dummy_secret_key_12345
aws.s3.region=us-east-1
aws.s3.bucket-name=posthere-bucket
```

### Option B: Using Real AWS Credentials (Production)
1. Create AWS S3 bucket
2. Generate AWS Access Key and Secret Key
3. Update `application.properties`:

```properties
aws.s3.access-key=your-actual-access-key
aws.s3.secret-key=your-actual-secret-key
aws.s3.region=us-east-1
aws.s3.bucket-name=your-bucket-name
```

**Best Practice:** Use AWS IAM Roles instead of hardcoding credentials.

---

## Step 4: Configure Email (SMTP)

### Gmail Configuration
1. Enable 2-Factor Authentication on Gmail
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Update `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### Other Email Providers
- **Outlook**: `smtp.outlook.com:587`
- **Yahoo**: `smtp.mail.yahoo.com:465`
- **SendGrid**: `smtp.sendgrid.net:587`

Update the SMTP configuration accordingly.

---

## Step 5: Create Temporary Directories

The application automatically creates these directories:

```bash
mkdir -p /tmp/posthere-upload
mkdir -p /tmp/posthere-extraction
```

Or let the application create them automatically on first run.

---

## Step 6: Build and Run

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

**Expected Output:**
```
Started HelloGeminiApplication in X seconds
```

---

## Step 7: Verify Installation

### Check H2 Console
Open in browser: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: `password`

### Verify API Health
```bash
curl -X GET http://localhost:8080/actuator/health 2>/dev/null || \
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"test"}'
```

---

## Testing the Implementation

### 1. Generate JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser"}'
```

Save the token from response.

### 2. Create Test ZIP File
```bash
# Create a simple test file
echo "Test content" > test.txt

# Create ZIP (macOS/Linux)
zip test.zip test.txt
```

### 3. Upload ZIP File
```bash
TOKEN="<paste-token-here>"

curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.zip"
```

Note the `fileKey` and `id` from response.

### 4. Check File Status
```bash
curl -X GET http://localhost:8080/api/files/details/1
```

### 5. Extract and Email (with dummy SMTP)
```bash
TOKEN="<paste-token-here>"

curl -X POST http://localhost:8080/api/files/extract-and-email \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileKey": "uploads/xxxx-xxxx/test.zip",
    "recipientEmail": "arshdeepdubey.ad@gmail.com",
    "subject": "Test Email",
    "message": "Test message"
  }'
```

---

## Common Issues and Solutions

### Issue 1: JWT Token Not Working
**Problem:** 401 Unauthorized errors

**Solution:**
- Verify token format: `Bearer <token>`
- Check token expiration: `jwt.expiration=86400000`
- Validate JWT secret key length (min 32 chars)

```bash
# Generate new token
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser"}'
```

### Issue 2: File Upload Size Limit
**Problem:** 413 Payload Too Large

**Solution:** Update `application.properties`:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Issue 3: S3 Connection Failed
**Problem:** Connection timeout errors

**Solution:**
- Verify AWS credentials are correct
- Check S3 bucket name is correct
- Verify region is correct
- For local testing, use dummy values

### Issue 4: Email Not Sending
**Problem:** Email authentication fails

**Solution:**
1. Verify SMTP credentials
2. Check "Less secure apps" setting (Gmail)
3. Generate new app password (Gmail)
4. Check SMTP port: 587 (TLS) or 465 (SSL)
5. Enable debug logging:

```properties
logging.level.org.springframework.mail=DEBUG
```

### Issue 5: No Space in Temp Directory
**Problem:** File extraction fails

**Solution:**
- Clean temp directories:
```bash
rm -rf /tmp/posthere-extraction/*
rm -rf /tmp/posthere-upload/*
```

- Or change temp directory location in `application.properties`:
```properties
file.extraction.temp-dir=/var/tmp/posthere-extraction
file.upload.temp-dir=/var/tmp/posthere-upload
```

---

## Database Schema

H2 Database automatically creates tables on startup. View schema:

```sql
-- Connect via H2 console at http://localhost:8080/h2-console

SELECT * FROM FILE_UPLOADS;
```

**Table Structure:**
```sql
CREATE TABLE file_uploads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255),
    file_key VARCHAR(500) UNIQUE,
    file_size BIGINT,
    upload_status VARCHAR(50),
    destination_status VARCHAR(50),
    upload_date_time TIMESTAMP,
    sent_to_destination_date_time TIMESTAMP,
    s3_bucket_name VARCHAR(255),
    error_message TEXT,
    original_file_name VARCHAR(255)
);
```

---

## Production Deployment

### Before Deployment

1. **Security:**
   - Generate strong JWT secret (32+ chars)
   - Use AWS IAM roles instead of access keys
   - Use environment variables for sensitive data
   - Enable HTTPS

2. **Database:**
   - Replace H2 with production database (PostgreSQL, MySQL)
   - Configure connection pooling
   - Enable backups

3. **Email:**
   - Use production SMTP service (SendGrid, AWS SES)
   - Configure SPF/DKIM records
   - Set up bounce handling

4. **S3:**
   - Enable versioning
   - Configure lifecycle policies
   - Enable encryption
   - Set up CloudFront for delivery

5. **Monitoring:**
   - Enable application logs
   - Set up error tracking (Sentry, DataDog)
   - Configure alerts

### Environment Variables Setup

Create `.env` file or set system variables:

```bash
export JWT_SECRET=YourVerySecureJWTSecretKeyWith32Characters
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_S3_BUCKET=your-bucket-name
export SPRING_MAIL_USERNAME=your-email@gmail.com
export SPRING_MAIL_PASSWORD=your-app-password
```

### Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/gemini-hello-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```bash
docker build -t posthere:latest .
docker run -p 8080:8080 \
  -e JWT_SECRET=<your-secret> \
  -e AWS_ACCESS_KEY_ID=<key> \
  -e AWS_SECRET_ACCESS_KEY=<secret> \
  posthere:latest
```

---

## File Structure

```
Posthere/
├── src/
│   ├── main/
│   │   ├── java/com/example/gemini/
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java (NEW)
│   │   │   │   ├── FileController.java (NEW)
│   │   │   │   ├── DetailsController.java
│   │   │   │   └── HelloController.java
│   │   │   ├── service/
│   │   │   │   ├── S3Service.java (NEW)
│   │   │   │   ├── EmailService.java (NEW)
│   │   │   │   ├── FileExtractionService.java (NEW)
│   │   │   │   ├── FileUploadService.java (NEW)
│   │   │   │   └── DetailsService.java
│   │   │   ├── repository/
│   │   │   │   ├── FileUploadRepository.java (NEW)
│   │   │   │   └── DetailsRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── FileUpload.java (NEW)
│   │   │   │   └── Details.java
│   │   │   ├── dto/
│   │   │   │   ├── FileUploadResponseDTO.java (NEW)
│   │   │   │   ├── FileExtractionEmailRequestDTO.java (NEW)
│   │   │   │   └── DetailsDTO.java
│   │   │   ├── config/
│   │   │   │   ├── AwsS3Config.java (NEW)
│   │   │   │   ├── SecurityConfig.java (NEW)
│   │   │   │   └── LoadDatabase.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java (NEW)
│   │   │   │   └── JwtAuthenticationFilter.java (NEW)
│   │   │   └── HelloGeminiApplication.java
│   │   └── resources/
│   │       ├── application.properties (UPDATED)
│   │       └── logback-spring.xml
│   └── test/
│       └── java/...
├── pom.xml (UPDATED)
├── API_DOCUMENTATION.md (NEW)
└── curl-commands.md (UPDATED)
```

---

## Performance Tuning

### For Large Files
```properties
# Increase connection timeout
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Database connection pooling
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

### For High Traffic
```properties
# Increase thread pool
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

---

## Next Steps

1. ✅ Run the microservice: `mvn spring-boot:run`
2. ✅ Test all endpoints using curl commands
3. ✅ Configure real AWS and email credentials
4. ✅ Set up monitoring and logging
5. ✅ Deploy to production environment
6. ✅ Set up automated backups

---

## Support

For issues or questions, refer to:
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Complete API reference
- [curl-commands.md](curl-commands.md) - Testing examples
- Application logs in `logs/` directory
