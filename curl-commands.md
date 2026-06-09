# Curl Commands for Testing Endpoints

This file contains curl commands to test all endpoints in the Posthere application.

> [!NOTE]
> Default Spring Boot port is **8080**. If you've configured a different port, replace `8080` with your configured port in all commands below.

---

## 1. Authentication Endpoints

### POST /api/auth/generate-token
Generate a JWT token for authenticated operations.

```bash
curl -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "message": "Token generated successfully"
}
```

**Save the token for use in authenticated endpoints:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | jq -r '.token')
echo $TOKEN
```

---

### POST /api/auth/validate-token
Validate an existing JWT token.

```bash
curl -X POST http://localhost:8080/api/auth/validate-token \
  -H "Content-Type: application/json" \
  -d '{"token":"<your-jwt-token>"}'
```

**Expected Response:**
```json
{
  "valid": true,
  "username": "user@example.com",
  "message": "Token is valid"
}
```

---

## 2. File Management Endpoints

### POST /api/files/upload
Upload a ZIP file to S3 bucket (Requires Authentication).

**Requirements:**
- File size: Maximum 5MB
- File type: ZIP only
- Bearer token required

```bash
# Step 1: Generate token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | jq -r '.token')

# Step 2: Upload ZIP file
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/archive.zip"
```

**Expected Response:**
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

---

### GET /api/files/details/{fileId}
Fetch file details and status (No Authentication Required).

```bash
curl -X GET http://localhost:8080/api/files/details/1
```

**Expected Response:**
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
- uploadStatus: `PENDING`, `UPLOADED`, `FAILED`
- destinationStatus: `NOT_SENT`, `SENT`, `FAILED`

---

### POST /api/files/extract-and-email
Extract ZIP file from S3 and send via email (Requires Authentication).

**Requirements:**
- Bearer token required
- File must exist in S3
- Valid recipient email required

```bash
# Step 1: Generate token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com"}' | jq -r '.token')

# Step 2: Extract and email
curl -X POST http://localhost:8080/api/files/extract-and-email \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileKey": "uploads/12345-67890/archive.zip",
    "recipientEmail": "arshdeepdubey.ad@gmail.com",
    "subject": "Your Extracted Files",
    "message": "Please find your extracted files attached."
  }'
```

**Expected Response:**
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

---

## 3. Legacy Endpoints (Details Management)

### GET /api/details
Get all details from the database.

```bash
curl -X GET http://localhost:8080/api/details
```

**Expected Response:**
```json
[
  {
    "fname": "John",
    "lname": "Doe",
    "city": "New York"
  }
]
```

---

### GET /api/details/{id}
Get a specific detail by ID.

```bash
curl -X GET http://localhost:8080/api/details/1
```

**Expected Response:**
```json
{
  "fname": "John",
  "lname": "Doe",
  "city": "New York"
}
```

---

### POST /api/details
Create a new detail entry.

**Windows Command Prompt:**
```bash
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d "{\"fname\":\"Alice\",\"lname\":\"Smith\",\"city\":\"London\"}"
```

**PowerShell:**
```powershell
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d '{\"fname\":\"Alice\",\"lname\":\"Smith\",\"city\":\"London\"}'
```

**Git Bash / WSL:**
```bash
curl -X POST http://localhost:8080/api/details \
  -H "Content-Type: application/json" \
  -d '{
    "fname": "Alice",
    "lname": "Smith",
    "city": "London"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "fname": "Alice",
  "lname": "Smith",
  "city": "London"
}
```

---

## Quick Test Script

### PowerShell Script
Save as `test-endpoints.ps1`:

```powershell
Write-Host "Testing Hello Endpoint..." -ForegroundColor Green
curl http://localhost:8080/hello
Write-Host "`n"

Write-Host "Testing GET All Details..." -ForegroundColor Green
curl http://localhost:8080/api/details
Write-Host "`n"

Write-Host "Testing POST Create Detail..." -ForegroundColor Green
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d '{\"fname\":\"Test\",\"lname\":\"User\",\"city\":\"TestCity\"}'
Write-Host "`n"

Write-Host "Testing GET Detail by ID..." -ForegroundColor Green
curl http://localhost:8080/api/details/1
```

### Bash Script
Save as `test-endpoints.sh`:

```bash
#!/bin/bash

echo "Testing Hello Endpoint..."
curl http://localhost:8080/hello
echo -e "\n"

echo "Testing GET All Details..."
curl http://localhost:8080/api/details
echo -e "\n"

echo "Testing POST Create Detail..."
curl -X POST http://localhost:8080/api/details \
  -H "Content-Type: application/json" \
  -d '{
    "fname": "Test",
    "lname": "User",
    "city": "TestCity"
  }'
echo -e "\n"

echo "Testing GET Detail by ID..."
curl http://localhost:8080/api/details/1
```

---

## Tips

1. **Check Application Status:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   *(Only if Spring Actuator is enabled)*

2. **Pretty Print JSON Response:**
   ```bash
   curl http://localhost:8080/api/details | python -m json.tool
   ```

3. **Save Response to File:**
   ```bash
   curl http://localhost:8080/api/details -o response.json
   ```

4. **View Response Headers:**
   ```bash
   curl -i http://localhost:8080/api/details
   ```

5. **Verbose Mode (for debugging):**
   ```bash
   curl -v http://localhost:8080/api/details
   ```
