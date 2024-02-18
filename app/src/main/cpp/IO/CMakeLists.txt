cmake_minimum_required(VERSION 2.8)
INCLUDE_DIRECTORIES("${PROJECT_SOURCE_DIR}/SDK")
INCLUDE_DIRECTORIES("${PROJECT_SOURCE_DIR}/IO")

message(STATUS ${PROJECT_SOURCE_DIR})
set(CMAKE_CXX_STANDARD 11)

FILE(GLOB SOURCE_MAIN ${PROJECT_SOURCE_DIR}/IO/src/*.cpp)

set(SOURCE_KPLIO  ${SOURCE_MAIN})
add_library(KPL_IO SHARED ${SOURCE_KPLIO})
add_library(sdk_kpl SHARED IMPORTED)

set_target_properties(sdk_kpl PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/SDK/${ANDROID_ABI}/libsdk_build.so)

target_link_libraries(KPL_IO  sdk_kpl)
target_link_libraries(KPL_IO  android)
target_link_libraries(KPL_IO  log)