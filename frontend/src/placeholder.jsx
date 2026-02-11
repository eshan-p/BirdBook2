// import { useState } from "react";
// import "./App.css";

// function placeholder() {
//   const [username, setUsername] = useState("");
//   const [password, setPassword] = useState("");
//   const [message, setMessage] = useState("");

//   const handleSubmit = async (e) => {
//     e.preventDefault();

//     try {
//       const response = await fetch("http://localhost:8080/auth/login", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json"
//         },
//         body: JSON.stringify({
//           username: username,
//           password: password,   // included for requirement
//           role: "BASIC"          // REQUIRED by backend
//         })
//       });

//       if (!response.ok) {
//         throw new Error("Login failed");
//       }

//       const data = await response.json();
//       localStorage.setItem("token", data.token);
//       setMessage("Login successful. Token saved.");
//     } catch (error) {
//       setMessage("Login failed: " + error);
//     }
//   };

//   return (
//     <div className="login-container">
//       <h1>BirdBook</h1>

//       <form className="login-form" onSubmit={handleSubmit}>
//         <input
//           type="text"
//           placeholder="Username"
//           value={username}
//           onChange={(e) => setUsername(e.target.value)}
//           required
//         />

//         <input
//           type="password"
//           placeholder="Password"
//           value={password}
//           onChange={(e) => setPassword(e.target.value)}
//           required
//         />

//         <button type="submit">Log In</button>
//       </form>

//       <p className="login-message">{message}</p>
//     </div>
//   );
// }

// export default App;
