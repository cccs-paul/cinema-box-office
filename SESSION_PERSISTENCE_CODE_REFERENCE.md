# Session Persistence Implementation - Code Reference

## Key Code Changes

### 1. Frontend: ResponsibilityCentreService

**File:** `frontend/src/app/services/responsibility-centre.service.ts`

**Changes:** Added `{ withCredentials: true }` to all HTTP methods

```typescript
getAllResponsibilityCentres(): Observable<ResponsibilityCentreDTO[]> {
  return this.http.get<ResponsibilityCentreDTO[]>(this.apiUrl, { withCredentials: true });
}

getResponsibilityCentre(id: number): Observable<ResponsibilityCentreDTO> {
  return this.http.get<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
}

createResponsibilityCentre(name: string, description: string): Observable<ResponsibilityCentreDTO> {
  return this.http.post<ResponsibilityCentreDTO>(this.apiUrl, {
    name,
    description
  }, { withCredentials: true });
}

updateResponsibilityCentre(id: number, name: string, description: string): Observable<ResponsibilityCentreDTO> {
  return this.http.put<ResponsibilityCentreDTO>(`${this.apiUrl}/${id}`, {
    name,
    description
  }, { withCredentials: true });
}

deleteResponsibilityCentre(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
}

grantAccess(rcId: number, targetUsername: string, accessLevel: string): Observable<RCAccessDTO> {
  return this.http.post<RCAccessDTO>(`${this.apiUrl}/${rcId}/access/grant`, {
    targetUsername,
    accessLevel
  }, { withCredentials: true });
}

revokeAccess(rcId: number, targetUsername: string): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/${rcId}/access/${targetUsername}`, { withCredentials: true });
}

getResponsibilityCentreAccess(rcId: number): Observable<RCAccessDTO[]> {
  return this.http.get<RCAccessDTO[]>(`${this.apiUrl}/${rcId}/access`, { withCredentials: true });
}
```

---

### 2. Frontend: AuthService

**File:** `frontend/src/app/services/auth.service.ts`

**Changes:** Added `{ withCredentials: true }` to authentication methods

```typescript
loginLocal(username: string, password: string): Observable<User> {
  return this.http
    .post<User>(
      `${this.API_URL}/users/authenticate?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
      {},
      { withCredentials: true }  // ADDED
    )
    .pipe(
      tap((user) => this.setCurrentUser(user)),
      catchError((error) => this.handleError(error))
    );
}

loginLdap(username: string, password: string): Observable<User> {
  return this.http
    .post<User>(
      `${this.API_URL}/users/ldap-login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
      {},
      { withCredentials: true }  // ADDED
    )
    .pipe(
      tap((user) => this.setCurrentUser(user)),
      catchError((error) => this.handleError(error))
    );
}

handleOAuth2Callback(): Observable<User> {
  return this.http
    .get<User>(`${this.API_URL}/users/oauth2-callback`, { withCredentials: true })  // ADDED
    .pipe(
      tap((user) => this.setCurrentUser(user)),
      catchError((error) => this.handleError(error))
    );
}
```

---

### 3. Backend: UserController - Authenticate Method

**File:** `backend/src/main/java/com/myrc/controller/UserController.java`

**Changes:** Added HttpServletResponse parameter and session context persistence

```java
@PostMapping("/authenticate")
@Operation(summary = "Authenticate user", description = "Authenticates a user with username and password")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication failed - invalid credentials or locked account")
})
public ResponseEntity<UserDTO> authenticate(
        @Parameter(description = "Username", required = true)
        @RequestParam String username,
        @Parameter(description = "Password", required = true)
        @RequestParam String password,
        HttpServletRequest request,
        HttpServletResponse response) {  // ADDED response parameter
    
    Optional<UserDTO> user = userService.authenticate(username, password);
    if (user.isPresent()) {
        // Create/access session
        HttpSession session = request.getSession(true);
        
        // Establish security context
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(username, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // KEY FIX: Persist security context to session
        // This ensures Spring Security can load it on subsequent requests
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.saveContext(SecurityContextHolder.getContext(), request, response);
        
        logger.info("User authenticated with session: " + username + 
                    " (Session ID: " + session.getId() + ")");
        return ResponseEntity.ok(user.get());
    } else {
        logger.warning("Authentication failed for user: " + username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
```

---

### 4. Backend: AuthenticationConfig - Bean & Session Management

**File:** `backend/src/main/java/com/myrc/config/AuthenticationConfig.java`

**Changes:** Added HttpSessionSecurityContextRepository bean and session management

```java
// NEW BEAN
@Bean
public HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
    return new HttpSessionSecurityContextRepository();
}

// IN SECURITY FILTER CHAIN
@Bean
public SecurityFilterChain basicSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // ADDED SESSION MANAGEMENT
        .sessionManagement(session -> session.sessionFixation().migrateSession())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(
                "/api/health",
                "/api/users/authenticate",
                "/api/users/ldap-login",
                "/api/users/oauth2-callback"
            ).permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
}

// CORS CONFIGURATION (Already present, key settings)
private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:4200"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Content-Type"));
    configuration.setAllowCredentials(true);  // KEY: Allows credentials in CORS requests
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### 5. Required Imports

**File:** `backend/src/main/java/com/myrc/controller/UserController.java`

```java
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
```

**File:** `backend/src/main/java/com/myrc/config/AuthenticationConfig.java`

```java
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
```

---

## How It Works

### Authentication Flow

1. **Client sends credentials:**
   ```
   POST /api/users/authenticate?username=admin&password=Admin%40123
   ```

2. **Server validates credentials:**
   - UserService checks database
   - If valid, creates security context

3. **Server creates session:**
   ```java
   HttpSession session = request.getSession(true);  // Creates JSESSIONID
   ```

4. **Server saves context to session (KEY STEP):**
   ```java
   HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
   repository.saveContext(SecurityContextHolder.getContext(), request, response);
   ```

5. **Server responds with:**
   ```
   Set-Cookie: JSESSIONID=<sessionid>; Path=/api; HttpOnly
   ```

6. **Client stores cookie and includes in subsequent requests:**
   ```
   Cookie: JSESSIONID=<sessionid>
   POST /api/responsibility-centres
   ```

7. **Spring Security on next request:**
   - Retrieves JSESSIONID from cookie
   - Loads HttpSession from JSESSIONID
   - Loads SecurityContext from session
   - Request executes with authenticated user

---

## Why `withCredentials: true` is Essential

In CORS cross-origin requests (frontend on 4200, API on 8080):

- **Without `withCredentials: true`:**
  - Browser won't send cookies with request
  - Browser won't process Set-Cookie response header
  - JSESSIONID never persisted

- **With `withCredentials: true`:**
  - Browser sends cookies with each request
  - Browser processes Set-Cookie response
  - JSESSIONID sent and stored
  - Server can load session context

- **Server must respond with:**
  - `Access-Control-Allow-Credentials: true`
  - `Access-Control-Allow-Origin: http://localhost:4200` (specific origin, not `*`)

---

## Testing the Implementation

### 1. Test Authentication Creates Session
```bash
curl -s -i -c /tmp/cookies.txt -X POST \
  "http://localhost:8080/api/users/authenticate?username=admin&password=Admin%40123"

# Look for: Set-Cookie: JSESSIONID=...
```

### 2. Test Session Persists
```bash
curl -s -b /tmp/cookies.txt -X GET \
  http://localhost:8080/api/responsibility-centres

# Should return 200 with RC list (not 401)
```

### 3. Test RC Creation with Session
```bash
curl -s -b /tmp/cookies.txt -X POST \
  http://localhost:8080/api/responsibility-centres \
  -H "Content-Type: application/json" \
  -d '{"name":"Test RC","description":"Test"}'

# Should return 201 Created (not 401)
```

---

## Before vs After

### Before (Broken)
```
POST /authenticate → JSESSIONID created ✅
POST /responsibility-centres → 401 Unauthorized ❌
Reason: SecurityContext not saved to session
```

### After (Fixed)
```
POST /authenticate → JSESSIONID created ✅
repository.saveContext() → Context saved to session ✅
POST /responsibility-centres → 201 Created ✅
Reason: SecurityContext properly persisted and restored
```

---

## Summary of Changes

1. **Frontend:** Added `{ withCredentials: true }` to all HTTP calls
2. **Backend:** Added `repository.saveContext()` to authenticate method
3. **Backend:** Added HttpSessionSecurityContextRepository bean
4. **Backend:** Configured session management in security filter chain
5. **CORS:** Already configured with `setAllowCredentials(true)`

**Result:** Session-based authentication fully functional ✅
