# Full-Stack Student Management System (Spring Boot + JWT + MySQL)

Backend and frontend run together: the HTML/CSS/JS pages live in
`src/main/resources/static`, so one command starts everything.

## Run
1. Install Java 17+, Maven and MySQL (MySQL must be running).
2. Open `src/main/resources/application.properties` and set `spring.datasource.password`.
   The `student_db` database and all tables are created automatically.
3. `mvn spring-boot:run`   (or run `StudentManagementApplication` from Eclipse/STS/IntelliJ)
4. Open http://localhost:8080  ->  Register -> Login -> Dashboard

## Where things happen (backend)
| Folder | What it does |
|---|---|
| controller/ | Receives HTTP requests (AuthController, StudentController) |
| service/ | Business rules (AuthService, StudentService, TokenBlacklistService) |
| repository/ | Talks to MySQL (Spring Data JPA) |
| model/ | Tables: User, Student, BlacklistedToken |
| dto/ | Shape + validation of request JSON |
| security/ | JwtUtil (make/check token), JwtAuthFilter (checks every request), 401 handler, user lookup |
| config/SecurityConfig | Which URLs are public, plugs in the JWT filter, BCrypt bean |
| exception/ | Turns errors into clean JSON + correct status codes |

## Where things happen (frontend, static/)
| File | Page |
|---|---|
| register.html + js/register.js | Registration page |
| login.html + js/login.js | Login page (stores the JWT in localStorage) |
| dashboard.html + js/dashboard.js | Student dashboard: list, view by ID, delete, logout |
| add-student.html + js/add-student.js | Add Student form |
| edit-student.html + js/edit-student.js | Edit Student form |
| js/api.js | Helpers: adds the `Authorization: Bearer` header to every call |

## Postman checks (acceptance criteria)
1. POST /api/auth/register with {"username","email","password","confirmPassword"} -> 201
2. Console prints `Password hash: $2a$10$...`
3. Check MySQL: `SELECT * FROM users;` shows the hash, not the password
4. POST /api/auth/login {"email","password"} -> 200 with a `token`
5. Wrong password -> 401 "Invalid email or password"
6. GET /api/students with NO token -> 401
7. GET /api/students with header `Authorization: Bearer <token>` -> 200; also try POST, PUT, DELETE
8. POST /api/auth/logout with the token -> 200, then GET /api/students with the same token -> 401
9. In the browser, check the success/error messages on every page
10. Everything above works with frontend + backend + MySQL connected

## Notes
- The password hash is printed to the console only because the assignment asks for it. Do not do this in production.
- `jwt.secret` in application.properties must be 32+ characters. Change it before any real deployment.
- Tokens expire after 1 hour (`jwt.expiration-ms`).
