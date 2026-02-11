import { useState, FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Signup() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { setUser } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    try {
      const res = await fetch("http://localhost:8080/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          username,
          password
        })
      });

      if (!res.ok) throw new Error("Signup failed");
      const data = await res.json();
      setUser(data);
      navigate("/onboarding");
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("Something went wrong");
        }
      }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-[#F7F7F7] px-4">
      <div className="w-full max-w-md bg-white p-8 drop-shadow rounded">
        <div className="flex flex-row items-center justify-center border-b border-gray-300 pb-4 mb-6">
          <img src="/src/assets/bird.svg" alt="bird" className="w-8 h-8"/>
          <h2 className="text-2xl font-bold ml-3">Sign Up</h2>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              className="w-full px-4 py-3 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div>
            <input
              className="w-full px-4 py-3 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button 
            className="w-full py-3 bg-blue-600 text-white rounded font-semibold hover:bg-blue-700 transition duration-200"
            type="submit"
          >
            Create Account
          </button>
        </form>

        {error && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded">
            <p className="text-red-600 text-sm text-center">{error}</p>
          </div>
        )}

        <div className="mt-6 text-center text-sm text-gray-600">
          Already have an account?{" "}
          <Link to="/login" className="text-blue-500 hover:text-blue-600 font-semibold">
            Log in
          </Link>
        </div>
      </div>
    </div>
  );
}