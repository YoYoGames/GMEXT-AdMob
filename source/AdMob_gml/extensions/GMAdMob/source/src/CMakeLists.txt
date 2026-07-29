set(SRC_DIR ${CMAKE_CURRENT_LIST_DIR})

# =============================================================================
# Deployment target
# Unset elsewhere, so it defaults to the host's newest SDK - mismatches the
# GameMaker app's own target and crashes the linker. Pin to the SDKs' floor.
# =============================================================================
set_target_properties(${PROJECT_NAME} PROPERTIES
  XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET "12.0"
  XCODE_ATTRIBUTE_TVOS_DEPLOYMENT_TARGET "12.0")

# =============================================================================
# GameMaker iOS integration (optional)
# =============================================================================
set(EXT_GM_XCODEPROJ_OVERRIDE "~/GameMakerStudio2/GM_IOS/AdMob/AdMob/AdMob.xcodeproj" CACHE FILEPATH "" FORCE)
set(EXT_GM_APP_TARGET_OVERRIDE "" CACHE STRING "" FORCE)

# =============================================================================
# Public headers for Apple XCFramework packaging
# Users should edit these to match their project structure.
# The packager will copy these into a headers folder passed to xcodebuild.
# =============================================================================
set(EXT_PUBLIC_HEADERS_IOS
    "${CMAKE_SOURCE_DIR}/src/ios/${PROJECT_NAME}_ios.h;${CMAKE_SOURCE_DIR}/code_gen/ios/${PROJECT_NAME}Internal_ios.h"
    CACHE STRING "Semicolon-separated list of public headers to embed in the iOS xcframework")

set(EXT_PUBLIC_HEADERS_TVOS
    "${CMAKE_SOURCE_DIR}/src/tvos/${PROJECT_NAME}_tvos.h;${CMAKE_SOURCE_DIR}/code_gen/tvos/${PROJECT_NAME}Internal_tvos.h"
    CACHE STRING "Semicolon-separated list of public headers to embed in the tvOS xcframework")

# =============================================================================
# User implementation sources per platform (editable template)
# =============================================================================
if(WIN32)
  file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/native/*.cpp")

elseif(APPLE)
  if(CMAKE_OSX_SYSROOT MATCHES "iphone")
    file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/ios/*.mm" "${SRC_DIR}/ios/*.cpp" "${SRC_DIR}/ios/*.swift")
  elseif(CMAKE_OSX_SYSROOT MATCHES "appletv")
    file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/tvos/*.mm" "${SRC_DIR}/tvos/*.cpp" "${SRC_DIR}/tvos/*.swift")
  else()
    file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/native/*.mm" "${SRC_DIR}/native/*.cpp" "${SRC_DIR}/native/*.swift")
  endif()

elseif(ANDROID)
  file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/android/*.cpp")

elseif(UNIX)
  file(GLOB TARGET_SRCS CONFIGURE_DEPENDS "${SRC_DIR}/native/*.cpp")
endif()

target_sources(${PROJECT_NAME} PRIVATE ${TARGET_SRCS})
target_include_directories(${PROJECT_NAME} PRIVATE "${SRC_DIR}")
