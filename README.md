Disclaimer: Cryptographic algorithms are built completely from scratch purely for self-educational purposes. Not for production use!!!

Since custom-rolled cryptography is unprofessional, I also included a fully library-based version of the cryptography in `src/main/java/com.example.encryptMsg/cryptogrpahy/EncryptionCompliant.java`. The custom version is in `EncryptionCustom.java`.

To toggle from the custom-rolled version to the library-based version, go to `src/main/java/com.example.encryptMsg/service/UserService.java`, then find the class constructor and edit `@Qualifier("custom")` to `@Qualifier("compliant")`.

## Running the project
This is a fullstack application; the backend and frontend must be run simultaneously. The project files include both the frontend and backend.

Backend: run `src/main/java/com.example.encryptMsg/EncryptMsgApplication.java`.

Frontend: access `src/main/frontend` in the terminal/CMD, then enter `npm run dev`.

Ports: http://localhost:8080/ for backend; http://localhost:5173/ for frontend.

## Basic project information

This project was done for self-study on cryptographic algorithms, bitwise operations, finite-field arithmetic in GF(2^8) and GF(2^128), SpringBoot, Rest API, strategy patterns, database interaction, unit-test writing, and UI/UX integration with ReactJS frontend.

The application asks you to create an account with a username and password, after which it lets you store encrypted text-entries in a database (which is cleared as soon as the backend program is terminated).

Features:
1. Create account (SHA-256 password hashing)
2. Delete account (eradication of all account data)
3. Create message (AES-256-GCM or AES-256-CBC message encryption depending on user-choice)
4. Delete message (eradiation of all message data)
5. Show all messages upon login (AES-256-GCM/CBC message decryption)

Your text-entries are encrypted using Rijndael AES-256 cryptography with Galois Counter Mode (GCM) or Cipher Block Chaining (CBC) based on your choice during account creation.
Your password is encrypted using a one-way SHA-256 hashing algorithm.
Passwords are salted and stretched using PBKDF2 (implements HMAC-SHA256).

My core cryptographic algorithms (PBKDF2, SHA-256, AES-256-GCM) are ***fully custom-rolled*** with no use of cipher libraries.
While custom-rolled cryptography is unprofessional, highly vulnerable to side-channel attacks, and doesn't take advantage of hardware optimizations, I believed it to be worth the practice. The code for all my custom cryptography is found in `src/main/java/com.example.encryptMsg/cryptography/EncryptionCustom.java`.

As already mentioned at the top, I therefore added a fully library-based version of the cryptography in `cryptography/EncryptionCompliant.java`.

Additional protection implemented against:
1. Timing attacks 
   1. Solution: constant-time array comparison found in `cryptography/customrolled/AES256Universal.java`
3. Man-in-the-middle attacks
   1. Solution: cipher modes (AES-GCM and AES-CBC)
   2. Tested in `src/test/java/com.example.encryptMsg/crackingTests`
2. SQL injections
   1. Solution: SpringDataJPA's prepared SQL statements
   2. Tested in `crackingTests/Injection_SQL_Tests.java`
4. Padding oracle attacks
   1. Solution: providing the GCM option which doesn't use padding
4. Rainbow table attacks
   1. Solution: using salt
5. Brute-force attacks
   1. Solution: PBKDF2 key-stretching with HMAC-SHA256
5. Time-of-check to Time-of-use race-condition
   1. Solution: immediately using system-state upon access and implementing a global exception handler for the case that the state does not exist
   2. See global exception handler in `java/com.example.encryptMsg/GlobalExceptionHandler.java`
6. String literals are not cleared readily by Java's garbage collector
   1. Solution: accepting sensitive data from the frontend, incl. password and message-plaintext, as character-arrays instead of Strings
2. Sensitive data generally remains in main-memory for quite some time until cleared by Java's garbage collector.
   1. Solution: filling arrays with 0s as soon as they are no longer needed
6. SpringBoot's JSON parser "Jackson" parses text-inputs as Strings by default
   1. Solution: implemented a class `java/com.example.encryptMsg/config/CharArrDeserialization.java` which overrides Jackson's deserialization process to parse texts as char-arrays

Strategy patterns (loosely-coupled):
1. There is a frontend button-row in account-creation with which the user decides whether to use AES-GCM or AES-CBC as the message cipher mode. The choice of cipher-mode is then transmitted to the backend to switch between `AES256GCM.java` and `AES256CBC.java`, respectively.
2. The editor decides whether the custom-rolled cryptography or the fully library-based cryptography is used by toggling the @Qualifier annotation argument between "custom" and "compliant". The argument determines which of the two `CryptographyToggle` interface implementations `EncryptionCompliant.java` and `EncryptionCustom.java` should be used in `UserService.java`.

All Rest API communication to the frontend is found in `src/main/java/controller/UserController.java`.

All unit-tests are found in `src/test/java/com.example.encryptMsg/`. Mockito is the mocking framework used in `UserControllerTest.java` and `UserServiceTest.java`.

### Backend development tools
Java 21, SpringBoot 4.1.0, Maven 4.0.0, Jar packaging, Properties configuration

Key dependencies: Spring Web, Spring Boot DevTools, Spring Data JPA, Spring Web MVC, H2 Database

Key plugins: Eirslett Frontend Maven 1.15.1, Maven Resources node 20.11.0 npm 10.2.4

### Frontend development tools
TypeScript 6.0.2, React 19.2.8, Vite 8.2.2

### NIST documentation
I did my best to design my SHA-256 and AES-256 algorithms in a manner that is faithful to the official documentation by the National Institute of Standards and Technology (NIST).

FIPS 197 (AES): https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197-upd1.pdf

FIPS PUB 180-4 (SHA): https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.180-4.pdf

SP 800-38D (GCM): https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf

FIPS PUB 198-1 (HMAC): https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.198-1.pdf

Special Publication 800-132 (PBKDF and salting): https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-132.pdf

CMVP Overview Page (advice against custom-rolled cryptography): https://csrc.nist.gov/projects/cryptographic-module-validation-program