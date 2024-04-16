import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { isAdminUser } from '../services/AuthService';
import { getAllUserRequests,rejectUser,generateUser } from '../services/UserService';
import { toast } from 'react-toastify';


const AccountRequestsComponent = () => { 

	
		const [users, setUsers] = useState([])

		const navigate =useNavigate()

		const isAdmin = isAdminUser();

		useEffect(()=>{
			listAllUsers();
		},[])

		function listAllUsers(){
			getAllUserRequests().then((response)=>{
				setUsers(response.data);
			}).catch(error =>{
				console.error(error);
			})
		}

		function addNewAccount()
		{
			navigate('/add-account')
		}

		function rejectUserRequest(id)
		{
			rejectUser(id).then((response)=>{
				console.log(response.data);
				toast.success('Account request is rejected successfully!');
				listAllUsers()
			}).catch(error=>{
				toast.error(error)
				console.error(error);
			})
		}

		function generateAccountRequest(id)
		{
			generateUser(id).then((response)=>{
				console.log(response.data);
				toast.success('Account generated successfully!');
				listAllUsers()
			}).catch(error=>{
				toast.error(error)
				console.error(error);
			})
		}


	return (  
		<div className='container'>
			<h2 className='text-center'>List of Accounts Requests</h2>
			{
			  isAdmin &&	
			  <button className='btn btn-primary mb-2' onClick={addNewAccount}>Add Account</button>
			}
			<div>
				<table className='table table-bordered table-stripped'>
					<thead>
						<tr>
							<th>
								Full Name
							</th>
							<th>
								Aadhaar
							</th>
							<th>
								PAN
							</th>
							<th>
								Mobile No
							</th>
							<th>
								Email
							</th>
							<th>
								Address
							</th>
							<th>
								Actions
							</th>
						</tr>
					</thead>
					<tbody>
						{
							users.map(user =>
								<tr key={user.id}>
									<td>{user.name}</td>
									<td>{user.aadhaarno}</td>
									<td>{user.panno}</td>
									<td>{user.mobile}</td>
									<td>{user.email}</td>
									<td>{user.address}</td>
									<td>
										<button className='btn btn-info' onClick={() =>generateAccountRequest(user.id)}>Generate</button>
										<button className='btn btn-danger' onClick={(e)=> rejectUserRequest(user.id)} style={{margin: "10px"}}> Reject</button>
									</td>
								</tr>
							)
						}
						
					</tbody>
				</table>
			</div>
		</div>
	
	); 
}
export default AccountRequestsComponent;
