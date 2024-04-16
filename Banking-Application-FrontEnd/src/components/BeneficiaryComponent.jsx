import React, { useEffect, useState } from 'react'
import { getAllBeneficiaries } from '../services/BeneficiaryService';
import { useNavigate } from 'react-router-dom';

const BeneficiaryComponent = () => {

    const [beneficiaries, setBeneficiaries] = useState([])

    const [selectedBeneficiary, setSelectedBeneficiary] = useState(null);

    const navigate =useNavigate();

    useEffect(()=>{
        listAllBeneficiaries();
    },[])

    function listAllBeneficiaries(){
        getAllBeneficiaries().then((response)=>{
            setBeneficiaries(response.data);
        }).catch(error =>{
            console.error(error);
        })
    }

    function addNewBeneficiary()
	{
	    navigate('/add-beneficiary')
	}

    function transferMoneyRequest(beneficiary)
	{
        setSelectedBeneficiary(beneficiary);
		navigate('/transfer', { state: { beneficiary } });
	}

  return (
    <div className='container'>
    <h2 className='text-center'>My Beneficiaries</h2>
    <button className='btn btn-primary mb-2' onClick={addNewBeneficiary}>Add Beneficiary</button>
    <div>
        <table className='table table-bordered table-stripped'>
            <thead>
                <tr>
                    <th>
                        Account Name
                    </th>
                    <th>
                        IFSC
                    </th>
                    <th>
                        Account Number
                    </th>
                    <th>
                        Amount Limit
                    </th>
                    <th>
                        Status
                    </th>
                </tr>
            </thead>
            <tbody>
                {
                    beneficiaries.map(beneficiary =>
                        <tr key={beneficiary.id}>
                            <td>{beneficiary.beneficiaryaccountname}</td>
                            <td>{beneficiary.beneficiaryaccountifsc}</td>
                            <td>{beneficiary.beneficiaryaccountnumber}</td>
                            <td>{beneficiary.amountlimit}</td>
                            <td>{beneficiary.status}</td>
                            <td>
								<button className='btn btn-info' onClick={() =>transferMoneyRequest(beneficiary)}>Transfer Money</button>
							</td>
                        </tr>
                    )
                }                
            </tbody>
        </table>
    </div>
</div>
  )
}

export default BeneficiaryComponent