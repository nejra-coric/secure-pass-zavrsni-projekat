// swift-tools-version: 6.2

import PackageDescription

let package = Package(
    name: "SecurePassEngine",
    products: [
        .library(
            name: "SecurePassEngine",
            targets: ["SecurePassEngine"]
        ),
    ],
    targets: [
        .target(
            name: "SecurePassEngine"
        ),
        .testTarget(
            name: "SecurePassEngineTests",
            dependencies: ["SecurePassEngine"]
        ),
    ]
)
