# Posthere Microservice - API Documentation

## Overview
This microservice provides three main API endpoints for file management, S3 integration, and email delivery with OAuth JWT authentication.

---

## Authentication

### 1. Generate JWT Token
**Endpoint:** `POST /api/auth/generate-token`
**Description:** Generate a JWT token for authenticated operations
**Authentication:** None required for token generation
**Request Body:**
```json
{
  "username": "user@example.com"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE4MDAwMDAwLCJleHAiOjE3MTgwODY0MDB9.xxxx",
  "message": "Token generated successfully"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}'
```

---

### 2. Validate JWT Token
**Endpoint:** `POST /api/auth/validate-token`
**Description:** Validate an existing JWT token
**Authentication:** None required
**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE4MDAwMDAwLCJleHAiOjE3MTgwODY0MDB9.xxxx"
}
```

**Response:**
```json
{
  "valid": true,
  "username": "user@example.com",
  "message": "Token is valid"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/validate-token \
  -H "Content-Type: application/json" \
  -d '{"token":"<your-jwt-token>"}'
```

---

## File Management Endpoints

### 3. Upload ZIP File to S3
**Endpoint:** `POST /api/files/upload`
**Description:** Upload a ZIP file (max 5MB) to S3 bucket. Requires JWT authentication.
**Authentication:** Required (Bearer Token)
**Content-Type:** multipart/form-data
**Parameters:**
- `file` (MultipartFile, required): ZIP file to upload (max 5MB)

**Request Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: multipart/form-data
```

**Response:**
```json
{
  "id": 1,
  "fileName": "archive.zip",
  "fileKey": "uploads/12345-67890/archive.zip",
  "fileSize": 1024000,
  "uploadStatus": "UPLOADED",
  "destinationStatus": "SENT",
  "uploadDateTime": "2024-06-10T10:30:00",
  "sentToDestinationDateTime": "2024-06-10T10:30:05",
  "s3BucketName": "posthere-bucket",
  "errorMessage": null,
  "message": "Operation completed successfully"
}
```

**cURL Example:**
```bash
# First, get a token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# Then upload file
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/archive.zip"
```

---

### 4. Get File Details
**Endpoint:** `GET /api/files/details/{fileId}`
**Description:** Fetch file details and its upload/destination status
**Authentication:** Not required (optional)
**Path Parameters:**
- `fileId` (Long, required): Database ID of the uploaded file

**Response:**
```json
{
  "id": 1,
  "fileName": "archive.zip",
  "fileKey": "uploads/12345-67890/archive.zip",
  "fileSize": 1024000,
  "uploadStatus": "UPLOADED",
  "destinationStatus": "SENT",
  "uploadDateTime": "2024-06-10T10:30:00",
  "sentToDestinationDateTime": "2024-06-10T10:30:05",
  "s3BucketName": "posthere-bucket",
  "errorMessage": null,
  "message": "Operation completed successfully"
}
```

**Status Values:**
- **uploadStatus**: `PENDING`, `UPLOADED`, `FAILED`
- **destinationStatus**: `NOT_SENT`, `SENT`, `FAILED`

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/files/details/1
```

---

### 5. Extract ZIP and Send Email
**Endpoint:** `POST /api/files/extract-and-email`
**Description:** Extract ZIP file from S3 and send extracted files via email. Requires JWT authentication.
**Authentication:** Required (Bearer Token)
**Content-Type:** application/json

**Request Body:**
```json
{
  "fileKey": "uploads/12345-67890/archive.zip",
  "recipientEmail": "arshdeepdubey.ad@gmail.com",
  "subject": "Your Extracted Files",
  "message": "Please find your extracted files attached."
}
```

**Request Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Response:**
```json
{
  "id": 1,
  "fileName": "archive.zip",
  "fileKey": "uploads/12345-67890/archive.zip",
  "fileSize": 1024000,
  "uploadStatus": "UPLOADED",
  "destinationStatus": "SENT",
  "uploadDateTime": "2024-06-10T10:30:00",
  "sentToDestinationDateTime": "2024-06-10T10:35:00",
  "s3BucketName": "posthere-bucket",
  "errorMessage": null,
  "message": "Operation completed successfully"
}
```

**Workflow:**
1. Validates file exists in S3
2. Downloads file from S3
3. Extracts ZIP file to temporary local storage
4. Sends extracted file(s) via email with HTML template
5. Cleans up temporary extraction directory
6. Updates database with email send status

**cURL Example:**
```bash
# First, get a token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# Then extract and email
curl -X POST http://localhost:8080/api/files/extract-and-email \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileKey": "uploads/12345-67890/archive.zip",
    "recipientEmail": "arshdeepdubey.ad@gmail.com",
    "subject": "Your Extracted Files",
    "message": "Please find your files attached."
  }'
```

---

## Configuration

### AWS S3 Setup
Update `application.properties`:
```properties
aws.s3.access-key=your-actual-aws-access-key
aws.s3.secret-key=your-actual-aws-secret-key
aws.s3.region=us-east-1
aws.s3.bucket-name=your-bucket-name
```

### Email Configuration
Update `application.properties` for SMTP:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### JWT Secret Configuration
Update `application.properties`:
```properties
jwt.secret=your-secret-key-min-32-characters
jwt.expiration=86400000
```

---

## Error Handling

All endpoints return appropriate HTTP status codes:
- **200 OK**: Successful GET/POST request
- **201 CREATED**: File successfully created
- **400 Bad Request**: Invalid request parameters or file validation failed
- **401 Unauthorized**: Missing or invalid JWT token
- **404 Not Found**: File not found in database or S3
- **500 Internal Server Error**: Server error during processing

**Error Response Format:**
```json
{
  "message": "Error description",
  "errorMessage": "Detailed error information",
  "uploadStatus": null,
  "destinationStatus": null
}
```

---

## Validation Rules

### File Upload
- File size: Maximum 5MB
- File type: Only ZIP files allowed (`application/zip`, `application/x-zip-compressed`)
- Required: JWT token in Authorization header

### Extract and Email
- File must exist in S3 bucket
- Recipient email must be valid
- Required: JWT token in Authorization header

---

## Database Schema

### file_uploads Table
```sql
CREATE TABLE file_uploads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_key VARCHAR(500) NOT NULL UNIQUE,
    file_size BIGINT,
    upload_status VARCHAR(50) NOT NULL, -- PENDING, UPLOADED, FAILED
    destination_status VARCHAR(50) NOT NULL, -- NOT_SENT, SENT, FAILED
    upload_date_time TIMESTAMP,
    sent_to_destination_date_time TIMESTAMP,
    s3_bucket_name VARCHAR(255),
    error_message TEXT,
    original_file_name VARCHAR(255)
);
```

---

## Temporary Storage

- **File Upload Temp**: `/tmp/posthere-upload`
- **File Extraction Temp**: `/tmp/posthere-extraction`

These directories are automatically created and managed by the application. Extracted files are cleaned up after email delivery.

---

## Email Template

Default HTML email template includes:
- Header with "File Delivery Notification"
- File details (name, delivered date, status)
- Extracted file attachment
- Professional footer

Template is customizable via `EmailService.getDefaultEmailTemplate()`

---

## Testing Workflow

1. **Generate Token**
```bash
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser"}'
```

2. **Upload File**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@test.zip"
```

3. **Check Status**
```bash
curl -X GET http://localhost:8080/api/files/details/1
```

4. **Extract and Email**
```bash
curl -X POST http://localhost:8080/api/files/extract-and-email \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"fileKey":"uploads/xxx/test.zip","recipientEmail":"user@example.com"}'
```

---

## Security Notes

- ⚠️ **JWT Secret**: Change the default secret key in production
- ⚠️ **AWS Credentials**: Never hardcode credentials; use IAM roles or environment variables
- ⚠️ **Email Credentials**: Store securely; use app-specific passwords for Gmail
- ⚠️ **CORS**: Configure CORS policy in production
- ⚠️ **HTTPS**: Always use HTTPS in production

---

## Future Enhancements

- [ ] Database persistence (move from H2 to production DB)
- [ ] Multiple file extraction and combined email
- [ ] File encryption before S3 upload
- [ ] Async processing for large files
- [ ] S3 lifecycle policies
- [ ] Email template customization
- [ ] Rate limiting
- [ ] Audit logging
