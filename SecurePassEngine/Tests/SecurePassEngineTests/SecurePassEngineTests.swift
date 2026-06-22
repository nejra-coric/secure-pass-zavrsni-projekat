import Foundation
#if canImport(Darwin)
import Darwin
#elseif canImport(Glibc)
import Glibc
#elseif canImport(Musl)
import Musl
#endif
import Testing
@testable import SecurePassEngine

@Test func generatePasswordRespectsLength() throws {
    let password = try #require(
        SecurePassEngine.generatePassword(length: 16, includeSpecialChars: true)
    )
    #expect(password.count == 16)
}

@Test func generatePasswordWithoutSpecialChars() throws {
    let password = try #require(
        SecurePassEngine.generatePassword(length: 12, includeSpecialChars: false)
    )
    let special = CharacterSet(charactersIn: "!@#$%^&*()-_=+[]{}|;:,.<>?")
    #expect(password.rangeOfCharacter(from: special) == nil)
}

@Test func generatePasswordIncludesAllClassesWhenSpecialEnabled() throws {
    let password = try #require(
        SecurePassEngine.generatePassword(length: 8, includeSpecialChars: true)
    )
    #expect(password.rangeOfCharacter(from: .lowercaseLetters) != nil)
    #expect(password.rangeOfCharacter(from: .uppercaseLetters) != nil)
    #expect(password.rangeOfCharacter(from: .decimalDigits) != nil)
    #expect(password.rangeOfCharacter(from: CharacterSet(charactersIn: "!@#$%^&*()-_=+[]{}|;:,.<>?")) != nil)
}

@Test func calculateEntropyUsesLog2Formula() {
    let entropy = SecurePassEngine.calculateEntropy(password: "Aa1")
    let expected = 3.0 * (Darwin.log(62.0) / Darwin.log(2.0))
    #expect(abs(entropy - expected) < 0.001)
}

@Test func calculateEntropyEmptyPassword() {
    #expect(SecurePassEngine.calculateEntropy(password: "") == 0)
}

@Test func cExportGeneratePassword() {
    let bufferSize = 32
    let buffer = UnsafeMutablePointer<CChar>.allocate(capacity: bufferSize)
    defer { buffer.deallocate() }

    let status = securepass_generate_password(10, 1, buffer, Int32(bufferSize))
    #expect(status == 0)

    let password = String(cString: buffer)
    #expect(password.count == 10)
}

@Test func cExportCalculateEntropy() {
    let testPassword = "Test123!"
    let entropy = testPassword.withCString { securepass_calculate_entropy($0) }
    #expect(entropy > 0)
}
