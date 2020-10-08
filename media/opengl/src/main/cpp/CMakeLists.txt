# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

include_directories(
        glm
        j4a
        util
        render
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(j4a_files
        j4a/j4a_base.c
        j4a/class/android/os/Build.c
        )

set(util_files util/GLUtils.cpp)

add_library( # Sets the name of the library.
        native-render

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${j4a_files}
        ${util_files}
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        native-render

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        EGL
        GLESv3
        )