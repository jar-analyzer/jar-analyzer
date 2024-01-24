cmake_minimum_required(VERSION 3.25)

################ JNI CONFIG START ################

project(native C)

set(CMAKE_C_STANDARD 11)

# INCLUDE JNI

find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})

################ JNI CONFIG END ##################

if (WIN32)
    MESSAGE("BUILD WINDOWS")

    add_library(
            console SHARED
            jni_console.h
            jni_console.c
    )
else ()
    MESSAGE("BUILD LINUX")
endif ()
