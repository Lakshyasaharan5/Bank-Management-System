// Script to toggle the login form based on the button clicked
document.getElementById('customer-login-btn').addEventListener('click', function() {
    showForm('Customer Login');
});

document.getElementById('employee-login-btn').addEventListener('click', function() {
    showForm('Employee Login');
});

function showForm(title) {
    const formContainer = document.getElementById('login-form');
    const formTitle = document.getElementById('form-title');

    formTitle.textContent = title;
    formContainer.classList.remove('hidden');
    formContainer.style.display = 'block';
}
