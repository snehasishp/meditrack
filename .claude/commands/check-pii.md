# check-pii — PII Exposure Audit

Scan the codebase for endpoints, DTOs, logs, or comments that expose patient Personally Identifiable Information (PII) without proper masking or authorization.

## What This Checks

### PII Fields
- Email addresses
- Phone numbers
- Full names (when combined with other identifiers)
- Date of birth / age
- Physical addresses
- Medical record numbers

### Where to Look
- DTO response classes — are email, phone, DOB returned in JSON?
- Controller methods — are raw entities or full objects returned?
- Service layer — any `.log()`, `System.out`, or comment revealing PII?
- Test files — mock data with real-looking PII patterns?
- Exception messages — do they include patient identifiers?

## How to Run

Invoke directly in conversation:
```
/check-pii
```

## Output

For each finding, report:
1. **File and line** — clickable path:line
2. **Severity** — HIGH / MEDIUM / LOW
3. **What is exposed** — e.g., "full email returned in PatientResponse"
4. **Recommendation** — e.g., "mask to first 3 chars + '@***.com'"

## Severity Guide

| Level | Criteria |
|-------|----------|
| **HIGH** | Raw PII (full email, full phone, full DOB) in JSON response bodies without masking |
| **MEDIUM** | PII in logs, comments, or exception messages |
| **LOW** | PII in test fixtures (acceptable if clearly synthetic) |

## Rules Applied

This command enforces the following conventions:
- Patient DTOs may return email/phone **only** for authenticated admin access
- All PII returned to clients must be **masked** (e.g., `j***@example.com`, `+1-***-555-0101`)
- DOB should be replaced with **age** or omitted in list responses
- Exception messages must never include patient PII
