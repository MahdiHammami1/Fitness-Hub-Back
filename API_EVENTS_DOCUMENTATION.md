# API Endpoints Documentation - Events

## Public Events API (`/api/v1/events`)

### Get Upcoming Events
- **Endpoint**: `GET /api/v1/events/upcoming`
- **Authentication**: No
- **Query Parameters**: 
  - `page` (default: 0)
  - `size` (default: 12)
- **Response**: Paginated list of upcoming published events

### Get Past Events
- **Endpoint**: `GET /api/v1/events/past`
- **Authentication**: No
- **Query Parameters**:
  - `page` (default: 0)
  - `size` (default: 12)
- **Response**: Paginated list of past published events

### Get Event Details
- **Endpoint**: `GET /api/v1/events/{id}`
- **Authentication**: No
- **Path Parameters**: 
  - `id`: Event ID
- **Response**: Event object

### Download Event as ICS (Calendar File)
- **Endpoint**: `GET /api/v1/events/{id}/ics`
- **Authentication**: No
- **Path Parameters**: 
  - `id`: Event ID
- **Response**: ICS file (text/calendar)
- **Usage**: Users can add event to their calendar application

### Register for Event
- **Endpoint**: `POST /api/v1/events/{id}/registrations`
- **Authentication**: No
- **Path Parameters**: 
  - `id`: Event ID
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "emergencyContact": "Jane Doe",
  "acceptedTerms": true
}
```
- **Response**: `{ "message": "Registered" }`
- **Validation**:
  - Email must be valid
  - User cannot register twice for same event
  - Event must not be full

---

## Admin Events API (`/api/v1/admin/events`)

**All endpoints require**: `ADMIN` role

### Create Event
- **Endpoint**: `POST /api/v1/admin/events`
- **Authentication**: Admin only
- **Request Body**:
```json
{
  "title": "Event Title",
  "description": "Event Description",
  "startAt": "2025-12-20T10:00:00Z",
  "endAt": "2025-12-20T12:00:00Z",
  "location": "Paris, France",
  "capacity": 100,
  "isFree": false,
  "price": 29.99,
  "coverImageUrl": "https://example.com/image.jpg"
}
```
- **Response**: Created Event object (status: DRAFT)

### Update Event
- **Endpoint**: `PUT /api/v1/admin/events/{id}`
- **Authentication**: Admin only
- **Path Parameters**: 
  - `id`: Event ID
- **Request Body**: Same as Create Event
- **Response**: Updated Event object

### Publish Event
- **Endpoint**: `PATCH /api/v1/admin/events/{id}/publish`
- **Authentication**: Admin only
- **Path Parameters**: 
  - `id`: Event ID
- **Response**: Event object with status: PUBLISHED

### Cancel Event
- **Endpoint**: `PATCH /api/v1/admin/events/{id}/cancel`
- **Authentication**: Admin only
- **Path Parameters**: 
  - `id`: Event ID
- **Response**: Event object with status: CANCELLED

### Get Event Registrations
- **Endpoint**: `GET /api/v1/admin/events/{id}/registrations`
- **Authentication**: Admin only
- **Path Parameters**: 
  - `id`: Event ID
- **Query Parameters**:
  - `page` (default: 0)
  - `size` (default: 50)
- **Response**: Paginated list of EventRegistration objects

### Export Registrations as CSV
- **Endpoint**: `GET /api/v1/admin/events/{id}/registrations/export`
- **Authentication**: Admin only
- **Path Parameters**: 
  - `id`: Event ID
- **Response**: CSV file with columns: name, email, phone, emergencyContact, createdAt
- **Headers**: Content-Type: text/csv

---

## Data Models

### Event
```json
{
  "id": "event_id",
  "title": "Event Title",
  "description": "Event Description",
  "startAt": "2025-12-20T10:00:00Z",
  "endAt": "2025-12-20T12:00:00Z",
  "location": "Paris, France",
  "capacity": 100,
  "registrationsCount": 45,
  "isFree": false,
  "price": 29.99,
  "status": "PUBLISHED",
  "coverImageUrl": "https://example.com/image.jpg",
  "createdAt": "2025-12-01T08:00:00Z",
  "updatedAt": "2025-12-01T08:00:00Z"
}
```

### EventRegistration
```json
{
  "id": "registration_id",
  "eventId": "event_id",
  "userId": "user_id (optional)",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "emergencyContact": "Jane Doe",
  "acceptedTerms": true,
  "createdAt": "2025-12-10T14:30:00Z"
}
```

### EventStatus
- `DRAFT` - Event created but not yet published
- `PUBLISHED` - Event is visible and open for registrations
- `CANCELLED` - Event is cancelled
- `COMPLETED` - Event has finished

---

## Features

✅ **Public Features**:
- Browse upcoming and past events
- View event details
- Download event as ICS calendar file
- Register for events
- Validation of duplicate registrations
- Capacity checking

✅ **Admin Features**:
- Create/Edit events
- Publish/Cancel events
- View all registrations for an event
- Export registrations as CSV
- Role-based access control

