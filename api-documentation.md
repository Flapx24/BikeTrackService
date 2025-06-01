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
  "message": "User registered successfully"
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
    "name": "Route name",
    "description": "Route description",
    "city": "City name",
    "distance": 10.5,
    "averageScore": 4.5,
    "reviewCount": 10,
    "creationDate": "2023-01-01T12:00:00",
    "points": [...],
    "reviews": [...],
    "updates": [...]
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
      "name": "Route name",
      "description": "Route description",
      "city": "City name",
      "distance": 10.5,
      "averageScore": 4.5,
      "reviewCount": 10,
      "creationDate": "2023-01-01T12:00:00"
    }
    // more routes...
  ]
}
```

### Filter Routes

**Endpoint:** `GET /api/routes/filter`

**Description:** Get routes filtered by city and minimum rating.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Query Parameters:**

- **city:** City name (required)
- **minScore:** Minimum score (optional, default: 1)
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
      "name": "Route name",
      "description": "Route description",
      "city": "City name",
      "distance": 10.5,
      "averageScore": 4.5,
      "reviewCount": 10,
      "creationDate": "2023-01-01T12:00:00"
    }
    // more routes...
  ]
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "La puntuación debe estar entre 1 y 5"
  - "El nombre de la ciudad es obligatorio"

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
  "title": "Update title",
  "description": "Update description",
  "updateType": "CLOSURE",
  "startDate": "2023-01-01",
  "endDate": "2023-01-15"
}
```

_Note: `endDate` can be null for indefinite updates_

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Actualización de ruta creada con éxito",
  "data": {
    "id": 1,
    "routeId": 1,
    "title": "Update title",
    "description": "Update description",
    "updateType": "CLOSURE",
    "startDate": "2023-01-01",
    "endDate": "2023-01-15",
    "createdAt": "2023-01-01T12:00:00"
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "El ID de la ruta es obligatorio para crear una actualización de ruta"
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
  "routeId": 1,
  "title": "Updated title",
  "description": "Updated description",
  "updateType": "CLOSURE",
  "startDate": "2023-01-01",
  "endDate": "2023-01-20"
}
```

_Note: `endDate` can be null for indefinite updates_

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Actualización de ruta modificada con éxito",
  "data": {
    "id": 1,
    "routeId": 1,
    "title": "Updated title",
    "description": "Updated description",
    "updateType": "CLOSURE",
    "startDate": "2023-01-01",
    "endDate": "2023-01-20",
    "createdAt": "2023-01-01T12:00:00"
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
    "routeId": 1,
    "title": "Update title",
    "description": "Update description",
    "updateType": "CLOSURE",
    "startDate": "2023-01-01",
    "endDate": "2023-01-15",
    "createdAt": "2023-01-01T12:00:00"
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
      "routeId": 1,
      "title": "Update title",
      "description": "Update description",
      "updateType": "CLOSURE",
      "startDate": "2023-01-01",
      "endDate": "2023-01-15",
      "createdAt": "2023-01-01T12:00:00"
    }
    // more updates...
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
    { "latitude": 40.416775, "longitude": -3.70379 },
    { "latitude": 40.41789, "longitude": -3.705675 }
  ],
  "vehicleType": "BICYCLE"
}
```

_Note: `vehicleType` is optional and defaults to "BICYCLE"_

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Ruta calculada con éxito",
  "data": {
    "success": true,
    "message": "Route calculated successfully",
    "points": [
      { "latitude": 40.416775, "longitude": -3.70379 },
      { "latitude": 40.41699, "longitude": -3.704245 },
      { "latitude": 40.41789, "longitude": -3.705675 }
    ],
    "distance": 0.5,
    "time": 120
  }
}
```

**Error Responses:**

- **Code:** 400 Bad Request
  - "Se requieren al menos 2 puntos para calcular una ruta"
  - "No se pueden calcular rutas con más de 50 puntos"
  - "Error al calcular la ruta: {message}"

## Reviews

### Create Review

**Endpoint:** `POST /api/reviews/route/{routeId}`

**Description:** Create a review for a specific route.

**Headers:**

- **Authorization:** Bearer {token} (required)

**Path Parameters:**

- **routeId:** ID of the route to review (required)

**Request Body:**

```json
{
  "text": "Review text content",
  "rating": 4.5
}
```

**Success Response:**

- **Code:** 201 Created
- **Content:**

```json
{
  "success": true,
  "message": "Reseña creada con éxito",
  "data": {
    "id": 1,
    "userId": 10,
    "routeId": 5,
    "userName": "username",
    "text": "Review text content",
    "rating": 4.5,
    "createdAt": "2023-01-01T12:00:00"
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
  "rating": 5.0
}
```

**Success Response:**

- **Code:** 200 OK
- **Content:**

```json
{
  "success": true,
  "message": "Reseña actualizada con éxito",
  "data": {
    "id": 1,
    "userId": 10,
    "routeId": 5,
    "userName": "username",
    "text": "Updated review text",
    "rating": 5.0,
    "createdAt": "2023-01-01T12:00:00"
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
    "userId": 10,
    "routeId": 5,
    "userName": "username",
    "text": "Review text content",
    "rating": 4.5,
    "createdAt": "2023-01-01T12:00:00"
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
      "userId": 10,
      "routeId": 5,
      "userName": "currentUser",
      "text": "My review appears first!",
      "rating": 4.5,
      "createdAt": "2023-01-01T12:00:00"
    },
    {
      "id": 2,
      "userId": 11,
      "routeId": 5,
      "userName": "user2",
      "text": "Great route!",
      "rating": 5.0,
      "createdAt": "2023-01-02T12:00:00"
    }
    // more reviews...
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
    "address": "Workshop address",
    "coordinates": {
      "lat": 40.416775,
      "lng": -3.70379
    },
    "imageUrls": [
      "https://example.com/images/workshop1_1.jpg",
      "https://example.com/images/workshop1_2.jpg"
    ]
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
      "address": "Workshop address",
      "coordinates": {
        "lat": 40.416775,
        "lng": -3.70379
      },
      "imageUrls": [
        "https://example.com/images/workshop1_1.jpg",
        "https://example.com/images/workshop1_2.jpg"
      ]
    }
    // more workshops...
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
  "brand": "Brand Name",
  "model": "Model Name",
  "bicycleType": "ROAD",
  "color": "Red",
  "purchaseDate": "2023-01-01",
  "totalKilometers": 0,
  "components": []
}
```

_Note: `components` and `purchaseDate` can be null_

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
    "brand": "Brand Name",
    "model": "Model Name",
    "bicycleType": "ROAD",
    "color": "Red",
    "purchaseDate": "2023-01-01",
    "totalKilometers": 0,
    "ownerId": 10,
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
  "brand": "Updated Brand",
  "model": "Updated Model",
  "bicycleType": "ROAD",
  "color": "Blue",
  "purchaseDate": "2023-01-01",
  "totalKilometers": 100,
  "components": [1, 2, 3]
}
```

_Note: `components` is an array of component IDs, `purchaseDate` can be null_

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
    "brand": "Updated Brand",
    "model": "Updated Model",
    "bicycleType": "ROAD",
    "color": "Blue",
    "purchaseDate": "2023-01-01",
    "totalKilometers": 100,
    "ownerId": 10,
    "components": [1, 2, 3]
  }
}
```

**Error Responses:**

- **Code:** 404 Not Found
  - "Bicicleta no encontrada con ID: {bicycleId}"
- **Code:** 401 Unauthorized
  - "No tienes permiso para modificar esta bicicleta"
- **Code:** 400 Bad Request
  - "Los siguientes componentes no existen: [...]"

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
    "brand": "Brand Name",
    "model": "Model Name",
    "bicycleType": "ROAD",
    "color": "Red",
    "purchaseDate": "2023-01-01",
    "totalKilometers": 100,
    "ownerId": 10,
    "components": [
      {
        "id": 1,
        "name": "Component name",
        "type": "CHAIN",
        "brand": "Component brand",
        "model": "Component model",
        "purchaseDate": "2023-01-01",
        "installationDate": "2023-01-02",
        "currentKilometers": 100,
        "totalKilometers": 100,
        "recommendedKilometers": 1000
      }
      // more components...
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
      "brand": "Brand Name",
      "model": "Model Name",
      "bicycleType": "ROAD",
      "color": "Red",
      "purchaseDate": "2023-01-01",
      "totalKilometers": 100,
      "ownerId": 10
    }
    // more bicycles...
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
    "brand": "Brand Name",
    "model": "Model Name",
    "bicycleType": "ROAD",
    "color": "Red",
    "purchaseDate": "2023-01-01",
    "totalKilometers": 150,
    "ownerId": 10,
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
    "brand": "Brand Name",
    "model": "Model Name",
    "bicycleType": "ROAD",
    "color": "Red",
    "purchaseDate": "2023-01-01",
    "totalKilometers": 50,
    "ownerId": 10,
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
  "type": "CHAIN",
  "brand": "Component Brand",
  "model": "Component Model",
  "purchaseDate": "2023-01-01",
  "installationDate": "2023-01-02",
  "currentKilometers": 0,
  "totalKilometers": 0,
  "recommendedKilometers": 1000
}
```

_Note: `purchaseDate`, `installationDate` can be null_

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
    "type": "CHAIN",
    "brand": "Component Brand",
    "model": "Component Model",
    "purchaseDate": "2023-01-01",
    "installationDate": "2023-01-02",
    "currentKilometers": 0,
    "totalKilometers": 0,
    "recommendedKilometers": 1000,
    "bicycleId": 1
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
  "type": "CHAIN",
  "brand": "Updated Brand",
  "model": "Updated Model",
  "purchaseDate": "2023-01-01",
  "installationDate": "2023-01-02",
  "currentKilometers": 100,
  "totalKilometers": 100,
  "recommendedKilometers": 1200
}
```

_Note: `purchaseDate`, `installationDate` can be null_

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
    "type": "CHAIN",
    "brand": "Updated Brand",
    "model": "Updated Model",
    "purchaseDate": "2023-01-01",
    "installationDate": "2023-01-02",
    "currentKilometers": 100,
    "totalKilometers": 100,
    "recommendedKilometers": 1200,
    "bicycleId": 1
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
    "type": "CHAIN",
    "brand": "Component Brand",
    "model": "Component Model",
    "purchaseDate": "2023-01-01",
    "installationDate": "2023-01-02",
    "currentKilometers": 100,
    "totalKilometers": 100,
    "recommendedKilometers": 1000,
    "bicycleId": 1
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
      "type": "CHAIN",
      "brand": "Brand 1",
      "model": "Model 1",
      "purchaseDate": "2023-01-01",
      "installationDate": "2023-01-02",
      "currentKilometers": 100,
      "totalKilometers": 100,
      "recommendedKilometers": 1000,
      "bicycleId": 1
    }
    // more components...
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
