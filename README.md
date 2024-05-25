# Map Search App

This project is a map application developed using Kotlin. The application finds the user's current location and allows searching for specific locations via a search bar. The search results are retrieved using the Google Places API, and the user can see the selected location on the map.

## Features

- **Location Permission**: The application requests location permission when it starts.
- **Current Location**: If permission is granted, the user's current location is automatically found and displayed on the map.
- **Search Bar**: A search bar allows users to search for desired locations.
- **Places API**: Relevant places are retrieved using the Google Places API based on the search bar input.
- **Selected Location**: When a user selects a location from the search results, the map camera moves to that location, and the selected location name is displayed on the screen.

## Installation and Usage

### Requirements

- Android Studio
- Google Maps API key
- Google Places API must be enabled

(Developed in `Android Studio Hedgehog (version 2023.1.1 Patch 2`)

### Steps

1. **Clone the Project:**

    ```sh
    git clone https://github.com/mozandastan/android-map-search-app.git
    ```

2. **Add the API Key:**

    - Go to your `local.properties` file in the project and add your Google Maps API key:
    ```properties
    MAPS_API_KEY=YOUR_API_KEY_HERE
    ```

3. **Enable Google Places API:**

    - Go to the Google Cloud Console and enable the Google Places API.
    - Configure the Google Places API using the same API key.

4. **Run the Project:**

    - Open Android Studio and load the project.
    - Run the project to start the application on your device or emulator.

### Usage

- When you start the application, it will request location permission. Grant the permission.
- Your current location will be displayed on the map.
- Enter a location name in the search bar and search.
- Relevant locations will be listed. Select a location.
- The map will focus on the selected location, and the location name will be displayed on the screen.

## Screenshots

![ss3](https://github.com/mozandastan/android-map-search-app/assets/151640771/c72964c9-524b-44c5-8d0d-ecb2fa40de13)
![ss1](https://github.com/mozandastan/android-map-search-app/assets/151640771/bcf38245-77cd-4c52-82db-c9d180b57acd) 
![ss2](https://github.com/mozandastan/android-map-search-app/assets/151640771/fb5dc1de-78a3-46cc-8256-1d7a8219248a) 


## Contact

If you have any questions or feedback, please contact me at [m.ozandastan@gmail.com](mailto:m.ozandastan@gmail.com).
