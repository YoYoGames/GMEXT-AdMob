{
  "resourceType": "GMExtension",
  "resourceVersion": "1.2",
  "name": "AdMobPangle",
  "optionsFile": "options.json",
  "options": [],
  "exportToGame": true,
  "supportedTargets": -1,
  "extensionVersion": "0.0.1",
  "packageId": "",
  "productId": "",
  "author": "",
  "date": "2022-10-14T22:22:36.4639933-07:00",
  "license": "",
  "description": "",
  "helpfile": "",
  "iosProps": true,
  "tvosProps": false,
  "androidProps": true,
  "installdir": "",
  "files": [],
  "classname": "",
  "tvosclassname": null,
  "tvosdelegatename": null,
  "iosdelegatename": "",
  "androidclassname": "",
  "sourcedir": "",
  "androidsourcedir": "",
  "macsourcedir": "",
  "maccompilerflags": "",
  "tvosmaccompilerflags": "",
  "maclinkerflags": "",
  "tvosmaclinkerflags": "",
  "iosplistinject": "",
  "tvosplistinject": "",
  "androidinject": "",
  "androidmanifestinject": "",
  "androidactivityinject": "",
  "gradleinject": "\r\n    implementation  'com.google.ads.mediation:pangle:4.7.0.6.0'\r\n",
  "androidcodeinjection": "\r\n<YYAndroidTopLevelGradleAllprojectsRepositories>\r\nrepositories\r\n{\r\n    maven {\r\n        url 'https://artifact.bytedance.com/repository/pangle/'\r\n    }\r\n}\r\n</YYAndroidTopLevelGradleAllprojectsRepositories>\r\n\r\n\r\n<YYAndroidGradleDependencies>\r\n    implementation  'com.google.ads.mediation:pangle:4.7.0.6.0'\r\n</YYAndroidGradleDependencies>\r\n\r\n\r\n",
  "hasConvertedCodeInjection": true,
  "ioscodeinjection": "\r\n<YYIosCocoaPods>\r\npod 'GoogleMobileAdsMediationPangle'\r\n</YYIosCocoaPods>\r\n",
  "tvoscodeinjection": "",
  "iosSystemFrameworkEntries": [],
  "tvosSystemFrameworkEntries": [],
  "iosThirdPartyFrameworkEntries": [],
  "tvosThirdPartyFrameworkEntries": [],
  "IncludedResources": [],
  "androidPermissions": [],
  "copyToTargets": -1,
  "iosCocoaPods": "\r\npod 'GoogleMobileAdsMediationPangle'\r\n",
  "tvosCocoaPods": "",
  "iosCocoaPodDependencies": "",
  "tvosCocoaPodDependencies": "",
  "parent": {
    "name": "Mediations",
    "path": "folders/AdMob/Extensions/Mediations.yy",
  },
}