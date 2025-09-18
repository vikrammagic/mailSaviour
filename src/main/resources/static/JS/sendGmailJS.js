document.addEventListener('DOMContentLoaded', () => {
  // Retrieve sessionId from session storage
  const sessionId = sessionStorage.getItem('sessionId');

  // Check if sessionId exists
  if (!sessionId) {
    console.error("Missing sessionId in session storage!");
    return; // Exit if no sessionId
  }

  // Create a new FormData object
  const formData = new FormData();
  formData.append('sessionId', sessionId); // Add sessionId as a form parameter

  fetch('/messages/greet', {
    method: 'POST', // Use POST method for this endpoint
    body: formData // Send sessionId in the request body
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('Failed to fetch greet content');
      }
      return response.text();
    })
    .then(content => {
      document.getElementById("greet").textContent = content;
    })
    .catch(error => {
      console.error("Error fetching greet content:", error);
      document.getElementById("greet").textContent = "Connection Error!";
    });
});

function updateMailCount() {
  const mailCntSpan = document.getElementById('mailCnt');
  const emailTextarea = document.getElementById('eMails');
  const emailRegex = /^(?!.*\.\.)(?!\.)[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  const validEmails = emailTextarea.value
    .split('\n')
    .map(line => line.trim())
    .filter(line => line !== '')
    .filter(email => email.match(emailRegex));

  mailCntSpan.textContent = validEmails.length; // Update the mail count display

  // Change font size if count exceeds 30
  if (validEmails.length > 30) {
      mailCntSpan.style.fontSize = "150%";
  } else {
      mailCntSpan.style.fontSize = "100%";
  }
}


//theme
const toggleThemeButton = document.getElementById('toggleTheme');
const body = document.body;

toggleThemeButton.addEventListener('click', () => {
  body.classList.toggle('light');
  body.classList.toggle('dark');
});

// Set initial theme (optional)
if (localStorage.getItem('theme') === 'dark') {
  body.classList.add('dark');
 // input.classList.add('dark');
} else {
  body.classList.add('light');
  //input.classList.add('light');
}




//Submission
function validate() {
  event.preventDefault();

  const mailCount = parseInt(document.getElementById('mailCnt').innerText, 10);
  const but = document.querySelector('input[type="submit"]');
  const emailValue = document.getElementById('sentFrom').value;
  const emailRegex = /^(?!.*\.\.)(?!\.)[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  const bodyTextarea = document.getElementById('body');
  const bodyText = bodyTextarea.value.trim();
  const wordCount = bodyText.split(/\s+/).length;


  const emailTextarea = document.getElementById('eMails');
  const appPassInput = document.getElementById('appPass');
  const validEmails = emailTextarea.value
    .split('\n')
    .map(line => line.trim())
    .filter(line => line !== '')
    .filter(email => email.match(emailRegex));

  let trimmedAppPass = appPassInput.value.trim();
  trimmedAppPass = trimmedAppPass.replace(/\s/g, '');

  const formattedAppPass = trimmedAppPass.match(/.{1,4}/g).join(' ');

  but.style.backgroundColor = "#0400ff";

  switch (true) {
    case (mailCount > 30):
      alert("Please enter less than 31 recipients");
      but.style.backgroundColor = "#45a049";
      return;

    case (!emailRegex.test(emailValue)):
      alert('Invalid email sentFrom format. Please check and try again.');
      but.style.backgroundColor = "#45a049";
      return false;

    case (wordCount > 10000):
      alert("Body cannot contain more than 10000 words.");
      but.style.backgroundColor = "#45a049";
      return;

    case (trimmedAppPass.length !== 16 || !trimmedAppPass.match(/^[a-z]+$/)):
      alert("Invalid app password: Must be 16 lowercase letters.");
      but.style.backgroundColor = "#45a049";
      return;

    case (mailCount === 0):
      alert("Enter at least 1 valid recipient.");
      but.style.backgroundColor = "#45a049";
      return;

    default:
      appPassInput.value = formattedAppPass;
      emailTextarea.value = validEmails.join('\n');
      fetch('/sendGmail', {
        method: 'POST',
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sentFrom: document.getElementById('sentFrom').value,
          appPass: formattedAppPass,
          subject: document.getElementById('subject').value,
//          body: bodyHTML,
          realBody: bodyText,
          eMails: validEmails, // Send as an array
          firstName: document.getElementById('firstName').value,
          sessionId: sessionStorage.getItem('sessionId'),
          isPort587: document.getElementById('port').checked
        })
      })
      .then(response => response.text())
      .then(message => {
          alert(message);
          but.style.backgroundColor = "#45a049";
      })
      .catch(error => {
          console.error('Error sending emails:', error);
          alert("There was an error sending emails.");
          but.style.backgroundColor = "#45a049";
      });

      break;
  }
}
