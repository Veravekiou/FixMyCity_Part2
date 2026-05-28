# FixMyCity

Android mobile application for reporting local city issues such as potholes, broken streetlights, garbage, sidewalk damage, and vandalism.

## Coursework Context

This repository contains the **CWRK2 / Part 2 final implementation** of the FixMyCity project.

- Module: `CN6008 - Advanced Topics in Computer Science`
- Project title: `FixMyCity`
- Submission stage: `Coursework 2 - final implementation`

## Project Idea

FixMyCity is a citizen-reporting application that allows users to:

- create a city issue report
- choose an issue category
- choose the report location on a map
- optionally attach an image from the device
- submit the report to a cloud database
- view submitted reports

The aim of the app is to provide a simple mobile workflow for reporting urban problems to a central system.

## Current Features

- Main screen with navigation to report submission and reports list
- Report submission form with validation
- Category selection for common city problems
- Map-based location selection
- Optional image selection from device storage
- Firebase Firestore integration for saving report data
- Firebase Storage support prepared in the data layer
- Basic report listing structure

## Technologies Used

- Java
- Android Studio
- Android SDK
- Firebase Firestore
- Firebase Storage
- RecyclerView
- XML layouts

## Project Structure

Key areas of the codebase:

- `ui/` activities and adapter classes
- `data/` Firebase manager and repository layer
- `model/` report data model
- `utils/` constants

## Setup Notes

To run the project locally:

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Build and run on an emulator or Android device.

## Important Repository Note

The Firebase configuration file `app/google-services.json` is included so the submitted project can be built and tested for assessment.

## Status and Next Step

This repository represents the **Part 2 final implementation** of FixMyCity.

Possible future improvements include:

- improved UI and navigation
- stronger separation of UI and business logic
- additional advanced device or cloud features
- testing strategy and test cases
- evaluation and group contribution tracking

## Author

- `Varvara Vekiou`

