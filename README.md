## Running the project
The backend and frontend must be run simultaneously. The project files include both the frontend and backend.

Backend: run `src/main/java/com.example.encryptMsg/EncryptMsgApplication.java`.

Frontend: access `src/main/frontend` in the terminal, then enter `npm run dev`.

Ports: http://localhost8080/ for backend; http://localhost:5173/ for frontend.

## Basic project information

This project was done for self-study on cryptographic algorithms, SpringBoot, Rest API, database interaction, unit-test writing, ReactJS frontend, and UI/UX integration.

The application asks you to create an account with a username and password, after which it lets you store encrypted text-entries in a database (which is cleared as soon as the backend program is terminated).

There are five features: 1. Create account, 2. Delete account, 3. Create message, 4. Delete message, and 5. Fetch all messages after account login.

The text-entries are encrypted using Rijndael AES-256 cryptography with Cipher Block Chaining.
Your password is stored in ciphertext after being passed through a one-way SHA-256 algorithm. Passwords are salted and stretched using abridged PBKDF2 (with 600,000 iterations).


My core cryptographic algorithms (PBKDF2, SHA-256, AES-256, CBC mode) are ***fully custom-rolled*** and use no libaries.
While custom-rolled cryptography is unprofessional and highly vulnerable to side-channel attacks, I believed it to be worth the practice. The code for all my cryptography is found in `src/main/java/service/EncryptionService.java`.

Because this project is an exclusively server-side application, I refrained from Message Authentication measures (such as AES-GCM and HMAC) which would have added significant security to the application but cannot be implemented on the Application layer without dedicated client-side software.

The high-level implementation of these cryptographic algorithms in `src/main/java/service/userService` use a little help, as seen by 1. the import `java.security.SecureRandom` and 2. the method call `java.security.MessageDigest.isEqual()`.

All Rest API communication to the frontend is found in `src/main/java/controller/UserController.java`.

All unit-tests are found in `src/test/java/com.example.encryptMsg/`.

### Backend development tools
SpringBoot 4.1.0, Maven 4.0.0, Java 21, Jar packaging, Properties configuration

Key dependencies: Spring Web, Spring Boot DevTools, Spring Data JPA, Spring Web MVC, H2 Database (Hibernate)

Key plugins: Eirslett Frontend Maven 1.15.1, Maven Resources node 20.11.0 npm 10.2.4

### Frontend development tools
Vite 8.2.2, React 19.2.8, TypeScript 6.0.2, ESLint 8.67.0