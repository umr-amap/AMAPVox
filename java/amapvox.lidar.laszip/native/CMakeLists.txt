cmake_minimum_required(VERSION 3.10)

include( InstallRequiredSystemLibraries )

# set the project name
project(JLasApi VERSION 1.1)

#
# Shared library
#

set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)

add_library(${PROJECT_NAME} SHARED
    src/jlas_api.c
)

set_target_properties(${PROJECT_NAME} PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1.1
    PUBLIC_HEADER src/jlas_api.h
)

cmake_path(SET LASZIP "${PROJECT_SOURCE_DIR}/LAStools/LASzip")
target_include_directories(${PROJECT_NAME} PRIVATE
    "${LASZIP}/dll"
    "${LASZIP}/src"
)


