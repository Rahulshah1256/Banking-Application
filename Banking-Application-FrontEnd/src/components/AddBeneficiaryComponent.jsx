import React, { useState } from 'react'
import { saveBeneficiary } from '../services/BeneficiaryService'
import { useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'

const AddBeneficiaryComponent = () => {

  const [beneficiaryaccountname, setBeneficiaryAccountName] = useState('')
  const [beneficiaryaccountnumber, setBeneficiaryAccountNumber] = useState('')
  const [beneficiaryaccountifsc, setBeneficiaryAccountIfsc] = useState('')
  const [amountlimit, setAmountLimit] = useState('')
  const [error, setError] = useState('');

  const navigate = useNavigate();

  function saveNewBeneficiary(e) {
    e.preventDefault()

    const beneficiary = { beneficiaryaccountname, beneficiaryaccountnumber, beneficiaryaccountifsc, amountlimit }

    // Simple form validation
    if (!beneficiaryaccountname || !beneficiaryaccountnumber || !beneficiaryaccountifsc || !amountlimit) {
      setError('All feilds are mandatory.');
      if (!beneficiaryaccountname) {
        toast.error('Beneficiary account name is required.');
      }
      if (!beneficiaryaccountnumber) {
        toast.error('Beneficiary account number is required.');
      }
      if (!beneficiaryaccountifsc) {
        toast.error('Beneficiary account IFSC is required.');
      }
      if (!amountlimit) {
        toast.error('Amount transfer limit is required.');
      }
      return;
    }

    // Clear previous error messages
    setError('');
debugger;
    saveBeneficiary(beneficiary).then((response) => {
      console.log(response.data);
      navigate('/beneficiaries')
    }).catch(error => {
      console.error(error);
      toast.error(error);
    })

  }

  return (
    <div className='container'>
      <br /> <br />
      <div className='row'>
        <div className='card col-md-6 offset-md-3 offset-md-3'>

          <h2 className='text-center'>Add Beneficiary</h2>
          <div className='card-body'>

            <form>
              <div className='form-group mb-2'>
                <label className='form-label'>Name:</label>
                <input
                  type='text'
                  className='form-control'
                  placeholder='Enter name'
                  name='beneficiaryaccountname'
                  value={beneficiaryaccountname}
                  onChange={(e) => setBeneficiaryAccountName(e.target.value)}
                >
                </input>
              </div>

              <div className='form-group mb-2'>
                <label className='form-label'>Account Number:</label>
                <input
                  type='text'
                  className='form-control'
                  placeholder='Enter account number'
                  name='beneficiaryaccountnumber'
                  value={beneficiaryaccountnumber}
                  onChange={(e) => setBeneficiaryAccountNumber(e.target.value)}
                >
                </input>
              </div>

              <div className='form-group mb-2'>
                <label className='form-label'>IFSC Code:</label>
                <input
                  type='text'
                  className='form-control'
                  placeholder='Enter IFSC Code'
                  name='beneficiaryaccountifsc'
                  value={beneficiaryaccountifsc}
                  onChange={(e) => setBeneficiaryAccountIfsc(e.target.value)}
                >
                </input>
              </div>

              <div className='form-group mb-2'>
                <label className='form-label'>Amount Limit:</label>
                <input
                  type='text'
                  className='form-control'
                  placeholder='Enter amount limit'
                  name='amountlimit'
                  value={amountlimit}
                  onChange={(e) => setAmountLimit(e.target.value)}
                >
                </input>
              </div>

              <button className='btn btn-success' onClick={(e) => saveNewBeneficiary(e)}> Save</button>
              <br />
              {/* Display error message */}
              {error && <div className='alert alert-danger'>{error}</div>}
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AddBeneficiaryComponent