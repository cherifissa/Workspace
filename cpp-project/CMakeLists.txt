cmake_minimum_required(VERSION 3.20)
project(interop LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(JNI REQUIRED)
find_package(range-v3 CONFIG REQUIRED)

add_library(bridge SHARED bridge.cpp)
target_include_directories(bridge PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(bridge PRIVATE range-v3::range-v3)

add_executable(cpp_interop main.cpp)
target_include_directories(cpp_interop PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(cpp_interop PRIVATE ${JNI_LIBRARIES})