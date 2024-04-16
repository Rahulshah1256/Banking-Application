document.addEventListener("DOMContentLoaded", function () {
    const beneficiaryForm = document.getElementById("beneficiary-form");

    beneficiaryForm.addEventListener("submit", function (e) {
        e.preventDefault();

        // Get form data
        const name = document.getElementById("name").value;
        const bankName = document.getElementById("bankName").value;
        const accountNo = document.getElementById("accountNo").value;
        const maxLimit = document.getElementById("maxLimit").value;

        // Create a new beneficiary object
        const beneficiary = {
            name,
            bankName,
            accountNo,
            maxLimit,
        };

        // Here, you can send the beneficiary data to a server for processing and storage
        // You can use AJAX, Fetch API, or any other method to make an HTTP request to your backend

        // For now, we'll just log the beneficiary data
        console.log("New Beneficiary:", beneficiary);

        // Optionally, you can clear the form fields
        beneficiaryForm.reset();
    });
});
