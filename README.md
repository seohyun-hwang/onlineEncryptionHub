## Running the project
This is a fullstack application; the backend and frontend must be run simultaneously. The project files include both the frontend and backend.

Backend: run `src/main/java/com.example.encryptMsg/EncryptMsgApplication.java`.

Frontend: access `src/main/frontend` in the terminal/CMD, then enter `npm run dev`.

Ports: http://localhost:8080/ for backend; http://localhost:5173/ for frontend.

## Basic project information

This project was done for self-study on cryptographic algorithms, bitwise operations, finite-field arithmetic in GF(2^8) and GF(2^128), SpringBoot, Rest API, database interaction, unit-test writing, and UI/UX integration with ReactJS frontend.

The application asks you to create an account with a username and password, after which it lets you store encrypted text-entries in a database (which is cleared as soon as the backend program is terminated).

Features:
1. Create account (SHA-256 password hashing)
2. Delete account (eradication of all account data)
3. Create message (AES-256-GCM message encryption)
4. Delete message (eradiation of all message data)
5. Show all messages upon login (AES-256-GCM message decryption)

Your text-entries are encrypted using Rijndael AES-256 cryptography with Galois Counter Mode.
Your password is encrypted using a one-way SHA-256 hashing algorithm.
Passwords are salted and stretched using abridged PBKDF2 (uses SHA instead of HMAC-SHA; explained two paragraphs down).


My core cryptographic algorithms (PBKDF2, SHA-256, AES-256-GCM) are ***fully custom-rolled*** with no use of cipher libraries.
While custom-rolled cryptography is unprofessional and highly vulnerable to side-channel attacks, I believed it to be worth the practice. The code for all my cryptography is found in `src/main/java/service/EncryptionService.java`.

I first attempted AES-256 with CBC mode (as seen in the long comments) and later switched to GCM mode.

The high-level method-calling of these cryptographic algorithms in `src/main/java/service/UserService.java` uses a little help, as seen by 1. the import `java.security.SecureRandom` for more-secure pseudorandom number generation of salts and 2. the method call `java.security.MessageDigest.isEqual()` for constant-time array comparison of hashed values.

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