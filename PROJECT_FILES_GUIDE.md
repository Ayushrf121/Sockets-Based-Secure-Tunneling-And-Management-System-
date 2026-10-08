# Secure Proxy: File-by-File Guide (Milestones 1 and 2)

A plain-language explanation of every file in the project so far: what it is for,
the important functions inside it, and how the pieces fit together.

---

## 1. The big picture

We are building a **secure tunnel server** that only people on your local network
can use, with proper login. So far we have built two layers:

```
 MILESTONE 1: "Who are the users?"          MILESTONE 2: "How do we talk safely?"
 ---------------------------------          -------------------------------------
 User            (what a user is)           TlsContext   (sets up encryption)
 PasswordHasher  (scrambles passwords)      NetworkGuard (only local network)
 UserStore       (rules for storage)        TlsServer    (listens for clients)
 FileUserStore   (saves users in a file)    TlsClient    (connects to server)
```

These two layers are not connected yet. In Milestone 3 the server will use
the user layer to check logins inside the encrypted connection.

### Project folder layout

```
Secure_Proxy_PBL/
├── pom.xml                       <- project settings and libraries
├── .gitignore                    <- files git must NOT save (passwords, keys)
├── scripts/
│   └── generate-certs.ps1        <- creates the encryption certificates
├── certs/                        <- generated keys (never commit this)
├── docs/                         <- documentation
└── src/
    ├── main/java/com/secureproxy/
    │   ├── models/      User.java
    │   ├── security/    PasswordHasher.java, TlsContext.java, NetworkGuard.java
    │   ├── storage/     UserStore.java, FileUserStore.java
    │   ├── server/      TlsServer.java
    │   └── client/      TlsClient.java
    └── test/java/com/secureproxy/
        ├── security/    PasswordHasherTest.java, NetworkGuardTest.java
        └── storage/     FileUserStoreTest.java
```

**Main code** is what runs in the real program. **Test code** only checks that
the main code works and is never shipped.

---

## 2. Words you will see (mini dictionary)

| Word | Simple meaning |
|---|---|
| **Hash** | A one-way scramble. You can turn "pass123" into gibberish, but you cannot turn the gibberish back into "pass123". |
| **Salt** | Random extra data mixed in before hashing, so two users with the same password get different hashes. |
| **bcrypt** | A hashing method that is intentionally slow, so guessing millions of passwords takes forever for an attacker. |
| **TLS** | "TLS stands for Transport Layer Security, a security protocol that encrypts data sent over the internet to keep it private and safe between two communicating applications". The same encryption your browser uses for `https://`. Everything sent is scrambled so outsiders cannot read it. |
| **Certificate** | The server's "ID card" that proves who it is. |
| **Keystore** | A file holding the server's private key and certificate. Only the server has it. |
| **Truststore** | A file holding certificates a client is willing to trust. |
| **Certificate pinning** | The client trusts only our one certificate, nobody else's. A fake server is rejected. |
| **Socket** | One end of a network connection between two programs. |
| **Thread** | A worker that does one job. One thread per connected client lets many clients be served at once. |
| **Interface** | A list of rules ("a user store must be able to add, find, remove...") without saying how it is done. |
| **Record** | A short Java way to make a class that just holds data and cannot be changed after creation. |
| **Environment variable** | A setting you give the terminal (like a password) so it is not written inside the code. |

---

## 3. Milestone 1: Users and passwords

### 3.1 `User.java` (package `models`)

**What it is:** The definition of "a user" in our system.

```java
public record User(String username, String passwordHash, Role role, boolean enabled)
```

A user has four things:

| Field | Meaning |
|---|---|
| `username` | The login name, like `alice` |
| `passwordHash` | The scrambled password. We **never** keep the real password. |
| `role` | `ADMIN` (can manage users) or `USER` (normal person) |
| `enabled` | `true` = can log in, `false` = account switched off |

**Important function: `withEnabled(boolean newEnabled)`**
A `User` cannot be changed once created. To switch an account off, this function
gives you a **new copy** of the user with `enabled` changed. You then save that
copy with `store.update(...)`.

---

### 3.2 `PasswordHasher.java` (package `security`)

**What it is:** The only place in the project that touches passwords. It uses
bcrypt.

**Important functions:**

| Function | What it does | Used when |
|---|---|---|
| `hash(password)` | Turns a real password into a bcrypt hash, with a fresh random salt each time | Creating a new user |
| `verify(password, hash)` | Checks whether a typed password matches a stored hash. Returns `true` or `false`. | Logging in |

How it works in simple words:

```
Creating a user:   "MyPass123"  --hash()-->  $2a$12$k3...(long gibberish)   <- this is saved
Logging in later:  typed "MyPass123" + saved gibberish --verify()--> true
                   typed "wrong"     + saved gibberish --verify()--> false
```

Good to know:

- The `12` in `$2a$12$...` is the cost: how slow the hashing is. Higher means safer but slower.
- Hashing "MyPass123" twice gives two different results (because of the random salt). `verify()` still works because the salt is stored inside the hash.
- If the saved hash is broken or corrupt, `verify()` quietly returns `false` instead of crashing.
- bcrypt only reads the first 72 characters of a password. We will limit password length later.

---

### 3.3 `UserStore.java` (package `storage`)

**What it is:** An **interface**: a contract that says what any "user storage"
must be able to do. It does not say how.

| Function | Meaning |
|---|---|
| `find(username)` | Look up a user. Returns the user or "nothing found". |
| `add(user)` | Add a new user. Returns `false` if the name is already taken. |
| `remove(username)` | Delete a user. |
| `update(user)` | Replace an existing user (for example to disable them). |
| `list()` | Get all users. |

**Why bother?** Right now users are saved in a text file. In Phase 2 they will
live in a database. Because the rest of the program only talks to `UserStore`,
we can swap the file for a database later **without rewriting the server**.

---

### 3.4 `FileUserStore.java` (package `storage`)

**What it is:** The real, working version of `UserStore` that saves users in a
text file (`users.db`).

**How the file looks** (one user per line, parts separated by `|`):

```
# username|bcrypt_hash|ROLE|enabled
alice|$2a$12$abc...|USER|true
admin|$2a$12$xyz...|ADMIN|true
```

**Important functions:**

| Function | What it does |
|---|---|
| Constructor `FileUserStore(file)` | Opens the file and reads all users into memory. If the file does not exist yet, starts empty. |
| `load()` (private) | Reads each line, ignores blank lines and `#` comments, and turns each line into a `User`. |
| `save()` (private) | Writes all users back to the file. It first writes to a temporary file and then swaps it in, so a crash in the middle cannot leave a half-written, broken file. |
| `add(user)` | Checks the username is allowed, rejects duplicates, then saves. |
| `find`, `remove`, `update`, `list` | Do what `UserStore` promises. |

**Safety features, in simple words:**

1. **Username rules:** only letters, numbers and underscore, 3 to 32 characters long. This also prevents anyone from sneaking a `|` or a new line into the file to fake extra users.
2. **`synchronized`:** only one thread can use the store at a time, so two clients logging in at the same moment cannot corrupt the data.
3. **Safe saving:** the temp-file-then-swap trick described above.
4. **No plain passwords:** only hashes ever reach the file.

---

## 4. Milestone 2: Secure connection

### 4.1 `TlsContext.java` (package `security`)

**What it is:** The "encryption setup helper". It turns certificate files into
objects that can create secure sockets.

**Constants:**

- `DEFAULT_PORT = 5443`: the port the server listens on.
- `PROTOCOLS = {"TLSv1.3"}`: only the newest, strongest TLS version is allowed.

**Important functions:**

| Function | Used by | What it does |
|---|---|---|
| `load(file, password)` (private) | both | Opens a `.p12` file with the password. |
| `serverFactory(keystore, password)` | Server | Loads the server's private key and certificate and returns a factory that makes secure **server** sockets. |
| `clientFactory(truststore, password)` | Client | Loads the one certificate the client trusts and returns a factory that makes secure **client** sockets. Anything else is rejected (this is the pinning). |
| `passwordFromEnv()` | both | Reads the file password from the `PROXY_TLS_PASS` environment variable. Stops with a clear message if it is missing. |

Why the password comes from an environment variable: so it is never typed into
the code or saved in git.

---

### 4.2 `NetworkGuard.java` (package `security`)

**What it is:** The doorman that says "local network only".

**The one important function: `isAllowed(address)`**

It returns `true` only if the connecting computer's address is:

| Type | Example |
|---|---|
| This same computer (loopback) | `127.0.0.1` |
| Home / office ranges | `192.168.x.x`, `10.x.x.x`, `172.16.x.x` to `172.31.x.x` |

Anything else (like a public internet address `8.8.8.8`) returns `false` and the
server hangs up immediately.

**Warning:** do not run the server behind ngrok or a similar tunnel. A tunnel
makes every outside visitor look like they come from `127.0.0.1`, so this check
would be fooled.

---

### 4.3 `TlsServer.java` (package `server`)

**What it is:** The program that listens for clients. Right now it is an
"echo server": whatever you send, it sends back with `ECHO: ` in front. That is
enough to prove the secure connection works.

**Important functions:**

**`main()`: the listening loop**

```
1. Load the server key and certificate  (TlsContext.serverFactory)
2. Open a secure server socket on port 5443, TLS 1.3 only
3. Forever:
     wait for a client to connect                      (accept)
     is the client's address local?  (NetworkGuard)    no -> hang up
     hand the client to a worker thread                (pool.execute)
```

The server has a pool of 20 worker threads, so up to 20 clients can be served
at the same time. While one worker talks to a client, the main loop is already
waiting for the next one.

**`handle(socket)`: what one worker does for one client**

```
1. Do the TLS handshake (10 second limit)   <- both sides agree on encryption
2. After that, allow 5 minutes of silence   <- idle clients get dropped
3. Read a line from the client
4. Send back "ECHO: <that line>"
5. If the line was QUIT, stop. Otherwise go to 3.
```

If the handshake fails (for example a client sends plain text instead of
encrypted data), the worker logs `Connection failed` and finishes. The server
itself keeps running.

---

### 4.4 `TlsClient.java` (package `client`)

**What it is:** The program a user runs to connect to the server.

**Important function: `main()`**

```
1. Ask for the server address (for example 127.0.0.1 or 192.168.1.50)
2. Load the truststore: the single certificate we trust   (TlsContext.clientFactory)
3. Connect and run startHandshake()
      -> if the server's certificate is not ours, it fails HERE. Nothing is sent.
4. Print "Secure connection: TLSv1.3 / <cipher>"
5. Loop: read what you type -> send it -> print the server's reply
6. Type QUIT to exit
```

The key line is `socket.startHandshake()`. It forces the security check to
happen before any of your messages are sent.

---

### 4.5 `generate-certs.ps1` (folder `scripts`)

**What it is:** A PowerShell script that creates the three certificate files
using the JDK's `keytool`. Run it once.

| Step | Command | Creates | Who gets it |
|---|---|---|---|
| 1 | `keytool -genkeypair` | `certs/server.p12`: the server's private key and certificate | Server only. Never share. |
| 2 | `keytool -exportcert` | `certs/server.cer`: the public certificate only | Safe to copy to other computers |
| 3 | `keytool -importcert` | `certs/client-truststore.p12`: a truststore holding only that one certificate | Each client |

It refuses to run if `server.p12` already exists, because making a new
certificate would stop every existing client from trusting the server.

---

## 5. Build and project files

### `pom.xml`

The project's settings file for Maven (the build tool).

| Part | Meaning |
|---|---|
| `groupId` / `artifactId` | The project's name: `com.secureproxy` / `secure-proxy` |
| Java 17 | The Java version used to compile |
| `jbcrypt` | The bcrypt library used by `PasswordHasher` |
| `gson` | A JSON library. Not used yet; it is for the login messages in Milestone 3. |
| `junit-jupiter` (test only) | The testing library |
| `maven-surefire-plugin` | The part of Maven that runs the tests |

### `.gitignore`

Tells git which files never to save: `target/` (build output), `certs/` and
`*.p12` (keys), `users.db` (user data), `logs/`.

---

## 6. The tests

Tests are small programs that check the real code. Run all of them with
`mvn clean test`. Right now there are **14**.

### `PasswordHasherTest` (4 tests)

| Test | Checks |
|---|---|
| `correctPasswordVerifies` | The right password is accepted |
| `wrongPasswordFails` | A wrong password is rejected |
| `samePasswordGivesDifferentHashes` | Salting works |
| `malformedHashDoesNotThrow` | Garbage in the hash field does not crash |

### `FileUserStoreTest` (6 tests)

| Test | Checks |
|---|---|
| `addThenFind` | A new user can be found |
| `duplicateUsernameRejected` | The same name cannot be added twice |
| `dataSurvivesReload` | Users are still there after "restarting" |
| `fileNeverContainsPlainPassword` | The real password never appears in the file |
| `invalidUsernameRejected` | Names with bad characters are refused |
| `removeAndDisable` | Disabling and removing users works |

### `NetworkGuardTest` (4 tests)

| Test | Checks |
|---|---|
| `loopbackAllowed` | `127.0.0.1` is allowed |
| `privateRangesAllowed` | Home and office ranges are allowed |
| `publicAddressesRejected` | Internet addresses are blocked |
| `justOutsidePrivate172RangeRejected` | `172.15.x.x` and `172.32.x.x` are blocked (the private range is only 172.16 to 172.31) |

---

## 7. Following one message from start to finish

What happens when a client types `hello`:

```
CLIENT                                                   SERVER
TlsClient.main()                                         TlsServer.main()
  |  loads truststore (TlsContext.clientFactory)           |  loads keystore (TlsContext.serverFactory)
  |                                                        |  waits on port 5443
  |--------------- TCP connection -----------------------> |  accept()
  |                                                        |  NetworkGuard.isAllowed()  -> local? yes
  |                                                        |  handle() starts in a worker thread
  |<========== TLS handshake (certificate check) ========> |
  |  "is this certificate the one I trust?"  yes           |
  |  prints "Secure connection: TLSv1.3"                   |
  |                                                        |
  |--- types "hello" -- encrypted ----------------------> |  readLine() gets "hello"
  |<-- "ECHO: hello" -- encrypted ----------------------- |  println("ECHO: hello")
```

Anyone watching the network in between sees only scrambled data.

---

## 8. Old files

`VpnServer.java` and `VpnClient.java` (your first version) are replaced by
`TlsServer` and `TlsClient`. The old ones sent plain text on port 5000 with no
login. The new ones use encrypted TLS on port 5443. You can keep the old files in
`docs/legacy/` for reference, but they should not be inside `src/`.

---

## 9. What is coming next

| Milestone | What gets added | Which files above it uses |
|---|---|---|
| 3 | Login with JSON messages, lockout after 5 wrong passwords, admin command to add users | `UserStore`, `PasswordHasher`, `TlsServer` |
| 4 | Messaging between users, server admin commands | Login from Milestone 3 |
| 5 | Real tunneling: forwarding traffic through the server | The secure channel from Milestone 2 |
| 6 | File sharing and final documentation | All of the above |
