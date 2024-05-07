// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    val room_version = "2.6.1"
    id("androidx.room") version "$room_version" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
}