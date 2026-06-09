# Posthere Microservice - Quick Start Guide

## 5-Minute Quick Start

### 1. Build the Project
```bash
cd /Users/arshdeepdubey/Documents/github/Posthere
mvn clean install -DskipTests
```

### 2. Start the Application
```bash
mvn spring-boot:run
```

### 3. Generate JWT Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | jq -r '.token')

echo "Your Token: $TOKEN"
```

### 4. Create Test ZIP File
```bash
echo "Hello World" > test.txt
zip test.zip test.txt
```

### 5. Upload ZIP to S3
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.zip"
```

Save the `fileKey` from response (e.g., `uploads/uuid/test.zip`)

### 6. Check Upload Status
```bash
curl -X GET http://localhost:8080/api/files/details/1
```

### 7. Extract and Email
```bash
curl -X POST http://localhost:8080/api/files/extract-and-email \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileKey": "uploads/uuid/test.zip",
    "recipientEmail": "arshdeepdubey.ad@gmail.com"
  }'
```

---

## Configuration Quick Reference

**JWT Secret** (application.properties):
```properties
jwt.secret=MyDummySecretKeyForJWTTokenGenerationAndValidation123456789
```

**AWS S3** (application.properties):
```properties
aws.s3.access-key=dummy_access_key_12345
aws.s3.secret-key=dummy_secret_key_12345
aws.s3.bucket-name=posthere-bucket
```

**Email** (application.properties):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

---

## API Endpoints Summary

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/auth/generate-token` | No | Generate JWT token |
| POST | `/api/auth/validate-token` | No | Validate JWT token |
| POST | `/api/files/upload` | **Yes** | Upload ZIP to S3 (max 5MB) |
| GET | `/api/files/details/{id}` | No | Get file details & status |
| POST | `/api/files/extract-and-email` | **Yes** | Extract ZIP & email file |

---

## Database Access

**H2 Console:** http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: `password`

**View uploaded files:**
```sql
SELECT id, file_name, upload_status, destination_status FROM file_uploads;
```

---

## Troubleshooting

**Port Already in Use:**
```bash
# Change port in application.properties
server.port=9090
```

**Dependencies Not Found:**
```bash
mvn clean install
```

**JWT Token Expired:**
```bash
# Generate new token
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}'
```

**Email Not Sending (with dummy config):**
- Email will fail with dummy SMTP config
- Update spring.mail.* properties for actual email server

---

## Next Steps

1. Read [SETUP_GUIDE.md](SETUP_GUIDE.md) for detailed configuration
2. Check [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference
3. Review [curl-commands.md](curl-commands.md) for testing examples
4. Configure real AWS credentials and email server
5. Deploy to production
