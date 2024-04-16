import React from 'react';
import { toast } from 'react-toastify';

const RegistrationSuccess = () => {

  toast.success('Congratulations your account request is saved successfully')
  toast.success('You will recieve a Bank confirmation once your details are verified and Account generated')

  return (
    <div className="registration-success">
      <h2>Registration Successfull</h2>
      <p>Congratulations!!! your account request is saved successfully.</p>
      <p>You will recieve a Bank confirmation once your details are verified and Account generated.</p>
    </div>
  );
};

export default RegistrationSuccess;
