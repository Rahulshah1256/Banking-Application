import React, { useEffect } from 'react'
import { useState } from 'react'
import { getAccount, saveAccount, updateAccount } from '../services/AccountService'
import { useNavigate } from 'react-router-dom'
import { useParams } from 'react-router-dom'

const AccountComponent = () => {

  const [name,setName]=useState('')
  const [email,setEmail]=useState('')
  const [aadhaarno,setAadhaarno]=useState('')
  const [completed,setCompleted]=useState(false)
  const navigate = useNavigate()
  const {id} = useParams()
 
  function saveOrUpdateAccount(e){
    e.preventDefault()
    const account = {name,email,aadhaarno,completed}
    
    console.log(account)

    if(id){
      updateAccount(id,account).then((response)=>{
        console.log(response.data);
        navigate('/accounts-requests')
      }).catch(error =>{
        console.error(error);
      })
    }else{
      saveAccount(account).then((response)=>{
        console.log(response.data);
        navigate('/accounts-requests')
      }).catch(error =>{
        console.error(error);
      })
    }
  }

  function pageTitle(){
    if(id){
          return  <h2 className='text-center'>Update Account</h2>
    }else{
          return  <h2 className='text-center'>Add Account</h2>
    }
  }

  useEffect(()=>{
    if(id){
      getAccount(id).then((response)=>{
        console.log(response.data);
        setName(response.data.name);
        setEmail(response.data.email);
        setAadhaarno(response.data.aadhaarno);
        setCompleted(response.data.completed);
      }).catch(error=>{
        console.error(error);
      })
    }

  },[id])

  return (
    <div className='container'>
      <br /> <br />
        <div className='row'>
            <div className='card col-md-6 offset-md-3 offset-md-3'>

                  {pageTitle()}
                  <div className='card-body'>

                    <form>
                      <div className='form-group mb-2'>
                          <label className='form-label'>Account Title:</label>
                          <input
                              type='text'
                              className='form-control'
                              placeholder='Enter account title'
                              name='title'
                              value={title}
                              onChange={(e) => setTitle(e.target.value)}
                          >
                          </input>
                      </div>

                      <div className='form-group mb-2'>
                          <label className='form-label'>Account Description:</label>
                          <input
                              type='text'
                              className='form-control'
                              placeholder='Enter account description'
                              name='description'
                              value={description}
                              onChange={(e) => setDescription(e.target.value)}
                          >
                          </input>
                      </div>

                      <div className='form-group mb-2'>
                          <label className='form-label'>Account Generated:</label>
                          <select
                              type='text'
                              className='form-control'
                              placeholder='Enter account generated'
                              name='completed'
                              value={completed}
                              onChange={(e) => setCompleted(e.target.value)}
                          >
                            <option value="false">No</option>
                            <option value="true">Yes</option>
                          </select>
                      </div>
                      <button className='btn btn-success' onClick={(e)=> saveOrUpdateAccount(e)}> Save</button>
                    </form>

                  </div>
            
            
            </div>
        </div>
    </div>
  )
}

export default AccountComponent