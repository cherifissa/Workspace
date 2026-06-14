cmake_minimum_required(VERSION 3.20)
project(interop LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if(APPLE)
  execute_process(
    COMMAND /usr/libexec/java_home
    OUTPUT_VARIABLE JAVA_HOME
    OUTPUT_STRIP_TRAILING_WHITESPACE
  )
  set(ENV{JAVA_HOME} "${JAVA_HOME}")
  set(JAVA_HOME "${JAVA_HOME}" CACHE PATH "" FORCE)
endif()

find_package(JNI REQUIRED)
find_package(Python3 REQUIRED COMPONENTS Development)
find_package(range-v3 CONFIG REQUIRED)

add_library(bridge SHARED bridge.cpp)
target_include_directories(bridge PRIVATE ${JNI_INCLUDE_DIRS} ${Python3_INCLUDE_DIRS})
target_link_libraries(bridge PRIVATE range-v3::range-v3 Python3::Python)
