# Journal App

This is an App with which users can put down thoughts, feelings or even other important stuff for safe keeping.
This project is the final challenge of the 2018 ALCwithGoogle programme.

This is a relatively simple implementation of CRUD using Firebase Firestore, Storage and Material Design.
Text input are stored in Firestore, images attached are uploaded to cloud storage.
This data is persistent online, hence, even if the user logs into another device, their data are available.
And the app has a cool name too *Journally*

## Getting Started

You can clone the repo or just install the [APK](https://github.com/wil-power/journal-app/blob/master/release/app-release.apk)

### Prerequisite To Run Code

* Android Studio 2.4 or above.
* Build tools version 28
* Add Firebase to the project. Follow [Add Firebase to Android Project](https://firebase.google.com/docs/android/setup)
* Add Google Authentication to the project. 
Follow [Add Google Authentication](https://developers.google.com/identity/sign-in/android/start-integrating)

### Prerequisites To Install APK
* Make sure Google Play Services is installed on target device.

# Screenshots

1             |  2          | 3    | 4
:-------------------------:|:-------------------------:|:--------------:|:-----:
![screen1](https://github.com/wil-power/journal-app/blob/master/Screenshot_20180701-201002.png)  | ![screen2](https://github.com/wil-power/journal-app/blob/master/Screenshot_20180701-200852.png) | ![screen3](https://github.com/wil-power/journal-app/blob/master/Screenshot_20180701-202451.png) | ![screen4](https://github.com/wil-power/journal-app/blob/master/Screenshot_20180701-202500.png) 

## Features
* Google Sign in.
* Usual CRUD activities.
* Synchronised data across all devices logged into.


* Real time updates.
* Search.
* Swipe left to delete.
* Click to view entry details.
* Long click to update entry.
* Add images.

## Built With

* [Firebase](https://firebase.google.com/) - Database and authentication
* [Firebase Storage](https://firebase.google.com/products/storage/) - Image storage
* [Material Design](https://material.io/develop/android/) - UI design
* [Glide](https://bumptech.github.io/glide/) - Image loading

## Author

* **Wilfred Agyei Asomani** 

See also the list of [contributors](https://github.com/wil-power/Journally/graphs/contributors).

## Contribution
Ideas to make this much better are accepted.

## License

This project is licensed under the Apache License v2 - see the [LICENSE.md](https://github.com/wil-power/journal-app/blob/master/LICENSE) file for details

## Acknowledgments

* Dharan Aditya
* Stack Overflow
* [Android Developers](https://developer.android.com/)
* Firebase Team.
* Udacity.
* Andela Learning Community.
