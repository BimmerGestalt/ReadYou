Car Resources
=============

The car requires that all Connected Apps authenticate with BMW-signed certificate files.
This project needs copies of these official certificate files to log in to the car.
These certificates and other resource files can be found in official Connected Apps, and every compatible Connected App will have a `assets/carapplications` directory inside it's APK File.
This project's build script automatically extracts the needed files from these official APKs during compilation.

Generally, the exact version of the APK doesn't matter, as long as it contains the necessary resources.

Please place copies of the following Android APKs in this `external` directory:
  - [BMW Connected Classic](https://apkpure.com/bmw-connected-classic/com.bmwgroup.connected.bmw.usa/download?from=details)
  - [MINI Connected Classic](https://apkpure.com/mini-connected-classic/com.bmwgroup.connected.mini.usa/download?from=details)

After placing these files in this `external` directory, re-run the build process and it should complete successfully.
The build process should automatically extract the necessary files to the proper directories.
