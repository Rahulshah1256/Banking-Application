import React, { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom';
import { getAllAccounts } from '../services/AccountService';
import { saveTransfer } from '../services/TransferService';

export const TransferComponent = () => {

    const navigator = useNavigate();

    const [amount, setAmount] = useState('')
    const [accounts, setAccounts] = useState([]);
    const [selectedAccount, setSelectedAccount] = useState('');
    const [accountBalance, setAccountBalance] = useState(0);

    const location = useLocation();

    const [beneficiaryDetails, setBeneficiaryDetails] = useState({
        beneficiaryaccountname: '',
        beneficiaryaccountnumber: '',
        beneficiaryaccountifsc: '',
        amountlimit: '',
    });

    // Use ref to keep track of the latest state
    const latestBeneficiaryDetails = useRef(beneficiaryDetails);

    //Get data from benefeciary component

    useEffect(() => {
        if (location.state && location.state.beneficiary) {
            setBeneficiaryDetails(location.state.beneficiary);
            latestBeneficiaryDetails.current = location.state.beneficiary;
            if (latestBeneficiaryDetails.current) {
                listAllAccounts();
            }
        }
    }, [location.state]);

    //load all accounts from API

    function listAllAccounts() {
        getAllAccounts()
            .then((response) => {
                setAccounts(response.data);
            })
            .catch(error => {
                console.error(error);
            })
    }

    useEffect(() => {
        console.log('Updated Accounts:', accounts);
        if (accounts.length > 0) {
            setSelectedAccount(accounts[0].accountNumber);
            setAccountBalance(accounts[0].balance);
            console.log(selectedAccount);
            console.log(accountBalance);
        }
    }, [accounts]);


    function handleAccountChange(e) {
        const selectedAccountNumber = e.target.value;
        setSelectedAccount(selectedAccountNumber);

        // Find the selected account in the accounts array
        const selectedAccount = accounts.find((account) => account.accountNumber === selectedAccountNumber);

        if (selectedAccount) {
            setAccountBalance(selectedAccount.balance);
        } else {
            setAccountBalance(0);
        }
    }

    function saveTransferMoney(e) {
        e.preventDefault();

        const transferAmount = parseFloat(amount);

        if (isNaN(transferAmount)) {
            alert('Please enter a valid amount.');
            return;
        }

        if (transferAmount > latestBeneficiaryDetails.current.amountlimit) {
            alert('The transfer amount exceeds the beneficiary\'s amount limit.');
            return;
        }

        if (transferAmount > accountBalance) {
            alert('Insufficient account balance for the transfer.');
            return;
        }

        const transactionData = {
            fromaccount: selectedAccount,
            toaccount: latestBeneficiaryDetails.current.beneficiaryaccountnumber,
            amount: transferAmount,
            transactiondate: new Date(),
            // Add other properties from your TransactionDto
          };

        saveTransfer(transactionData).then((response) => {
            console.log(response.data);
            navigator('/accounts')
        }).catch(error => {
            console.error(error);
        })

    }


    return (
        <div className='container'>
            <br /> <br />
            <div className='row'>
                <div className='card col-md-6 offset-md-3 offset-md-3'>

                    <h2 className='text-center'>Transfer Money</h2>
                    <div className='card-body'>

                        <form>
                            <div className='form-group mb-2'>
                                <label className='form-label'>Select Account:</label>
                                <select
                                    className='form-control'
                                    name='selectedAccount'
                                    value={selectedAccount}
                                    onChange={(e) => handleAccountChange(e)}
                                >
                                    <option value=''>Select an account</option>
                                    {accounts.map((account) => (
                                        <option key={account.id} value={account.accountNumber}>
                                            {account.accountNumber}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>Account Balance:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    name='accountBalance'
                                    value={accountBalance}
                                    disabled
                                />
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>Name:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    name='beneficiaryaccountname'
                                    value={latestBeneficiaryDetails.current.beneficiaryaccountname}
                                    disabled
                                >
                                </input>
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>Account Number:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    name='beneficiaryaccountnumber'
                                    value={latestBeneficiaryDetails.current.beneficiaryaccountnumber}
                                    disabled
                                >
                                </input>
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>IFSC Code:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    name='beneficiaryaccountifsc'
                                    value={latestBeneficiaryDetails.current.beneficiaryaccountifsc}
                                    disabled
                                >
                                </input>
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>Amount Limit:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    name='amountlimit'
                                    value={latestBeneficiaryDetails.current.amountlimit}
                                    disabled
                                >
                                </input>
                            </div>

                            <div className='form-group mb-2'>
                                <label className='form-label'>Amount:</label>
                                <input
                                    type='text'
                                    className='form-control'
                                    placeholder='Enter amount'
                                    name='amount'
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                >
                                </input>
                            </div>

                            <button className='btn btn-success' onClick={(e) => saveTransferMoney(e)}> Transfer</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    )
}
