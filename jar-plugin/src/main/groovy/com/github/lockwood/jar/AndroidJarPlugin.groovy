package com.github.lockwood.jar

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

/**
 * Gradle plugin which will find your android.jar
 *
 * In your root build.gradle add
 *
 * plugins {
 *   id 'com.github.lockwood.jar' version “1.0.0”
 * }
 *
 * ext.androidJar = androidJar.find(targetSdkVersion)
 *
 */
class AndroidJarPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('androidJar', AndroidJarPluginExtension, project)
    }
}

class AndroidJarPluginExtension {

    Project mProject

    AndroidJarPluginExtension(Project project) {
        mProject = project
    }

    ConfigurableFileCollection find(int sdk) {
        def sdkLocation = findSdkLocation(mProject.rootDir).toString()
        mProject.files(sdkLocation + "/platforms/android-" + sdk.toString() + "/android.jar")
    }

    private static File findSdkLocation(File rootDir) {
        def localPropertiesFile = new File(rootDir, "local.properties")

        if (localPropertiesFile.exists()) {
            return findLocalPropertiesAndroidFile(rootDir, localPropertiesFile)
        } else {
            return findEnvAndroidFile()
        }
    }

    private static File findLocalPropertiesAndroidFile(File rootDir, File localPropertiesFile) {
        def localProperties = new Properties()
        localProperties.load(localPropertiesFile.newReader())

        def sdkDir = localProperties.getProperty("sdk.dir")

        if (sdkDir != null) {
            return new File(sdkDir)
        } else {
            def androidDir = localProperties.getProperty("android.dir")

            if (androidDir != null) {
                return new File(rootDir, androidDir)
            }
        }

        throw new RuntimeException("No sdk.dir property defined in local.properties file.")
    }

    private static File findEnvAndroidFile() {
        def envVar = System.getenv("ANDROID_HOME")

        if (envVar != null) {
            return new File(envVar)
        } else {
            def property = System.getProperty("android.home")

            if (property != null) {
                return new File(property)
            }
        }

        throw new RuntimeException("Can't find SDK path")
    }
}