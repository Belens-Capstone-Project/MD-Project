# BELENS MOBILE DEVELOPMENT

## Belens App

Belens App is a mobile application designed to provide users with insights into their nutritional habits using advanced image recognition technology. It helps users make informed decisions about their drink choices by analyzing images of beverages and offering nutritional information.

## Features

- **Welcoming Activity**: Welcomes users with an attractive interface that makes it easy for them to start the app experience.
- **Login & Register**: Login system for registered users and registration option for new users. Can be connected to social media accounts or email.
- **Bottom Navigation**: Bottom navigation that provides quick access to the app's main features, such as Home, History, News, Profile, and Settings.
- **News Feature**: Presents the latest health news, drink trends, and relevant nutrition information.
- **History Feature**: Keeps a history of scanning and analyzing drinks that have been selected by the user.
- **Search**: Sorting news according to the user's wishes
- **Image-Based Drink Recognition**: Upload images of beverages to receive predictions about drink types and their nutritional values.
- **Nutritional Analysis**: Detailed nutritional breakdown, including calorie counts, and other essential dietary information.
- **User-Friendly Interface**: Intuitive design for seamless navigation and usability.

## Prerequisites

To set up and run the application, ensure the following are installed on your local development environment:

- Android Studio
- Android Device or Android Emulator with minimum Lollipop Version

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Belens-Capstone-Project/MD-Project
   cd belens-app
   ```

2. **Open the project in Android Studio**

3. **Run the Application**
   - Open the project in Android Studio
   - Build and run the application on an emulator or connected device

## How It Works

1. **Capture or Upload an Image**
   - Users can capture a photo of their drink using the camera or upload an existing image from their gallery.

2. **Prediction and Analysis**
   - The app sends the image to the /predict endpoint of the API. The API responds with:
     - Predicted drink label
     - Nutritional information
     - Beverage Grade
     - Recommendation

3. **Display Results**
   - The app displays the results in a user-friendly format, allowing users to save the entry to their drink log.

4. **History**
   - Users can view historical scanned beverage data.

## Deployment

1. **Build Release Version**
   - Generate a signed APK in Android Studio
   - Follow the instructions to create a release build and sign it with a keystore

## Tools and Technologies

- **Frontend**: Kotlin for Android development
- **Backend**: Flask API hosted on Google Cloud Run
- **Database**: Firebase Realtime Data Store for storing user data and profile images
- **Storage**: Google Cloud Storage for uploaded images
- **Testing**: Postman for API testing and Android Studio's built-in testing tools

## Contact

For questions or assistance, please reach out via:
- **GitHub Repository**: [Belens](https://github.com/Belens-Capstone-Project/MD-Project)
- **Email**: appbelens@gmail.com
