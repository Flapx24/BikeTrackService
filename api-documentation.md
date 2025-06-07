# BikeTrack API Documentation

This document provides a comprehensive reference for all endpoints available in the BikeTrack API.

## General Information

All requests to protected endpoints must include an `Authorization` header with a valid JWT token:

```
Authorization: Bearer {token}
```

Most responses follow this structure:

```json
{
  "success": true|false,
  "message": "Message explaining the result",
  "data": {}  // Optional, contains requested data
}
```

## Table of Contents

1. [Authentication](#authentication)
2. [Routes](#routes)
3. [Route Updates](#route-updates)
4. [Route Calculation](#route-calculation)
5. [Reviews](#reviews)
6. [Workshops](#workshops)
7. [Bicycles](#bicycles)
8. [Bicycle Components](#bicycle-components)

## Authentication

### Register

**Endpoint:** `POST /api/auth/register`

**Description:** Register a new user.

**Request Body:**

```json
{
  "username": "myusername",
  "name": "John",
  "surname": "Doe",
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Request Body Fields:**

- **username** (string, required): Unique username for the user.
- **name** (string, required): User's first name.
- **surname** (string, optional): User's last name.
- **email** (string, required): User's email address. Must be a valid email format and unique.
- **password** (string, required): User's password.

_Note: Fields like `imageUrl`, `role`, and `active` are handled automatically by the system and should not be included in the request._

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "id": 123,
  "name": "John",
  "message": "Registro exitoso",
  "imageUrl": "http://example.com/image.jpg"
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El nombre de usuario es obligatorio"
  - "El nombre es obligatorio"
  - "El correo electrónico es obligatorio"
  - "Por favor, introduce un correo electrónico válido"
  - "La contraseña es obligatoria"
  - "Email already exists"
  - "Username already exists"

### Login

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate a user and get a JWT token.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "securepassword",
  "rememberMe": true
}
```

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "id": 123,
  "token": "jwt_token_here",
  "name": "name",
  "message": "Login successful"
}
```

**Error Responses:**

- **Code:** 401 Unauthorized
  - "Correo o contraseña incorrectos. Verifica tus datos."
  - "La cuenta no está activada. Por favor, activa tu cuenta."
  - "Error en la autenticación. Verifica tus credenciales."
- **Code:** 500 Internal Server Error
  - "Ha ocurrido un problema inesperado: {message}"

### Token Login

**Endpoint:** `POST /api/auth/token-login`

**Description:** Validate and refresh a JWT token.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "id": 123,
  "name": "name",
  "message": "Autenticación válida"
}
```

**Error Responses:**

- **Code:** 401 Unauthorized
  - "Token no proporcionado o formato inválido"
  - "Token inválido o expirado"
- **Code:** 500 Internal Server Error
  - "Error al procesar token: {message}"

### Validate Token

**Endpoint:** `POST /api/auth/validate-token`

**Description:** Check if a token is valid.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Token válido"
}
```

**Error Responses:**

- **Code:** 200 OK (with failure indication)
  - `{ "success": false, "message": "Token inválido o expirado" }`
- **Code:** 401 Unauthorized
  - "Token no proporcionado o formato inválido"
- **Code:** 500 Internal Server Error
  - "Error al validar token: {message}"

## Routes

### Get Route by ID

**Endpoint:** `GET /api/routes/{routeId}`

**Description:** Get a route by its ID with all details.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Ruta recuperada con éxito",
  "data": {
    "id": 1,
    "title": "Route name",
    "description": "Route description",
    "difficulty": "EASY",
    "imageUrls": ["url1", "url2"],
    "city": "City name",
    "routePoints": [
      {"lat": 40.416775, "lng": -3.70379}
    ],
    "averageReviewScore": 4.5,
    "reviews": [...],
    "updates": [...],
    "reviewCount": 10,
    "updateCount": 2,
    "calculatedRoutePoints": [...],
    "calculatedEstimatedTimeMinutes": 120,
    "calculatedTotalDistanceKm": 10.5
  }
}
```

_Note: `reviews` field is limited to 15 items in the initial response_

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"

### Get All Routes

**Endpoint:** `GET /api/routes`

**Description:** Get all routes with pagination.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Query Parameters:**

- **lastRouteId:** ID of the last route received (optional, for pagination)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Rutas recuperadas con éxito",
  "data": [
    {
      "id": 2,
      "title": "Route name",
      "description": "Route description",
      "difficulty": "MEDIUM",
      "imageUrls": ["url1"],
      "city": "City name",
      "routePoints": [{ "lat": 40.416775, "lng": -3.70379 }],
      "averageReviewScore": 4.5,
      "reviewCount": 10,
      "updateCount": 1
    }
  ]
}
```

### Filter Routes

**Endpoint:** `GET /api/routes/filter`

**Description:** Get routes filtered by city and/or minimum rating.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Query Parameters:**

- **city:** City name (optional)
- **minScore:** Minimum score (optional, default: 0)
- **lastRouteId:** ID of the last route received (optional, for pagination)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Rutas filtradas recuperadas con éxito",
  "data": [
    {
      "id": 3,
      "title": "Route name",
      "description": "Route description",
      "difficulty": "HARD",
      "imageUrls": [],
      "city": "City name",
      "routePoints": [{ "lat": 40.416775, "lng": -3.70379 }],
      "averageReviewScore": 4.5,
      "reviewCount": 10,
      "updateCount": 0
    }
  ]
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "La puntuación debe estar entre 0 y 5"

## Route Updates

### Create Route Update

**Endpoint:** `POST /api/route-updates`

**Description:** Create a new update for a route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Request Body:**

```json
{
  "routeId": 1,
  "description": "Update description",
  "date": "2023-01-01",
  "type": "CLOSURE",
  "resolved": false
}
```

**Request Body Fields:**

- **routeId** (number, required): ID of the route to update
- **description** (string, required): Description of the update
- **date** (string, required): Date in format "yyyy-MM-dd"
- **type** (string, required): Type of update. Valid values: "INCIDENT", "INFO", "MAINTENANCE", "CLOSURE", "OTHER"
- **resolved** (boolean, optional): Whether the update is resolved (default: false)

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Actualización de ruta creada con éxito",
  "data": {
    "id": 1,
    "description": "Update description",
    "date": "2023-01-01",
    "type": "CLOSURE",
    "resolved": false,
    "routeId": 1,
    "userId": 10
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El ID de la ruta es obligatorio para crear una actualización de ruta"
  - "La descripción es obligatoria"
  - "La fecha es obligatoria"
  - "El tipo de actualización es obligatorio"
- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"

### Update Route Update

**Endpoint:** `PUT /api/route-updates`

**Description:** Update an existing route update.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Request Body:**

```json
{
  "id": 1,
  "description": "Updated description",
  "date": "2023-01-02",
  "type": "MAINTENANCE",
  "resolved": true
}
```

**Request Body Fields:**

- **id** (number, required): ID of the route update to modify
- **description** (string, required): Updated description
- **date** (string, required): Date in format "yyyy-MM-dd"
- **type** (string, required): Type of update. Valid values: "INCIDENT", "INFO", "MAINTENANCE", "CLOSURE", "OTHER"
- **resolved** (boolean, optional): Whether the update is resolved

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Actualización de ruta modificada con éxito",
  "data": {
    "id": 1,
    "description": "Updated description",
    "date": "2023-01-02",
    "type": "MAINTENANCE",
    "resolved": true,
    "routeId": 1,
    "userId": 10
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El ID de la actualización de ruta es obligatorio para la actualización"
  - "La actualización de ruta existente no tiene una ruta asociada"
- **Code:** 404 Not Found
  - "Actualización de ruta no encontrada con ID: {id}"
- **Code:** 403 Forbidden
  - "No tienes permiso para modificar esta actualización de ruta"

### Delete Route Update

**Endpoint:** `DELETE /api/route-updates/{routeUpdateId}`

**Description:** Delete a route update.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeUpdateId:** ID of the route update (required)

**Success Response:**

- **Code:** 204 No Content

**Error Responses:**

- **Code:** 404 Not Found
  - "Actualización de ruta no encontrada con ID: {routeUpdateId}"
- **Code:** 403 Forbidden
  - "No tienes permiso para eliminar esta actualización de ruta"
- **Code:** 500 Internal Server Error
  - "No se pudo eliminar la actualización de ruta"

### Get Route Update

**Endpoint:** `GET /api/route-updates/{routeUpdateId}`

**Description:** Get a specific route update by its ID.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeUpdateId:** ID of the route update (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Actualización de ruta recuperada con éxito",
  "data": {
    "id": 1,
    "description": "Update description",
    "date": "2023-01-01",
    "type": "CLOSURE",
    "resolved": false,
    "routeId": 1,
    "userId": 10
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Actualización de ruta no encontrada con ID: {routeUpdateId}"

### Get Route Updates by Route

**Endpoint:** `GET /api/route-updates/route/{routeId}`

**Description:** Get all updates for a specific route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Actualizaciones de ruta recuperadas con éxito",
  "data": [
    {
      "id": 1,
      "description": "Update description",
      "date": "2023-01-01",
      "type": "CLOSURE",
      "resolved": false,
      "routeId": 1,
      "userId": 10
    }
  ]
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"

## Route Calculation

### Calculate Route

**Endpoint:** `POST /api/route-calculation/calculate`

**Description:** Calculate an optimal route between provided points.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Request Body:**

```json
{
  "points": [
    { "lat": 40.416775, "lng": -3.70379 },
    { "lat": 40.41789, "lng": -3.705675 }
  ],
  "vehicleType": "BICYCLE"
}
```

**Request Body Fields:**

- **points** (array, required): Array of geographical points with lat and lng coordinates. Minimum 2 points, maximum 50 points.
  - **lat** (number, required): Latitude coordinate (-90 to 90)
  - **lng** (number, required): Longitude coordinate (-180 to 180)
- **vehicleType** (string, required): Type of vehicle for route calculation.

**Valid vehicleType values:**

- `"BICYCLE"` (Bicicleta)
- `"CAR"` (Automóvil)
- `"WALKING"` (Caminando)

**Alternative aliases also accepted:**

- For BICYCLE: `"BIKE"`, `"BICICLETA"`, `"CYCLING"`
- For CAR: `"AUTO"`, `"AUTOMOBILE"`, `"COCHE"`, `"AUTOMOVIL"`, `"DRIVING"`
- For WALKING: `"FOOT"`, `"WALK"`, `"CAMINANDO"`, `"PIE"`

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Ruta calculada con éxito",
  "data": {
    "routePoints": [
      { "lat": 40.416775, "lng": -3.70379 },
      { "lat": 40.41699, "lng": -3.704245 },
      { "lat": 40.41789, "lng": -3.705675 }
    ],
    "totalDistanceKm": 0.5,
    "estimatedTimeMinutes": 120,
    "vehicleType": "BICYCLE"
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El cuerpo de la solicitud es obligatorio"
  - "Los puntos de la ruta son obligatorios"
  - "Se requieren al menos 2 puntos para calcular una ruta"
  - "No se pueden calcular rutas con más de 50 puntos"
  - "El tipo de vehículo es obligatorio. Los valores permitidos son: BICYCLE (Bicicleta), CAR (Automóvil), WALKING (Caminando)"
  - "Tipo de vehículo inválido: '{value}'. Los valores permitidos son: BICYCLE (Bicicleta), CAR (Automóvil), WALKING (Caminando)"
  - "El punto {number} es nulo"
  - "El punto {number} tiene coordenadas nulas"
  - "El punto {number} tiene latitud inválida: {value}. Debe estar entre -90 y 90"
  - "El punto {number} tiene longitud inválida: {value}. Debe estar entre -180 y 180"
  - "Error al calcular la ruta: {message}"

## Reviews

### Create Review

**Endpoint:** `POST /api/reviews/route/{routeId}`

**Description:** Create a review for a specific route, date is set automatically.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route to review (required)

**Request Body:**

```json
{
  "text": "Review text content",
  "rating": 4
}
```

**Request Body Fields:**

- **text** (string, optional): Review text content
- **rating** (integer, required): Rating from 1 to 5

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Reseña creada con éxito",
  "data": {
    "id": 1,
    "user": {
      "id": 10,
      "username": "username",
      "imageUrl": "http://example.com/image.jpg"
    },
    "rating": 4,
    "text": "Review text content",
    "date": "2023-01-01",
    "routeId": 5
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"
- **Code:** 409 Conflict
  - "Ya tienes una reseña para esta ruta. Por favor, actualiza tu reseña existente."

### Update Review

**Endpoint:** `PUT /api/reviews/route/{routeId}`

**Description:** Update the current user's review for a specific route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Request Body:**

```json
{
  "text": "Updated review text",
  "rating": 5
}
```

**Request Body Fields:**

- **text** (string, optional): Updated review text content
- **rating** (integer, required): Rating from 1 to 5

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Reseña actualizada con éxito",
  "data": {
    "id": 1,
    "user": {
      "id": 10,
      "username": "username",
      "imageUrl": "http://example.com/image.jpg"
    },
    "rating": 5,
    "text": "Updated review text",
    "date": "2023-01-01",
    "routeId": 5
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"
  - "No tienes una reseña existente para esta ruta"

### Delete Review

**Endpoint:** `DELETE /api/reviews/route/{routeId}`

**Description:** Delete the current user's review for a specific route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Success Response:**

- **Code:** 204 No Content

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"
  - "No tienes una reseña para eliminar en esta ruta"
- **Code:** 500 Internal Server Error
  - "No se pudo eliminar la reseña"

### Get Current User's Review

**Endpoint:** `GET /api/reviews/route/{routeId}/mine`

**Description:** Get the current user's review for a specific route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Reseña recuperada con éxito",
  "data": {
    "id": 1,
    "user": {
      "id": 10,
      "username": "username",
      "imageUrl": "http://example.com/image.jpg"
    },
    "rating": 4,
    "text": "Review text content",
    "date": "2023-01-01",
    "routeId": 5
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"
  - "No tienes ninguna reseña para esta ruta"

### Get Route Reviews

**Endpoint:** `GET /api/reviews/route/{routeId}`

**Description:** Get all reviews for a specific route with pagination. If the authenticated user has a review for this route, it will be returned first, followed by other reviews in descending order by ID.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route (required)

**Query Parameters:**

- **lastReviewId:** ID of the last review received (optional, for pagination)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Reseñas recuperadas con éxito",
  "data": [
    {
      "id": 1,
      "user": {
        "id": 10,
        "username": "currentUser",
        "imageUrl": "http://example.com/image.jpg"
      },
      "rating": 4,
      "text": "My review appears first!",
      "date": "2023-01-01",
      "routeId": 5
    },
    {
      "id": 2,
      "user": {
        "id": 11,
        "username": "user2",
        "imageUrl": "http://example.com/image2.jpg"
      },
      "rating": 5,
      "text": "Great route!",
      "date": "2023-01-02",
      "routeId": 5
    }
  ]
}
```

_Note: The authenticated user's review (if it exists) will always appear first in the results, regardless of its creation date. Other reviews follow in descending order by ID._

**Error Responses:**

- **Code:** 404 Not Found
  - "Ruta no encontrada con ID: {routeId}"
- **Code:** 500 Internal Server Error
  - "Error al obtener las reseñas: {message}"

## Workshops

### Get Workshop by ID

**Endpoint:** `GET /api/workshops/{workshopId}`

**Description:** Get details of a specific workshop.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **workshopId:** ID of the workshop (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Taller recuperado con éxito",
  "data": {
    "id": 1,
    "name": "Workshop Name",
    "city": "City name",
    "imageUrls": [
      "https://example.com/images/workshop1_1.jpg",
      "https://example.com/images/workshop1_2.jpg"
    ],
    "address": "Workshop address",
    "coordinates": {
      "lat": 40.416775,
      "lng": -3.70379
    }
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Taller no encontrado con ID: {workshopId}"

### Get Workshops by City

**Endpoint:** `GET /api/workshops/city`

**Description:** Get all workshops in a specific city.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Query Parameters:**

- **city:** Name of the city (required, case insensitive)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Talleres recuperados con éxito",
  "data": [
    {
      "id": 1,
      "name": "Workshop Name",
      "city": "City name",
      "imageUrls": [
        "https://example.com/images/workshop1_1.jpg",
        "https://example.com/images/workshop1_2.jpg"
      ],
      "address": "Workshop address",
      "coordinates": {
        "lat": 40.416775,
        "lng": -3.70379
      }
    }
  ]
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El nombre de la ciudad es obligatorio"
- **Code:** 404 Not Found
  - "No se encontraron talleres en la ciudad: {city}"

## Bicycles

### Create Bicycle

**Endpoint:** `POST /api/bicycles`

**Description:** Create a new bicycle for the authenticated user.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Request Body:**

```json
{
  "name": "My Bicycle",
  "totalKilometers": 0,
  "lastMaintenanceDate": "2023-01-01"
}
```

**Request Body Fields:**

- **name** (string, required): Name of the bicycle
- **iconUrl** (string, optional): URL for the bicycle icon/image
- **totalKilometers** (number, optional): Total kilometers (default: 0)
- **lastMaintenanceDate** (string, optional): Last maintenance date in "yyyy-MM-dd" format
- **components** (array, optional): Array of component DTOs

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Bicicleta creada con éxito",
  "data": {
    "id": 1,
    "name": "My Bicycle",
    "iconUrl": "http://example.com/bike.jpg",
    "ownerId": 10,
    "totalKilometers": 0,
    "lastMaintenanceDate": "2023-01-01",
    "componentCount": 0,
    "components": []
  }
}
```

### Update Bicycle

**Endpoint:** `PUT /api/bicycles/{bicycleId}`

**Description:** Update an existing bicycle.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle to update (required)

**Request Body:**

```json
{
  "name": "Updated Bicycle Name",
  "totalKilometers": 100,
  "lastMaintenanceDate": "2023-01-01",
  "components": []
}
```

**Request Body Fields:**

- **name** (string, required): Updated name of the bicycle
- **iconUrl** (string, optional): Updated icon URL
- **totalKilometers** (number, optional): Updated total kilometers
- **lastMaintenanceDate** (string, optional): Updated maintenance date in "yyyy-MM-dd" format
- **components** (array, optional): Array of component DTOs

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Bicicleta actualizada con éxito",
  "data": {
    "id": 1,
    "name": "Updated Bicycle Name",
    "iconUrl": "http://example.com/new-bike.jpg",
    "ownerId": 10,
    "totalKilometers": 100,
    "lastMaintenanceDate": "2023-01-01",
    "componentCount": 0,
    "components": []
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para modificar esta bicicleta"

### Delete Bicycle

**Endpoint:** `DELETE /api/bicycles/{bicycleId}`

**Description:** Delete a bicycle.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle to delete (required)

**Success Response:**

- **Code:** 204 No Content

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para eliminar esta bicicleta"

### Get Bicycle by ID

**Endpoint:** `GET /api/bicycles/{bicycleId}`

**Description:** Get a bicycle by its ID.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle to retrieve (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Bicicleta recuperada con éxito",
  "data": {
    "id": 1,
    "name": "My Bicycle",
    "iconUrl": "http://example.com/bike.jpg",
    "ownerId": 10,
    "totalKilometers": 100,
    "lastMaintenanceDate": "2023-01-01",
    "componentCount": 2,
    "components": [
      {
        "id": 1,
        "name": "Component name",
        "maxKilometers": 1000,
        "currentKilometers": 100
      }
    ]
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para ver esta bicicleta"

### Get All User Bicycles

**Endpoint:** `GET /api/bicycles`

**Description:** Get all bicycles of the authenticated user (summary view without components).

**Headers:**

- **Authorization:** Bearer {token} (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Bicicletas recuperadas con éxito",
  "data": [
    {
      "id": 1,
      "name": "My Bicycle 1",
      "iconUrl": "http://example.com/bike1.jpg",
      "ownerId": 10,
      "totalKilometers": 100,
      "lastMaintenanceDate": "2023-01-01",
      "needsMaintenance": "false",
      "componentCount": 2
    }
  ]
}
```

### Add Kilometers to Bicycle

**Endpoint:** `POST /api/bicycles/{bicycleId}/add-kilometers`

**Description:** Add kilometers to a bicycle and its components.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle (required)

**Query Parameters:**

- **kilometers:** Kilometers to add (required, must be positive)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Kilómetros añadidos con éxito",
  "data": {
    "id": 1,
    "name": "My Bicycle",
    "iconUrl": "http://example.com/bike.jpg",
    "ownerId": 10,
    "totalKilometers": 150,
    "lastMaintenanceDate": "2023-01-01",
    "componentCount": 2,
    "components": [...]
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "Los kilómetros deben ser un valor positivo"
- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para modificar esta bicicleta"

### Subtract Kilometers from Bicycle

**Endpoint:** `POST /api/bicycles/{bicycleId}/subtract-kilometers`

**Description:** Subtract kilometers from a bicycle and its components.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle (required)

**Query Parameters:**

- **kilometers:** Kilometers to subtract (required, must be positive)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Kilómetros restados con éxito",
  "data": {
    "id": 1,
    "name": "My Bicycle",
    "iconUrl": "http://example.com/bike.jpg",
    "ownerId": 10,
    "totalKilometers": 50,
    "lastMaintenanceDate": "2023-01-01",
    "componentCount": 2,
    "components": [...]
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "Los kilómetros deben ser un valor positivo"
- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para modificar esta bicicleta"

## Bicycle Components

### Create Component

**Endpoint:** `POST /api/components/bicycle/{bicycleId}`

**Description:** Create a new component for a bicycle.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle to add the component to (required)

**Request Body:**

```json
{
  "name": "Component Name",
  "maxKilometers": 1000,
  "currentKilometers": 0
}
```

**Request Body Fields:**

- **name** (string, required): Name of the component
- **maxKilometers** (number, required): Maximum kilometers before maintenance is needed (must be positive)
- **currentKilometers** (number, required): Current kilometers on the component

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Componente creado con éxito",
  "data": {
    "id": 1,
    "name": "Component Name",
    "maxKilometers": 1000,
    "currentKilometers": 0
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para añadir componentes a esta bicicleta"

### Update Component

**Endpoint:** `PUT /api/components/{componentId}`

**Description:** Update an existing component.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **componentId:** ID of the component to update (required)

**Request Body:**

```json
{
  "name": "Updated Component Name",
  "maxKilometers": 1200,
  "currentKilometers": 100
}
```

**Request Body Fields:**

- **name** (string, required): Updated name of the component
- **maxKilometers** (number, required): Updated maximum kilometers (must be positive)
- **currentKilometers** (number, required): Updated current kilometers

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Componente actualizado con éxito",
  "data": {
    "id": 1,
    "name": "Updated Component Name",
    "maxKilometers": 1200,
    "currentKilometers": 100
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Componente no encontrado con ID: {componentId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para modificar este componente"

### Delete Component

**Endpoint:** `DELETE /api/components/{componentId}`

**Description:** Delete a component.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **componentId:** ID of the component to delete (required)

**Success Response:**

- **Code:** 204 No Content

**Error Responses:**

- **Code:** 404 Not Found
  - "Componente no encontrado con ID: {componentId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para eliminar este componente"
- **Code:** 500 Internal Server Error
  - "No se pudo eliminar el componente"

### Get Component by ID

**Endpoint:** `GET /api/components/{componentId}`

**Description:** Get a component by its ID.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **componentId:** ID of the component to retrieve (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Componente recuperado con éxito",
  "data": {
    "id": 1,
    "name": "Component Name",
    "maxKilometers": 1000,
    "currentKilometers": 100
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Componente no encontrado con ID: {componentId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para acceder a este componente"

### Get All Components for a Bicycle

**Endpoint:** `GET /api/components/bicycle/{bicycleId}`

**Description:** Get all components for a specific bicycle.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle to get components for (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Componentes recuperados con éxito",
  "data": [
    {
      "id": 1,
      "name": "Component 1",
      "maxKilometers": 1000,
      "currentKilometers": 100
    }
  ]
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para ver los componentes de esta bicicleta"

### Reset Component Kilometers

**Endpoint:** `POST /api/components/bicycle/{bicycleId}/reset`

**Description:** Reset the current kilometers for all components of a bicycle.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **bicycleId:** ID of the bicycle (required)

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Kilómetros de los componentes reiniciados con éxito"
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para reiniciar los componentes de esta bicicleta"
- **Code:** 500 Internal Server Error
  - "No se pudieron reiniciar los kilómetros de los componentes"
