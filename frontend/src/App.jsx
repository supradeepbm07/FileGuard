import { useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api";

function App() {
  const [page, setPage] = useState("login");

  const [username, setUsername] = useState("admin");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("FileGuard@123");

  const [token, setToken] = useState("");
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  // =========================
  // REGISTER
  // =========================
  const handleRegister = async (event) => {
    event.preventDefault();

    try {
      setMessage("Creating account...");

      const response = await fetch(`${API}/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          email,
          password,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || data.error || "Registration failed"
        );
      }

      setMessage("Registration successful! Please login.");

      setEmail("");
      setPassword("");

      setTimeout(() => {
        setPage("login");
        setMessage("");
      }, 1200);

    } catch (error) {
      setMessage(error.message);
    }
  };

  // =========================
  // LOGIN
  // =========================
  const handleLogin = async (event) => {
    event.preventDefault();

    try {
      setMessage("Logging in...");

      const response = await fetch(`${API}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          password,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || data.error || "Login failed"
        );
      }

      setToken(data.token);
      setMessage("Login successful.");
    } catch (error) {
      setMessage(error.message);
    }
  };

  // =========================
  // FILE SELECTION
  // =========================
  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];

    if (selectedFile) {
      setFile(selectedFile);
      setResult(null);
      setMessage("");
    }
  };

  // =========================
  // FILE UPLOAD
  // =========================
  const handleUpload = async () => {
    if (!file) {
      setMessage("Please select a file.");
      return;
    }

    if (!token) {
      setMessage("Please login first.");
      return;
    }

    try {
      setLoading(true);
      setMessage("Analyzing and encrypting file...");
      setResult(null);

      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch(`${API}/files/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message ||
            data.error ||
            "File upload failed."
        );
      }

      setResult(data);
      setMessage(
        "File analyzed and encrypted successfully."
      );

    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  // =========================
  // RISK CLASS
  // =========================
  const getRiskClass = () => {
    if (!result?.riskAnalysis?.riskLevel) {
      return "";
    }

    return result.riskAnalysis.riskLevel.toLowerCase();
  };

  return (
    <div className="app">

      {/* ================= HEADER ================= */}
      <header className="header">

        <div>
          <h1>🛡️ FileGuard</h1>
          <p>Secure File Protection & Risk Analysis</p>
        </div>

        <div
          className={
            token ? "status online" : "status"
          }
        >
          {token
            ? "● Authenticated"
            : "● Not Logged In"}
        </div>

      </header>

      <main className="container">

        {/* ================================================= */}
        {/* REGISTER PAGE */}
        {/* ================================================= */}

        {!token && page === "register" && (

          <section className="card login-card">

            <div className="login-icon">
              📝
            </div>

            <h2>Create FileGuard Account</h2>

            <p className="muted">
              Register to securely analyze and protect your files.
            </p>

            <form onSubmit={handleRegister}>

              <label>Username</label>

              <input
                type="text"
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                placeholder="Enter username"
                required
              />

              <label>Email</label>

              <input
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="Enter email"
                required
              />

              <label>Password</label>

              <input
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Minimum 8 characters"
                required
              />

              <button
                type="submit"
                className="primary-button"
              >
                📝 Register
              </button>

            </form>

            {message && (
              <p className="message">
                {message}
              </p>
            )}

            <p className="switch-text">
              Already have an account?
            </p>

            <button
              className="secondary-button"
              onClick={() => {
                setPage("login");
                setMessage("");
              }}
            >
              🔑 Go to Login
            </button>

          </section>

        )}

        {/* ================================================= */}
        {/* LOGIN PAGE */}
        {/* ================================================= */}

        {!token && page === "login" && (

          <section className="card login-card">

            <div className="login-icon">
              🔐
            </div>

            <h2>FileGuard Login</h2>

            <p className="muted">
              Sign in to securely analyze and protect your files.
            </p>

            <form onSubmit={handleLogin}>

              <label>Username</label>

              <input
                type="text"
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                placeholder="Enter username"
                required
              />

              <label>Password</label>

              <input
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Enter password"
                required
              />

              <button
                type="submit"
                className="primary-button"
              >
                🔑 Login
              </button>

            </form>

            {message && (
              <p className="message">
                {message}
              </p>
            )}

            <p className="switch-text">
              Don't have an account?
            </p>

            <button
              className="secondary-button"
              onClick={() => {
                setPage("register");
                setMessage("");
              }}
            >
              📝 Create New Account
            </button>

          </section>

        )}

        {/* ================================================= */}
        {/* DASHBOARD */}
        {/* ================================================= */}

        {token && (

          <>

            <section className="hero">

              <div>

                <p className="eyebrow">
                  SECURITY DASHBOARD
                </p>

                <h2>
                  Protect Your Files
                </h2>

                <p>
                  Upload a file to analyze its risk
                  profile and securely encrypt it
                  using AES-256-GCM.
                </p>

              </div>

              <div className="security-badge">
                🔒 Secure Session
              </div>

            </section>


            <section className="upload-card">

              <div className="upload-area">

                <div className="upload-icon">
                  📤
                </div>

                <h3>
                  {file
                    ? file.name
                    : "Upload a file for analysis"}
                </h3>

                <p>
                  {file
                    ? `${(
                        file.size / 1024
                      ).toFixed(2)} KB`
                    : "Select any file from your computer"}
                </p>

                <input
                  id="fileInput"
                  type="file"
                  onChange={handleFileChange}
                />

                <label
                  htmlFor="fileInput"
                  className="choose-button"
                >
                  Choose File
                </label>

                {file && (

                  <button
                    className="analyze-button"
                    onClick={handleUpload}
                    disabled={loading}
                  >
                    {loading
                      ? "⏳ Processing..."
                      : "🔍 Analyze & Encrypt"}
                  </button>

                )}

              </div>

            </section>


            {message && (

              <div className="notification">
                {message}
              </div>

            )}


            {result && (

              <section className="results">

                <div
                  className={`risk-card ${getRiskClass()}`}
                >

                  <div>

                    <span className="result-label">
                      RISK LEVEL
                    </span>

                    <h2>
                      {result.riskAnalysis.riskLevel}
                    </h2>

                    <p>
                      File security assessment completed
                    </p>

                  </div>

                  <div className="risk-score">

                    <span>
                      Risk Score
                    </span>

                    <strong>
                      {result.riskAnalysis.riskScore}
                    </strong>

                    <small>
                      / 100
                    </small>

                  </div>

                </div>


                <div className="result-grid">

                  <div className="card">

                    <h3>
                      📄 File Information
                    </h3>

                    <div className="info-row">
                      <span>File Name</span>

                      <strong>
                        {result.originalFileName}
                      </strong>
                    </div>

                    <div className="info-row">
                      <span>File Size</span>

                      <strong>
                        {result.fileSize} bytes
                      </strong>
                    </div>

                    <div className="info-row">
                      <span>Extension</span>

                      <strong>
                        .{result.riskAnalysis.extension}
                      </strong>
                    </div>

                    <div className="info-row">
                      <span>MIME Type</span>

                      <strong>
                        {result.riskAnalysis.detectedMimeType}
                      </strong>
                    </div>

                  </div>


                  <div className="card encryption-card">

                    <h3>
                      🔒 Encryption Status
                    </h3>

                    <div className="encryption-status">

                      <span>✓</span>

                      <strong>
                        Successfully Encrypted
                      </strong>

                    </div>

                    <div className="info-row">

                      <span>
                        Algorithm
                      </span>

                      <strong>
                        AES-256-GCM
                      </strong>

                    </div>

                    <div className="info-row">

                      <span>
                        Encrypted File
                      </span>

                      <strong className="path">
                        {result.encryptedFile}
                      </strong>

                    </div>

                  </div>

                </div>


                <div className="secure-banner">

                  <span>🛡️</span>

                  <div>

                    <strong>
                      FileGuard Protection Complete
                    </strong>

                    <p>
                      Your file has been analyzed
                      and encrypted successfully.
                    </p>

                  </div>

                </div>

              </section>

            )}

          </>

        )}

      </main>


      <footer>
        FileGuard • Secure File Protection System
      </footer>

    </div>
  );
}


export default App;