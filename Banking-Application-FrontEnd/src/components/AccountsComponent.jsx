import React, { useEffect, useState } from 'react'
import { getAllAccounts } from '../services/AccountService';
import { isAdminUser } from '../services/AuthService';
import { toast } from 'react-toastify';
import { useLocation } from 'react-router-dom';

const AccountsComponent = () => {

    const [accounts, setAccounts] = useState([])
    const isAdmin = isAdminUser();
	const location = useLocation();

    useEffect(()=>{
		const isComingFromLogin = location.state && location.state.from === 'login';
        if (isComingFromLogin) {
            toast.success('Logged in successfully');
        }
        listAllAccounts();
	}, [location.state]);

    function listAllAccounts(){
        getAllAccounts().then((response)=>{
            setAccounts(response.data);
        }).catch(error =>{
            console.error(error);
        })
    }

    return (  
		<div className='container'>
			{
            isAdmin &&
			<h2 className='text-center'>List of Accounts</h2>
            }
			{
            !isAdmin &&
			<h2 className='text-center'>My Accounts</h2>
            }
			<div>
				<table className='table table-bordered table-stripped'>
					<thead>
						<tr>
							<th>
								Account Number
							</th>
							<th>
                                Account Holders Name
							</th>
							<th>
								Account Type
							</th>
							<th>
								Branch Id
							</th>
							<th>
								IFSC Code
							</th>
							<th>
								Balance
							</th>
							<th>
                                Opening Date
							</th>
                            <th>
								Address
							</th>
							<th>
								Contact Number
							</th>
							<th>
                                Email Address
							</th>
							<th>
                                Nominee
							</th>
						</tr>
					</thead>
					<tbody>
						{
							accounts.map(account =>
								<tr key={account.id}>
                                    <td>{account.accountNumber}</td>
									<td>{account.accountHolderName}</td>
									<td>{account.accountType}</td>
									<td>{account.branchId}</td>
									<td>{account.ifscCode}</td>
									<td>{account.balance}</td>
									<td>{account.openDate}</td>
                                    <td>{account.address}</td>
									<td>{account.contactNumber}</td>
									<td>{account.emailAddress}</td>
                                    <td>{account.nominee}</td>
								</tr>
							)
						}
						
					</tbody>
				</table>
			</div>
		</div>
	
	); 
}

export default AccountsComponent