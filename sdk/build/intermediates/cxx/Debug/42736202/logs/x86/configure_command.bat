@echo off
"C:\\Users\\Madhuri\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HC:\\Users\\Madhuri\\AndroidStudioProjects\\openCV setup\\sdk\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=24" ^
  "-DANDROID_PLATFORM=android-24" ^
  "-DANDROID_ABI=x86" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86" ^
  "-DANDROID_NDK=C:\\Users\\Madhuri\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\Madhuri\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\Madhuri\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\Madhuri\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=C:\\Users\\Madhuri\\AndroidStudioProjects\\openCV setup\\sdk\\build\\intermediates\\cxx\\Debug\\42736202\\obj\\x86" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=C:\\Users\\Madhuri\\AndroidStudioProjects\\openCV setup\\sdk\\build\\intermediates\\cxx\\Debug\\42736202\\obj\\x86" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BC:\\Users\\Madhuri\\AndroidStudioProjects\\openCV setup\\sdk\\.cxx\\Debug\\42736202\\x86" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
