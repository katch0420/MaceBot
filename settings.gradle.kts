pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Check the latest version on https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.6"

    // Used for cross-compat for 26.1+ and older versions (https://codeberg.org/KikuGie/loom-back-compat)
    id("dev.kikugie.loom-back-compat") version "0.4"

    // Sometimes it is needed to make Gradle run at all, so it doesn't hurt to have
    // (https://github.com/gradle/foojay-toolchains)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        //
        // Each group folder targets the HIGHEST Minecraft version in its mc_compat range,
        // since that's what Loom actually compiles/runs against for that folder.
        //   "1.21.1"  -> covers >=1.21     <=1.21.1
        //   "1.21.3"  -> covers >=1.21.2   <=1.21.3
        //   "1.21.4"  -> covers  1.21.4
        //   "1.21.5"  -> covers  1.21.5
        //   "1.21.8"  -> covers >=1.21.6   <=1.21.8
        //   "1.21.10" -> covers >=1.21.9   <=1.21.10
        //   "1.21.11" -> covers  1.21.11
        versions("1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11")

        // Same range-grouping pattern applied here, named by upper bound:
        //   "26.1.2" -> covers 26.1, 26.1.1, 26.1.2
        //   "26.2.1" -> covers 26.2, 26.2.1
        // NOTE: this folds your earlier standalone "26.2.x" -> "26.2" entry into "26.2.1",
        // since 26.2.1 is a later patch of the same 26.2 line and having both would mean
        // two separate folders targeting overlapping versions. If you actually want "26.2"
        // on its own (not merged with 26.2.1's range), tell me and I'll split them back out.
        versions("26.1.2", "26.2")

        // NOTE: double check this grouping is what you meant — I inferred the ranges from
        // the flat list you gave (26.1, 26.1.1, 26.1.2, 26.2, 26.2.1) by following the same
        // "name = upper bound" convention as your other groups. If you actually wanted these
        // as 5 separate standalone folders instead of 2 grouped ranges, let me know and I'll
        // split them out with versions("26.1", "26.1.1", "26.1.2", "26.2", "26.2.1") instead.

        vcsVersion = "26.2" // change this if you'd rather diff against the oldest version instead
    }
}

rootProject.name = "MaceBot"