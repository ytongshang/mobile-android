cmake_minimum_required(VERSION 3.4.1)

IF (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Windows")
    add_definitions(-DWindows)
ELSEIF (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Linux")
    add_definitions(-DLinux)
ENDIF ()

# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# 配置目标so库编译信息
add_library( # Sets the name of the library.
        jnitest

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        jnitest.c
        )

# 指定编译目标库时，cmake要链接的库
target_link_libraries(

        # 指定目标库，native-lib 是在上面 add_library 中配置的目标库
        jnitest

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})