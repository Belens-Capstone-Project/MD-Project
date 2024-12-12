// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()  // Make sure to include this to fetch dependencies from Google's Maven repository
        mavenCentral()
    }

    dependencies {
        // Add the Google Services plugin classpath
        classpath ("com.android.tools.build:gradle:7.4.1")  // or the version you're using
        classpath ("com.google.gms:google-services:4.4.2")  // This is the version of the Google Services plugin
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}