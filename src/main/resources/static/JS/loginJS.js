(function(e) {
  e.onload = function() {
    e.sessionStorage.removeItem("sessionId");

    fetch('/messages/intro')
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to fetch intro content');
        }
        return response.text();
      })
      .then(content => {
        document.getElementById("intro").textContent = content;
      })
      .catch(error => {
        console.error("Error fetching intro content:", error);
        document.getElementById("intro").textContent = "Connection Error!";
      });
  };

  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const usernameError = document.getElementById("usernameError");
  const passwordError = document.getElementById("passwordError");
  const submitButton = document.getElementById("submitButton");

  submitButton.addEventListener("click", function(event) {
    event.preventDefault();

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    usernameError.textContent = "";
    passwordError.textContent = "";

    if (username.length !== 10) {
      usernameError.textContent = "Username must be 10 characters long.";
      return;
    }

    if (password.length !== 16) {
      passwordError.textContent = "Password must be 16 characters long.";
      return;
    }

    fetch("/loginForm", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userName: username, password: password })
    })
    .then(response => {
      if (!response.ok) {
        // Improved error handling: Attempt to parse JSON error response
        return response.json().then(errData => {
          const errorMessage = errData.message || `Login failed with status: ${response.status}`;
          throw new Error(errorMessage); // Re-throw the error for the catch block
        }).catch(() => { // Handle cases where the error response is not JSON
          throw new Error(`Login failed with status: ${response.status}`);
        });
      }
      return response.json(); // Proceed if the response is ok
    })
    .then(data => {
      if (data.status === 200) { // Check status code from server
        try {
          sessionStorage.setItem("sessionId", data.sessionId);
          window.location.href = data.redirectUrl;
        } catch (error) {
          console.error("Error during session storage or redirect:", error);
          alert("An error occurred during login. Please try again."); // Or a more specific message
        }
      } else {
        alert(data.message || "Login failed. Please try again.");
      }
    })
    .catch(error => {
      console.error("Error occurred during login:", error);
      alert(error.message || "An unexpected error occurred. Please try again.");
    });
  });
})(this);