# Setup

## Create a `.env` File

Before starting the application, create a `.env` file in the root directory of the project. This file should contain your HotelBeds API credentials. For example:

```makefile
HOTELBEDS_API_KEY=your_api_key
HOTELBEDS_API_SECRET=your_api_secret
```

## Run the Application:

Start your Spring Boot application. Ensure it is running on http://localhost:8080.

API Endpoint
GET /api/hotels/hbparser: This endpoint retrieves hotel data from the HotelBeds API and processes it.