# $Id: CMakeLists.txt 1059 2014-07-09 08:26:09Z RS $
# This is the CMake project file for configuration of the RiVLib examples.
#
#           Copyright (c) 2015 Riegl Laser Measurement Systems.

# The authors hereby  grant permission  to use,  and copy this software  for the
# purpose  of enhancing the useability  of  Riegl Laser Measurement Systems GmbH
# instruments you own. You may NOT distribute or modify the software for the use
# in commercial applications without the written consent of RLMS.
#
# In any case copyright notices and  this notice must be  retained in all copies
# verbatim in any distributions.

# IN  NO EVENT SHALL  THE AUTHORS OR  DISTRIBUTORS BE  LIABLE TO  ANY PARTY  FOR
# DIRECT, INDIRECT, SPECIAL,  INCIDENTAL, OR  CONSEQUENTIAL DAMAGES  ARISING OUT
# OF THE USE OF THIS SOFTWARE, ITS  DOCUMENTATION,  OR ANY DERIVATIVES  THEREOF,
# EVEN IF THE AUTHORS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# THE AUTHORS AND DISTRIBUTORS SPECIFICALLY DISCLAIM  ANY WARRANTIES, INCLUDING,
# BUT NOT LIMITED TO, THE IMPLIED WARRANTIES  OF MERCHANTABILITY,  FITNESS FOR A
# PARTICULAR PURPOSE,  AND  NON-INFRINGEMENT. THIS SOFTWARE  IS  PROVIDED  ON AN
# "AS IS" BASIS, AND THE AUTHORS  AND DISTRIBUTORS HAVE NO OBLIGATION TO PROVIDE
# MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

# This file is the input description for CMake, a tool which is able to
# generate build environments for various compilers.
# You can obtain a copy of CMake at http://www.cmake.org
# It is recommended to use the graphical tool from CMake to do the
# configuration. First you have to specify the source directory i.e. where
# the RiVlib examples are. Next you specify a directory where you want your
# build to take place. Typically this is the directory where the project files
# of your IDE will be created.
# Example:
# Source dir: ...../examples
# Build dir:  ...../examples-build
# The next step is to press the configure button. CMake will prompt you
# with a message and a variable in red, requesting you to specify the
# root directory of your variant of rivlib you will use. Use the ... navigate
# button to specify the subdirectory within your downloaded copy of RiVLib that
# fits your compiler.
# Example:
# RiVLib_DIR = .../rivlib-1_30-x86-windows-vc120 
# Repeat pressing the configuration button and resolve any more red lines,
# unitil there are no unsatisfied constraints remaining.
# Press generate, and the project files will be created.
# Now you can launch your favorite IDE or invoke the genrated Makefile.
#
# NOTE:
# RiVLib needs a compiler with C++11 support. Most recent compilers
# already do have such support. If using GCC you might need to add the
# flag -std=gnu++0x to the CMAKE_CXX_FLAGS variable.
#

cmake_minimum_required(VERSION 3.4.1)

include( InstallRequiredSystemLibraries )

project(RiVLibJNI VERSION 2.0.0 DESCRIPTION "RiVLib library JNI wrapper")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED on)
set(CMAKE_BUILD_TYPE Release)

set(RiVLib_VERSION 2_13_0)

#
# RiVLib
#

# optional: set RiVLib_DIR manually to specific variant of rivlib
# if not set cmake-gui will fail to configure project and will ask you to specify the root directory of your variant of rivlib
if (CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
    # 64 bits
    set(RiVLib_DIR "${CMAKE_SOURCE_DIR}/rivlib/rivlib-${RiVLib_VERSION}-x86_64-windows-vc142/cmake")
elseif (CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
    # 64 bits
    set(RiVLib_DIR "${CMAKE_SOURCE_DIR}/rivlib/rivlib-${RiVLib_VERSION}-x86_64-linux-gcc13/cmake")
endif ()

message("RiVLib folder ${RiVLib_DIR}")

find_package(RiVLib
    COMPONENTS scanlib scanifc ctrllib ctrlifc
)

include_directories(
    ${RiVLib_INCLUDE_DIRS}
)

#
# JNI
#

# optional: you may set specific Java version, if not FindJNI CMake module will look for default Java
#set(ENV{JAVA_HOME} "/usr/lib/jvm/bellsoft-java18-full-amd64")
# preliminary call to FindJava CMake module to make sure there is Java >= 17 installed on the system (or selected with JAVA_HOME) 
find_package(Java 25)
find_package(JNI REQUIRED) 

include_directories(
    ${JNI_INCLUDE_DIRS}
)

#
# Shared library
#

add_library(${PROJECT_NAME} SHARED
    src/mypointcloud.cpp
    src/rivliblibrary.cpp
)

set_target_properties(${PROJECT_NAME} PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
    PUBLIC_HEADER src/rivliblibrary.h
)

#target_include_directories(${PROJECT_NAME} PRIVATE . )

target_link_libraries( ${PROJECT_NAME}
    ${RiVLib_CTRLLIB_LIBRARY}
    ${RiVLib_SCANLIB_LIBRARY}
)

if (UNIX)
	set_target_properties( ${PROJECT_NAME}
    PROPERTIES
        LINK_FLAGS "-z origin"
        INSTALL_RPATH "\\\$ORIGIN"
)
endif (UNIX)

