cmake_minimum_required(VERSION 3.18)

# -----------------------------------------------------------------------------
# Core "bag of usage requirements" your main target links to
# -----------------------------------------------------------------------------
add_library(ext_third_party INTERFACE)

# List of runtime files to bubble up to parent
set(_EXT_TP_RUNTIME_DLLS "")

# Helper macro to append runtime(s)
function(_ext_tp_append_runtime)
  foreach(_f IN LISTS ARGN)
    if(_f)
      list(APPEND _EXT_TP_RUNTIME_DLLS "${_f}")
    endif()
  endforeach()
  set(_EXT_TP_RUNTIME_DLLS
      "${_EXT_TP_RUNTIME_DLLS}"
      PARENT_SCOPE)
endfunction()

# =============================================================================
# WINDOWS (MSVC/ClangCL)
# =============================================================================
if(WIN32)
# =============================================================================
# APPLE: split macOS vs iOS/tvOS
# =============================================================================
elseif(APPLE)
  # ----------------------
  # iOS (mobile)
  # ----------------------
  if((CMAKE_OSX_SYSROOT MATCHES "iphone"))

    # The GameMaker Xcode project (set via EXT_GM_XCODEPROJ_OVERRIDE in
    # src/CMakeLists.txt) is where `pod install` drops the Google Mobile Ads
    # SDK. Its parent folder holds the CocoaPods `Pods/` tree.
    get_filename_component(EXT_GM_PARENT_DIR "${EXT_GM_XCODEPROJ_OVERRIDE}" DIRECTORY)
    set(_PODS_DIR "${EXT_GM_PARENT_DIR}/Pods")

    if(NOT EXISTS "${_PODS_DIR}")
      message(FATAL_ERROR
        "[third_party] CocoaPods folder not found: ${_PODS_DIR}\n"
        "Run `pod install` in the GameMaker Xcode project first "
        "(EXT_GM_XCODEPROJ_OVERRIDE = ${EXT_GM_XCODEPROJ_OVERRIDE}).")
    endif()

    # Pick device vs simulator slice for the current SDK.
    # CMAKE_OSX_SYSROOT is iPhoneOS / iPhoneSimulator.
    if(CMAKE_OSX_SYSROOT MATCHES "simulator")
      set(_WANT_SIMULATOR TRUE)
    else()
      set(_WANT_SIMULATOR FALSE)
    endif()

    # -------------------------------------------------------------------------
    # Locate a CocoaPods-vendored .xcframework by name (its exact path under
    # Pods/ changes between SDK versions, e.g. Google-Mobile-Ads-SDK moved from
    # Frameworks/GoogleMobileAdsFramework-Current/ to Frameworks/GoogleMobileAdsFramework/),
    # select the device/simulator slice, then wire it into ext_third_party for
    # BOTH compile (-F, so `#import <Framework/...>` resolves) and link
    # (-F + -framework).
    # -------------------------------------------------------------------------
    function(_ext_tp_link_pod_xcframework _pod_dir _xcf_name _fw_name _required)
      # Bound the search to the pod's own folder when present; fall back to the
      # whole Pods tree if the vendor uses an unexpected layout.
      set(_search_root "${_PODS_DIR}/${_pod_dir}")
      if(NOT EXISTS "${_search_root}")
        set(_search_root "${_PODS_DIR}")
      endif()

      file(GLOB_RECURSE _entries LIST_DIRECTORIES TRUE "${_search_root}/*")
      set(_xcf "")
      foreach(_c IN LISTS _entries)
        if(IS_DIRECTORY "${_c}" AND _c MATCHES "/${_xcf_name}$")
          set(_xcf "${_c}")
          break()
        endif()
      endforeach()

      if(NOT _xcf)
        if(_required)
          message(FATAL_ERROR
            "[third_party] Missing ${_xcf_name} under: ${_search_root}\n"
            "Run `pod install` so the Google Mobile Ads SDK is available.")
        else()
          message(STATUS "[third_party] Optional ${_xcf_name} not found under ${_search_root} (skipping)")
          return()
        endif()
      endif()

      # Choose the matching slice inside the xcframework.
      if(_WANT_SIMULATOR)
        set(_slice "${_xcf}/ios-arm64_x86_64-simulator")
        if(NOT EXISTS "${_slice}")
          # Some vendors ship arm64-only simulator slices on newer toolchains.
          set(_slice "${_xcf}/ios-arm64-simulator")
        endif()
      else()
        set(_slice "${_xcf}/ios-arm64")
      endif()

      if(NOT EXISTS "${_slice}/${_fw_name}.framework")
        message(FATAL_ERROR
          "[third_party] Could not find ${_fw_name}.framework slice.\n"
          "XCFramework: ${_xcf}\n"
          "Tried slice: ${_slice}\n"
          "Sysroot: ${CMAKE_OSX_SYSROOT}")
      endif()

      message(STATUS "[third_party] Using ${_fw_name}: ${_slice}")

      # -F for both compile and link; link the framework by name.
      target_compile_options(ext_third_party INTERFACE "-F${_slice}")
      target_link_options(ext_third_party INTERFACE "-F${_slice}")
      target_link_libraries(ext_third_party INTERFACE "-framework" "${_fw_name}")
    endfunction()

    # Google Mobile Ads SDK + the User Messaging Platform it depends on.
    _ext_tp_link_pod_xcframework(
      "Google-Mobile-Ads-SDK" "GoogleMobileAds.xcframework" "GoogleMobileAds" TRUE)
    _ext_tp_link_pod_xcframework(
      "GoogleUserMessagingPlatform" "UserMessagingPlatform.xcframework" "UserMessagingPlatform" TRUE)

    # GoogleMobileAds is a static framework; -ObjC ensures its Objective-C
    # categories load at the final link. Harmless if already set downstream.
    target_link_options(ext_third_party INTERFACE "-ObjC")

  # ----------------------
  # macOS (desktop)
  # ----------------------
  else()
  endif() # macOS

# =============================================================================
# ANDROID (Google Mobile Ads is consumed via Gradle/AAR, not here)
# =============================================================================
elseif(ANDROID)
# =============================================================================
# LINUX (shared .so or static .a)
# =============================================================================
elseif(UNIX)
endif()

# =============================================================================
# Bubble up runtime list to parent
# =============================================================================
set(EXT_TP_RUNTIME_DLLS "${_EXT_TP_RUNTIME_DLLS}" PARENT_SCOPE)
